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
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static tv.hd3g.fflauncher.ConversionTool.APPEND_PARAM_AT_END;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.ffmpeg.ffprobe.StreamType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import net.datafaker.Faker;
import tv.hd3g.fflauncher.FFmpeg;
import tv.hd3g.fflauncher.filtering.AudioFilterSupplier;
import tv.hd3g.fflauncher.filtering.Filter;
import tv.hd3g.fflauncher.filtering.VideoFilterSupplier;
import tv.hd3g.fflauncher.resultparser.Ebur128StrErrFilterEvent;
import tv.hd3g.fflauncher.resultparser.RawStdErrFilterEvent;
import tv.hd3g.ffprobejaxb.FFprobeJAXB;
import tv.hd3g.processlauncher.InvalidExecution;
import tv.hd3g.processlauncher.LineEntry;
import tv.hd3g.processlauncher.Processlauncher;
import tv.hd3g.processlauncher.ProcesslauncherLifecycle;
import tv.hd3g.processlauncher.cmdline.ExecutableFinder;
import tv.hd3g.processlauncher.cmdline.Parameters;

class MediaAnalyserSessionTest {
	static Faker faker = instance();

	MediaAnalyserSession s;

	Parameters parameters;
	String source;
	String aFilterValue;
	String vFilterValue;
	String sourceFilePath;
	String lavfiFrame;
	String ebur128Result;
	String ebur128event;
	String rawEvent;

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
	StreamType streamType;
	@Mock
	BiConsumer<MediaAnalyserSession, Ebur128StrErrFilterEvent> ebur128EventConsumer;
	@Mock
	BiConsumer<MediaAnalyserSession, RawStdErrFilterEvent> rawStdErrEventConsumer;
	@Captor
	ArgumentCaptor<Ebur128StrErrFilterEvent> ebur128StrErrFilterEventCaptor;
	@Captor
	ArgumentCaptor<RawStdErrFilterEvent> rawStdErrFilterEventCaptor;

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
		when(aFilter.toString()).thenReturn(aFilterValue);
		when(vFilter.toString()).thenReturn(vFilterValue);

		when(ffmpeg.getInternalParameters()).thenReturn(parameters);

		lavfiFrame = "[LavfiMetadataFilterFrame(frame=1022, pts=981168, ptsTime=20.441, valuesByFilterKeysByFilterName={aphasemeter={phase=1.000000, mono_start=18.461}})]";
		rawEvent = "RawStdErrFilterEvent(filterName=raw, filterChainPos=0, content={t=2.80748, a=12, b=34})";
		ebur128Result = "Ebur128Summary(integrated=-17.6, integratedThreshold=-28.2, loudnessRange=6.5, loudnessRangeThreshold=-38.2, loudnessRangeLow=-21.6, loudnessRangeHigh=-15.1, samplePeak=-1.4, truePeak=-1.5)";
		ebur128event = "Ebur128StrErrFilterEvent(t=1.80748, target=-23.0, m=-25.5, s=-120.7, i=-19.2, lra=0.0, spk=Stereo[left=-5.5, right=-5.6], ftpk=Stereo[left=-19.1, right=-21.6], tpk=Stereo[left=-5.5, right=-5.6])";

		when(ffmpeg.execute(eq(executableFinder), any())).thenAnswer(invocation -> {
			final Consumer<LineEntry> consumer = invocation.getArgument(1, Consumer.class);
			consumer.accept(LineEntry.makeStdOut("frame:1022 pts:981168 pts_time:20.441", processLifecycle));
			consumer.accept(LineEntry.makeStdOut("lavfi.aphasemeter.phase=1.000000", processLifecycle));
			consumer.accept(LineEntry.makeStdOut("lavfi.aphasemeter.mono_start=18.461", processLifecycle));
			consumer.accept(LineEntry.makeStdErr(
					"[Parsed_ebur128_0 @ 0x0000000000000] t: 1.80748    TARGET:-23 LUFS    M: -25.5 S:-120.7     I: -19.2 LUFS       LRA:   0.0 LU  SPK:  -5.5  -5.6 dBFS  FTPK: -19.1 -21.6 dBFS  TPK:  -5.5  -5.6 dBFS",
					processLifecycle));
			consumer.accept(LineEntry.makeStdErr(
					"[Parsed_raw_0 @ 0x0000000000000] t: 2.80748 a: 12 b: 34",
					processLifecycle));
			consumer.accept(LineEntry.makeStdErr("[Parsed_ebur128_0 @ 0x55c6a78b3c80] Summary:", processLifecycle));
			consumer.accept(LineEntry.makeStdErr("Integrated loudness:", processLifecycle));
			consumer.accept(LineEntry.makeStdErr("  I:         -17.6 LUFS", processLifecycle));
			consumer.accept(LineEntry.makeStdErr("  Threshold: -28.2 LUFS", processLifecycle));
			consumer.accept(LineEntry.makeStdErr("", processLifecycle));
			consumer.accept(LineEntry.makeStdErr("Loudness range:", processLifecycle));
			consumer.accept(LineEntry.makeStdErr("  LRA:         6.5 LU", processLifecycle));
			consumer.accept(LineEntry.makeStdErr("  Threshold: -38.2 LUFS", processLifecycle));
			consumer.accept(LineEntry.makeStdErr("  LRA low:   -21.6 LUFS", processLifecycle));
			consumer.accept(LineEntry.makeStdErr("  LRA high:  -15.1 LUFS", processLifecycle));
			consumer.accept(LineEntry.makeStdErr("", processLifecycle));
			consumer.accept(LineEntry.makeStdErr("Sample peak:", processLifecycle));
			consumer.accept(LineEntry.makeStdErr("  Peak:       -1.4 dBFS", processLifecycle));
			consumer.accept(LineEntry.makeStdErr("", processLifecycle));
			consumer.accept(LineEntry.makeStdErr("True peak:", processLifecycle));
			consumer.accept(LineEntry.makeStdErr("  Peak:       -1.5 dBFS", processLifecycle));
			return processLifecycle;
		});

		when(processLifecycle.isCorrectlyDone()).thenReturn(true);
		when(processLifecycle.getLauncher()).thenReturn(processlauncher);
		when(processlauncher.getFullCommandLine()).thenReturn(faker.numerify("commandLine###"));

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
				streamType,
				ebur128EventConsumer,
				rawStdErrEventConsumer);
	}

	@Test
	void testNoSources() {
		assertThrows(IllegalArgumentException.class, () -> new MediaAnalyserSession(mediaAnalyser, null, null));
		assertDoesNotThrow(() -> new MediaAnalyserSession(mediaAnalyser, null, sourceFile));
		assertDoesNotThrow(() -> new MediaAnalyserSession(mediaAnalyser, source, null));
	}

	@Test
	void testSetFFprobeResult() {
		assertEquals(s, s.setFFprobeResult(null));
	}

	@Test
	void testProcess_noFilters() {
		when(mediaAnalyser.getAudioFilters()).thenReturn(List.of());
		when(mediaAnalyser.getVideoFilters()).thenReturn(List.of());
		s = new MediaAnalyserSession(mediaAnalyser, source, sourceFile);
		assertThrows(IllegalArgumentException.class, () -> s.process());
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
		verify(ffmpeg, times(2)).getInternalParameters();
		verify(aF, times(1)).toFilter();
		verify(vF, times(1)).toFilter();
	}

	private void checksProcess_NoAF() {
		checksProcessBase();
		verify(ffmpeg, times(1)).getInternalParameters();
		verify(ffmpeg, times(1)).setNoAudio();
		verify(vF, times(1)).toFilter();
	}

	private void checksProcess_NoVF() {
		checksProcessBase();
		verify(ffmpeg, times(1)).getInternalParameters();
		verify(ffmpeg, times(1)).setNoVideo();
		verify(aF, times(1)).toFilter();
	}

	@Test
	void testProcess() {
		s.setEbur128EventConsumer(ebur128EventConsumer);
		s.setRawStdErrEventConsumer(rawStdErrEventConsumer);
		final var result = s.process();

		checksProcess();
		verify(ffmpeg, times(1)).addSimpleInputSource(source);

		assertNotNull(result);
		assertEquals(ebur128Result, result.ebur128Summary().toString());
		assertEquals(lavfiFrame, result.lavfiMetadatas().toString());
		assertEquals(s, result.session());

		assertEquals(List.of(
				"-af",
				aFilterValue + ",ametadata=mode=print:file=-",
				"-vf",
				vFilterValue + ",metadata=mode=print:file=-"),
				parameters.getParameters());
		parameters.clear();

		verify(ebur128EventConsumer, times(1)).accept(eq(s), ebur128StrErrFilterEventCaptor.capture());
		assertEquals(ebur128event, ebur128StrErrFilterEventCaptor.getValue().toString());
		verify(rawStdErrEventConsumer, times(1)).accept(eq(s), rawStdErrFilterEventCaptor.capture());
		assertEquals(rawEvent, rawStdErrFilterEventCaptor.getValue().toString());

		verify(processLifecycle, atLeastOnce()).getLauncher();
		verify(processlauncher, atLeastOnce()).getFullCommandLine();
	}

	@Test
	void testProcess_notDone() {
		when(processLifecycle.isCorrectlyDone()).thenReturn(false);
		assertThrows(InvalidExecution.class, () -> s.process());

		checksProcess();
		verify(ffmpeg, times(1)).addSimpleInputSource(source);
		verify(processLifecycle, times(1)).getEndStatus();
		verify(processLifecycle, times(1)).getExitCode();
		reset(processLifecycle);

		assertEquals(List.of(
				"-af",
				aFilterValue + ",ametadata=mode=print:file=-",
				"-vf",
				vFilterValue + ",metadata=mode=print:file=-"),
				parameters.getParameters());
		parameters.clear();

		verify(processlauncher, atLeastOnce()).getFullCommandLine();
	}

	@Test
	void testProcess_FileInput() {
		s = new MediaAnalyserSession(mediaAnalyser, null, sourceFile);
		final var result = s.process();

		checksProcess();
		verify(ffmpeg, times(1)).addSimpleInputSource(sourceFile);

		assertNotNull(result);
		assertEquals(ebur128Result, result.ebur128Summary().toString());
		assertEquals(lavfiFrame, result.lavfiMetadatas().toString());
		assertEquals(s, result.session());

		assertEquals(List.of(
				"-af",
				aFilterValue + ",ametadata=mode=print:file=-",
				"-vf",
				vFilterValue + ",metadata=mode=print:file=-"),
				parameters.getParameters());
		parameters.clear();

		verify(processLifecycle, atLeastOnce()).getLauncher();
		verify(processlauncher, atLeastOnce()).getFullCommandLine();
	}

	@Test
	void testProcess_setFFprobeResult_noAudioStreams() {
		s.setFFprobeResult(ffprobeResult);

		when(ffprobeResult.getAudiosStreams()).thenReturn(Stream.empty());
		assertThrows(IllegalStateException.class, () -> s.process());
		verify(ffprobeResult, times(1)).getAudiosStreams();
	}

	@Test
	void testProcess_setFFprobeResult_noVideoStreams() {
		s.setFFprobeResult(ffprobeResult);

		when(ffprobeResult.getAudiosStreams()).thenReturn(Stream.of(streamType));
		when(ffprobeResult.getFirstVideoStream()).thenReturn(Optional.empty());
		assertThrows(IllegalStateException.class, () -> s.process());
		verify(ffprobeResult, times(1)).getAudiosStreams();
		verify(ffprobeResult, times(1)).getFirstVideoStream();
	}

	@Test
	void testProcess_setFFprobeResult_noAudioStreams_butNoAudioFilters() {
		when(mediaAnalyser.getAudioFilters()).thenReturn(List.of());
		s = new MediaAnalyserSession(mediaAnalyser, source, sourceFile);
		s.setFFprobeResult(ffprobeResult);

		when(ffprobeResult.getAudiosStreams()).thenReturn(Stream.empty());
		when(ffprobeResult.getFirstVideoStream()).thenReturn(Optional.ofNullable(streamType));

		final var result = s.process();

		checksProcess_NoAF();
		verify(ffmpeg, times(1)).addSimpleInputSource(source);

		assertNotNull(result);
		assertEquals(ebur128Result, result.ebur128Summary().toString());
		assertEquals(lavfiFrame, result.lavfiMetadatas().toString());
		assertEquals(s, result.session());

		assertEquals(List.of(
				"-vf",
				vFilterValue + ",metadata=mode=print:file=-"),
				parameters.getParameters());
		parameters.clear();

		verify(ffprobeResult, times(1)).getAudiosStreams();
		verify(ffprobeResult, times(1)).getFirstVideoStream();
		verify(processLifecycle, atLeastOnce()).getLauncher();
		verify(processlauncher, atLeastOnce()).getFullCommandLine();
	}

	@Test
	void testProcess_setFFprobeResult_noVideoStreams_butNoVideoFilters() {
		when(mediaAnalyser.getVideoFilters()).thenReturn(List.of());
		s = new MediaAnalyserSession(mediaAnalyser, source, sourceFile);
		s.setFFprobeResult(ffprobeResult);

		when(ffprobeResult.getAudiosStreams()).thenReturn(Stream.of(streamType));
		when(ffprobeResult.getFirstVideoStream()).thenReturn(Optional.empty());

		final var result = s.process();

		checksProcess_NoVF();
		verify(ffmpeg, times(1)).addSimpleInputSource(source);

		assertNotNull(result);
		assertEquals(ebur128Result, result.ebur128Summary().toString());
		assertEquals(lavfiFrame, result.lavfiMetadatas().toString());
		assertEquals(s, result.session());

		assertEquals(List.of(
				"-af",
				aFilterValue + ",ametadata=mode=print:file=-"),
				parameters.getParameters());
		parameters.clear();

		verify(ffprobeResult, times(1)).getAudiosStreams();
		verify(ffprobeResult, times(1)).getFirstVideoStream();
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

}
