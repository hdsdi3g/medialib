/*
 * This file is part of fflauncher.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * Copyright (C) hdsdi3g for hd3g.tv 2022
 *
 */
package tv.hd3g.fflauncher.recipes;

import static net.datafaker.Faker.instance;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static tv.hd3g.fflauncher.ConversionTool.APPEND_PARAM_AT_END;
import static tv.hd3g.fflauncher.enums.FFLogLevel.WARNING;
import static tv.hd3g.fflauncher.recipes.MediaAnalyserSession.extractEbur128TargetFromAFilterChains;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import net.datafaker.Faker;
import tv.hd3g.fflauncher.FFmpeg;
import tv.hd3g.fflauncher.filtering.AbstractFilterMetadata.Mode;
import tv.hd3g.fflauncher.filtering.AudioFilterAMetadata;
import tv.hd3g.fflauncher.filtering.AudioFilterSupplier;
import tv.hd3g.fflauncher.filtering.Filter;
import tv.hd3g.fflauncher.filtering.FilterChains;
import tv.hd3g.fflauncher.filtering.VideoFilterMetadata;
import tv.hd3g.fflauncher.filtering.VideoFilterSupplier;
import tv.hd3g.fflauncher.filtering.lavfimtd.LavfiMtdEvent;
import tv.hd3g.fflauncher.filtering.lavfimtd.LavfiMtdSiti;
import tv.hd3g.fflauncher.filtering.lavfimtd.LavfiMtdValue;
import tv.hd3g.ffprobejaxb.FFprobeJAXB;
import tv.hd3g.ffprobejaxb.data.FFProbeStream;
import tv.hd3g.processlauncher.InvalidExecution;
import tv.hd3g.processlauncher.LineEntry;
import tv.hd3g.processlauncher.Processlauncher;
import tv.hd3g.processlauncher.ProcesslauncherLifecycle;
import tv.hd3g.processlauncher.cmdline.ExecutableFinder;
import tv.hd3g.processlauncher.cmdline.Parameters;

class MediaAnalyserSessionTest {
	private static final String EBUR128_TARGET = "ebur128=target=";

	static Faker faker = instance();

	final Optional<Supplier<Stream<String>>> emptyLavfiLinesToMerge = Optional.empty();
	final static String SYSOUT = """
			frame:1022 pts:981168 pts_time:20.441
			lavfi.aphasemeter.phase=1.000000
			lavfi.aphasemeter.mono_start=18.461
			""";

	MediaAnalyserSession s;

	Parameters parameters;
	String source;
	String aFilterValue;
	String vFilterValue;
	String sourceFilePath;
	String ebur128Result;
	String ebur128event;
	String rawEvent;
	String commandLine;

	@Mock
	MediaAnalyser mediaAnalyser;
	@Mock
	ExecutableFinder executableFinder;
	@Mock
	ProcesslauncherLifecycle processLifecycle;
	@Mock
	Processlauncher processlauncher;
	@Mock
	File sourceFile;
	@Mock
	FFmpeg ffmpeg;
	@Mock
	AudioFilterSupplier aF;
	@Mock
	VideoFilterSupplier vF;
	@Mock
	Filter aFilter;
	@Mock
	Filter vFilter;
	@Mock
	FFprobeJAXB ffprobeResult;
	@Mock
	FFProbeStream streamType;

	MediaAnalyserSessionFilterContext vFilterContext;
	MediaAnalyserSessionFilterContext aFilterContext;

	@BeforeEach
	void init() throws Exception {
		openMocks(this).close();

		source = faker.numerify("source###");
		sourceFilePath = faker.numerify("sourceFilePath###");
		parameters = new Parameters();
		aFilterValue = faker.numerify("aFilterValue###");
		vFilterValue = faker.numerify("vFilterValue###");

		when(sourceFile.getPath()).thenReturn(sourceFilePath);
		when(aF.toFilter()).thenReturn(aFilter);
		when(vF.toFilter()).thenReturn(vFilter);
		when(aF.getFilterName()).thenReturn(aFilterValue);
		when(vF.getFilterName()).thenReturn(vFilterValue);

		when(aFilter.toString()).thenReturn(aFilterValue);
		when(vFilter.toString()).thenReturn(vFilterValue);
		when(aFilter.getFilterName()).thenReturn(aFilterValue);
		when(vFilter.getFilterName()).thenReturn(vFilterValue);

		vFilterContext = new MediaAnalyserSessionFilterContext(
				"video", vFilterValue, vFilterValue, vF.getClass().getName());
		aFilterContext = new MediaAnalyserSessionFilterContext(
				"audio", aFilterValue, aFilterValue, aF.getClass().getName());

		when(ffmpeg.getInternalParameters()).thenReturn(parameters);

		rawEvent = "RawStdErrFilterEvent(filterName=raw, filterChainPos=0, lineValue=t: 2.80748 a: 12 b: 34)";
		ebur128Result = "Ebur128Summary(integrated=-17.6, integratedThreshold=-28.2, loudnessRange=6.5, loudnessRangeThreshold=-38.2, loudnessRangeLow=-21.6, loudnessRangeHigh=-15.1, samplePeak=-1.4, truePeak=-1.5)";
		ebur128event = "Ebur128StrErrFilterEvent(t=1.80748, target=-23.0, m=-25.5, s=-120.7, i=-19.2, lra=0.0, spk=Stereo[left=-5.5, right=-5.6], ftpk=Stereo[left=-19.1, right=-21.6], tpk=Stereo[left=-5.5, right=-5.6])";

		when(ffmpeg.execute(eq(executableFinder), any())).thenAnswer(invocation -> {
			final Consumer<LineEntry> consumer = invocation.getArgument(1, Consumer.class);
			SYSOUT.lines().forEach(l -> consumer.accept(LineEntry.makeStdOut(l, processLifecycle)));
			return processLifecycle;
		});

		when(processLifecycle.isCorrectlyDone()).thenReturn(true);
		when(processLifecycle.getLauncher()).thenReturn(processlauncher);
		commandLine = faker.numerify("commandLine###");
		when(processlauncher.getFullCommandLine()).thenReturn(commandLine);

		when(mediaAnalyser.createFFmpeg()).thenReturn(ffmpeg);
		when(mediaAnalyser.getExecutableFinder()).thenReturn(executableFinder);
		when(mediaAnalyser.getAudioFilters()).thenReturn(List.of(aF));
		when(mediaAnalyser.getVideoFilters()).thenReturn(List.of(vF));
		s = new MediaAnalyserSession(mediaAnalyser, source, sourceFile);
	}

	@AfterEach
	void ends() {
		assertEquals("", parameters.toString());
		verify(mediaAnalyser, atLeastOnce()).getAudioFilters();
		verify(mediaAnalyser, atLeastOnce()).getVideoFilters();
		verify(aFilter, atLeast(0)).getFilterName();
		verify(vFilter, atLeast(0)).getFilterName();
		verify(aF, atLeast(0)).getFilterName();
		verify(vF, atLeast(0)).getFilterName();

		verifyNoMoreInteractions(
				mediaAnalyser,
				executableFinder,
				processLifecycle,
				processlauncher,
				sourceFile,
				ffmpeg,
				aF,
				vF,
				aFilter,
				vFilter,
				ffprobeResult,
				streamType);
	}

	@Test
	void testNoSources() {
		assertThrows(IllegalArgumentException.class, () -> new MediaAnalyserSession(mediaAnalyser, null, null));
		assertDoesNotThrow(() -> new MediaAnalyserSession(mediaAnalyser, null, sourceFile));
		assertDoesNotThrow(() -> new MediaAnalyserSession(mediaAnalyser, source, null));
	}

	@Test
	void testSetGetFFprobeResult() {
		assertEquals(s, s.setFFprobeResult(null));
		assertTrue(s.getFFprobeResult().isEmpty());

		assertEquals(s, s.setFFprobeResult(ffprobeResult));
		assertEquals(ffprobeResult, s.getFFprobeResult().get());
	}

	@Test
	void testProcess_noFilters() {
		when(mediaAnalyser.getAudioFilters()).thenReturn(List.of());
		when(mediaAnalyser.getVideoFilters()).thenReturn(List.of());
		s = new MediaAnalyserSession(mediaAnalyser, source, sourceFile);
		assertThrows(IllegalArgumentException.class, () -> s.process(emptyLavfiLinesToMerge));
	}

	private void checksProcessBase() {
		verify(mediaAnalyser, times(1)).createFFmpeg();
		verify(mediaAnalyser, times(1)).getExecutableFinder();
		verify(ffmpeg, times(1)).setHidebanner();
		verify(ffmpeg, times(1)).setNostats();
		verify(ffmpeg, times(1)).addSimpleOutputDestination("-", "null");
		verify(ffmpeg, times(1)).fixIOParametredVars(APPEND_PARAM_AT_END, APPEND_PARAM_AT_END);
		verify(ffmpeg, times(1)).execute(eq(executableFinder), any());
		verify(processLifecycle, times(1)).waitForEnd();
		verify(processLifecycle, times(1)).isCorrectlyDone();
	}

	private void checksProcess() {
		checksProcessBase();
		verify(ffmpeg, atLeast(1)).getInternalParameters();
		verify(aF, atLeastOnce()).toFilter();
		verify(vF, atLeastOnce()).toFilter();
	}

	private void checksProcess_NoAF() {
		checksProcessBase();
		verify(ffmpeg, atLeast(1)).getInternalParameters();
		verify(ffmpeg, times(1)).setNoAudio();

	}

	private void checksProcess_NoVF() {
		checksProcessBase();
		verify(ffmpeg, atLeast(1)).getInternalParameters();
		verify(ffmpeg, times(1)).setNoVideo();
	}

	@Test
	void testProcess() {
		final var result = s.process(emptyLavfiLinesToMerge);

		checksProcess();
		verify(ffmpeg, times(1)).addSimpleInputSource(source);
		verify(ffmpeg, times(1)).setLogLevel(WARNING, false, true);

		assertNotNull(result);
		checkMetadatas(result);
		assertEquals(List.of(aFilterContext, vFilterContext), result.filters());
		assertEquals(result.filters(), s.getFilterContextList());
		assertThat(result.r128Target()).isEmpty();

		assertEquals(List.of(
				"-af",
				aFilterValue,
				"-vf",
				vFilterValue),
				parameters.getParameters());
		parameters.clear();

		verify(processLifecycle, atLeastOnce()).getLauncher();
		verify(processlauncher, atLeastOnce()).getFullCommandLine();
		verify(aF, atLeastOnce()).getFilterName();
		verify(aF, atLeastOnce()).toFilter();
		verify(vF, atLeastOnce()).getFilterName();
		verify(vF, atLeastOnce()).toFilter();
	}

	@Test
	void testProcess_notDone() {
		when(processLifecycle.isCorrectlyDone()).thenReturn(false);
		assertThrows(InvalidExecution.class, () -> s.process(emptyLavfiLinesToMerge));

		checksProcess();
		verify(ffmpeg, times(1)).addSimpleInputSource(source);
		verify(ffmpeg, times(1)).setLogLevel(WARNING, false, true);
		verify(processLifecycle, times(1)).getEndStatus();
		verify(processLifecycle, times(1)).getExitCode();
		reset(processLifecycle);

		assertEquals(List.of(
				"-af",
				aFilterValue,
				"-vf",
				vFilterValue),
				parameters.getParameters());
		parameters.clear();

		verify(processlauncher, atLeastOnce()).getFullCommandLine();
	}

	@Test
	void testProcess_FileInput() {
		s = new MediaAnalyserSession(mediaAnalyser, null, sourceFile);
		final var result = s.process(emptyLavfiLinesToMerge);

		checksProcess();
		verify(ffmpeg, times(1)).addSimpleInputSource(sourceFile);
		verify(ffmpeg, times(1)).setLogLevel(WARNING, false, true);

		assertNotNull(result);
		checkMetadatas(result);
		assertEquals(List.of(aFilterContext, vFilterContext), result.filters());
		assertThat(result.r128Target()).isEmpty();

		assertEquals(List.of(
				"-af",
				aFilterValue,
				"-vf",
				vFilterValue),
				parameters.getParameters());
		parameters.clear();

		verify(processLifecycle, atLeastOnce()).getLauncher();
		verify(processlauncher, atLeastOnce()).getFullCommandLine();
	}

	@Test
	void testProcess_setFFprobeResult_noAudioStreams() {
		s.setFFprobeResult(ffprobeResult);

		when(ffprobeResult.getAudioStreams()).thenReturn(Stream.empty());
		assertThrows(IllegalStateException.class, () -> s.process(emptyLavfiLinesToMerge));
		verify(ffprobeResult, times(1)).getAudioStreams();
	}

	@Test
	void testProcess_setFFprobeResult_noVideoStreams() {
		s.setFFprobeResult(ffprobeResult);

		when(ffprobeResult.getAudioStreams()).thenReturn(Stream.of(streamType));
		when(ffprobeResult.getFirstVideoStream()).thenReturn(Optional.empty());
		assertThrows(IllegalStateException.class, () -> s.process(emptyLavfiLinesToMerge));
		verify(ffprobeResult, times(1)).getAudioStreams();
		verify(ffprobeResult, times(1)).getFirstVideoStream();
	}

	@Test
	void testProcess_setFFprobeResult_noAudioStreams_butNoAudioFilters() {
		when(mediaAnalyser.getAudioFilters()).thenReturn(List.of());
		s = new MediaAnalyserSession(mediaAnalyser, source, sourceFile);
		s.setFFprobeResult(ffprobeResult);

		when(ffprobeResult.getAudioStreams()).thenReturn(Stream.empty());
		when(ffprobeResult.getFirstVideoStream()).thenReturn(Optional.ofNullable(streamType));

		final var result = s.process(emptyLavfiLinesToMerge);

		checksProcess_NoAF();
		verify(ffmpeg, times(1)).addSimpleInputSource(source);
		verify(ffmpeg, times(1)).setLogLevel(WARNING, false, true);

		assertNotNull(result);
		checkMetadatas(result);

		assertEquals(List.of(vFilterContext), result.filters());
		assertThat(result.r128Target()).isEmpty();

		assertEquals(List.of(
				"-vf",
				vFilterValue),
				parameters.getParameters());
		parameters.clear();

		verify(ffprobeResult, times(1)).getAudioStreams();
		verify(ffprobeResult, times(1)).getFirstVideoStream();
		verify(processLifecycle, atLeastOnce()).getLauncher();
		verify(processlauncher, atLeastOnce()).getFullCommandLine();
		verify(vF, times(2)).toFilter();
	}

	@Test
	void testProcess_setFFprobeResult_noVideoStreams_butNoVideoFilters() {
		when(mediaAnalyser.getVideoFilters()).thenReturn(List.of());
		s = new MediaAnalyserSession(mediaAnalyser, source, sourceFile);
		s.setFFprobeResult(ffprobeResult);

		when(ffprobeResult.getAudioStreams()).thenReturn(Stream.of(streamType));
		when(ffprobeResult.getFirstVideoStream()).thenReturn(Optional.empty());

		final var result = s.process(emptyLavfiLinesToMerge);

		checksProcess_NoVF();
		verify(ffmpeg, times(1)).addSimpleInputSource(source);
		verify(ffmpeg, times(1)).setLogLevel(WARNING, false, true);

		assertNotNull(result);
		checkMetadatas(result);
		assertThat(result.r128Target()).isEmpty();

		assertEquals(List.of(aFilterContext), result.filters());

		assertEquals(List.of(
				"-af",
				aFilterValue),
				parameters.getParameters());
		parameters.clear();

		verify(ffprobeResult, times(1)).getAudioStreams();
		verify(ffprobeResult, times(1)).getFirstVideoStream();
		verify(processLifecycle, atLeastOnce()).getLauncher();
		verify(processlauncher, atLeastOnce()).getFullCommandLine();
		verify(aF, times(2)).toFilter();
	}

	@Test
	void testProcess_with_LavfiLinesToMerge() {
		final var added = """
				frame:119 pts:4966 pts_time:4.966
				lavfi.siti.si=11.67
				lavfi.siti.ti=5.60
				""".lines();
		final var result = s.process(Optional.ofNullable(() -> added));

		checksProcess();
		verify(ffmpeg, times(1)).addSimpleInputSource(source);
		verify(ffmpeg, times(1)).setLogLevel(WARNING, false, true);

		assertNotNull(result);
		assertThat(result.r128Target()).isEmpty();

		assertEquals(1, result.lavfiMetadatas().getEventCount());
		assertEquals(2, result.lavfiMetadatas().getReportCount());
		assertEquals(List.of(
				new LavfiMtdValue<>(1022, 981168l, 20.441f, 1f)),
				result.lavfiMetadatas().getAPhaseMeterReport());
		assertEquals(List.of(
				new LavfiMtdEvent("mono", null, Duration.ofMillis(18461), Duration.ZERO)),
				result.lavfiMetadatas().getMonoEvents());
		assertEquals(List.of(
				new LavfiMtdValue<>(119, 4966l, 4.966f, new LavfiMtdSiti(11.67f, 5.6f))),
				result.lavfiMetadatas().getSitiReport());

		parameters.clear();
		verify(processLifecycle, atLeastOnce()).getLauncher();
		verify(processlauncher, atLeastOnce()).getFullCommandLine();

	}

	@Test
	void testGetSource() {
		assertEquals(source, s.getSource());
	}

	@Test
	void testGetSourceFile() {
		assertEquals(sourceFile, s.getSourceFile());
	}

	@Test
	void testToString() {
		assertEquals(source, s.toString());
	}

	@Test
	void testToString_File() {
		s = new MediaAnalyserSession(mediaAnalyser, null, sourceFile);
		assertEquals(sourceFilePath, s.toString());
		verify(sourceFile, times(1)).getPath();
	}

	void checkMetadatas(final MediaAnalyserResult result) {
		assertEquals(1, result.lavfiMetadatas().getEventCount());
		assertEquals(1, result.lavfiMetadatas().getReportCount());
		assertEquals(List.of(
				new LavfiMtdValue<>(1022, 981168l, 20.441f, 1f)),
				result.lavfiMetadatas().getAPhaseMeterReport());
		assertEquals(List.of(
				new LavfiMtdEvent("mono", null, Duration.ofMillis(18461), Duration.ZERO)),
				result.lavfiMetadatas().getMonoEvents());
	}

	@Test
	void testImportFromOffline() {
		final var result = MediaAnalyserSession.importFromOffline(SYSOUT.lines(), List.of());

		assertNotNull(result);
		checkMetadatas(result);
		assertTrue(result.filters().isEmpty());
		assertTrue(result.r128Target().isEmpty());
	}

	@Test
	void testImportFromOffline_ebur128() {
		final var target = faker.random().nextInt();
		final var filter = new MediaAnalyserSessionFilterContext(null, null, EBUR128_TARGET + target, null);
		final var result = MediaAnalyserSession.importFromOffline(SYSOUT.lines(), List.of(filter));

		assertNotNull(result);
		checkMetadatas(result);
		assertEquals(List.of(filter), result.filters());
		assertThat(result.r128Target()).contains(target);
	}

	@Test
	void testExtract() {
		final var sysOutList = new ArrayList<String>();
		final var sysErrList = new ArrayList<String>();
		final var cmdLine = s.extract(sysOutList::add, sysErrList::add);
		assertEquals(commandLine, cmdLine);

		checksProcess();
		verify(ffmpeg, times(1)).addSimpleInputSource(source);
		verify(ffmpeg, times(1)).setLogLevel(WARNING, false, true);

		assertEquals(List.of(
				"-af",
				aFilterValue,
				"-vf",
				vFilterValue),
				parameters.getParameters());
		parameters.clear();

		verify(processLifecycle, atLeastOnce()).getLauncher();
		verify(processlauncher, atLeastOnce()).getFullCommandLine();

		assertEquals(SYSOUT.lines().toList(), sysOutList);
	}

	@Test
	void testGetAudioFilters() {
		assertEquals(List.of(aF), s.getAudioFilters());
		verify(aF, times(1)).toFilter();

	}

	@Test
	void testGetVideoFilters() {
		assertEquals(List.of(vF), s.getVideoFilters());
		verify(vF, times(1)).toFilter();

	}

	@Test
	void testGetAudioFilters_withMetadataFilter() {
		when(mediaAnalyser.getAudioFilters())
				.thenReturn(List.of(aF, new AudioFilterAMetadata(Mode.PRINT)));
		s = new MediaAnalyserSession(mediaAnalyser, source, sourceFile);

		assertEquals(List.of(aF), s.getAudioFilters());
		verify(aF, times(1)).toFilter();

	}

	@Test
	void testGetVideoFilters_withMetadataFilter() {
		when(mediaAnalyser.getVideoFilters())
				.thenReturn(List.of(vF, new VideoFilterMetadata(Mode.PRINT)));
		s = new MediaAnalyserSession(mediaAnalyser, source, sourceFile);

		assertEquals(List.of(vF), s.getVideoFilters());
		verify(vF, times(1)).toFilter();

	}

	@Test
	void testProcess_setPgmFFDuration() {
		final var pgmFFDuration = faker.numerify("duration###");
		s.setPgmFFDuration(pgmFFDuration);
		s.process(emptyLavfiLinesToMerge);

		checksProcess();
		verify(ffmpeg, times(1)).addSimpleInputSource(source);
		verify(ffmpeg, times(1)).addDuration(pgmFFDuration);
		verify(ffmpeg, times(1)).setLogLevel(WARNING, false, true);
		verify(processLifecycle, atLeastOnce()).getLauncher();
		verify(processlauncher, atLeastOnce()).getFullCommandLine();

		parameters.clear();
	}

	@Test
	void testProcess_setStartPosition() {
		final var pgmFFStartTime = faker.numerify("duration###");
		s.setPgmFFStartTime(pgmFFStartTime);
		s.process(emptyLavfiLinesToMerge);

		checksProcess();
		verify(ffmpeg, times(1)).addSimpleInputSource(source);
		verify(ffmpeg, times(1)).addStartPosition(pgmFFStartTime);
		verify(ffmpeg, times(1)).setLogLevel(WARNING, false, true);
		verify(processLifecycle, atLeastOnce()).getLauncher();
		verify(processlauncher, atLeastOnce()).getFullCommandLine();

		parameters.clear();
	}

	@Test
	void testProcessExtractR128Target() {
		final var target = faker.random().nextInt();
		when(ffmpeg.getInternalParameters()).thenReturn(Parameters.of("-af", EBUR128_TARGET + target));
		final var result = s.process(emptyLavfiLinesToMerge);

		checksProcess();
		Mockito.reset(ffmpeg);

		assertThat(result.r128Target()).contains(target);
		parameters.clear();

		verify(processLifecycle, atLeastOnce()).getLauncher();
		verify(processlauncher, atLeastOnce()).getFullCommandLine();
		verify(aF, atLeastOnce()).getFilterName();
		verify(aF, atLeastOnce()).toFilter();
		verify(vF, atLeastOnce()).getFilterName();
		verify(vF, atLeastOnce()).toFilter();
	}

	@Test
	void testExtractEbur128TargetFromAFilterChains() {
		assertThat(extractEbur128TargetFromAFilterChains(new FilterChains(aFilterValue)))
				.isEmpty();

		final var target = faker.random().nextInt();
		assertThat(extractEbur128TargetFromAFilterChains(new FilterChains(EBUR128_TARGET + target)))
				.contains(target);

	}

}
