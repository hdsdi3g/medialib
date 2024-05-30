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

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static org.apache.commons.io.FileUtils.openInputStream;
import static org.apache.commons.io.IOUtils.buffer;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static tv.hd3g.fflauncher.ConversionTool.APPEND_PARAM_AT_END;
import static tv.hd3g.fflauncher.FFprobe.FFPrintFormat.XML;
import static tv.hd3g.fflauncher.enums.ChannelLayout.STEREO;
import static tv.hd3g.fflauncher.ffprobecontainer.FFprobePictType.I;
import static tv.hd3g.fflauncher.ffprobecontainer.FFprobePictType.P;
import static tv.hd3g.fflauncher.recipes.ContainerAnalyserSession.importFromOffline;
import static tv.hd3g.processlauncher.LineEntry.makeStdErr;
import static tv.hd3g.processlauncher.LineEntry.makeStdOut;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import net.datafaker.Faker;
import tv.hd3g.fflauncher.FFprobe;
import tv.hd3g.fflauncher.ffprobecontainer.FFprobeAudioFrameConst;
import tv.hd3g.fflauncher.ffprobecontainer.FFprobePacket;
import tv.hd3g.fflauncher.ffprobecontainer.FFprobeVideoFrame;
import tv.hd3g.fflauncher.ffprobecontainer.FFprobeVideoFrameConst;
import tv.hd3g.fflauncher.progress.FFprobeXMLProgressWatcher;
import tv.hd3g.fflauncher.progress.FFprobeXMLProgressWatcher.ProgressConsumer;
import tv.hd3g.processlauncher.ExecutableToolRunning;
import tv.hd3g.processlauncher.LineEntry;
import tv.hd3g.processlauncher.Processlauncher;
import tv.hd3g.processlauncher.ProcesslauncherLifecycle;
import tv.hd3g.processlauncher.cmdline.ExecutableFinder;

class ContainerAnalyserSessionTest {
	static Faker faker = net.datafaker.Faker.instance();

	/**
	 * yt-dlp -f "bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best" https://www.youtube.com/shorts/QFaejpKvghs
	 * ffprobe -show_frames -print_format xml -show_packets -i 3\ Ways\ to\ Boost\ Your\ FPS\ NOW\!\ \[QFaejpKvghs\].mp4 2> /dev/null > ffprobe-streams-packets.xml
	 * gzip ffprobe-streams-packets.xml
	 */
	private static InputStream openExampleFile() {
		try {
			return new GzipCompressorInputStream(
					buffer(openInputStream(
							new File("src/test/resources/ffprobe-streams-packets.xml.gzip"))));
		} catch (final IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	ContainerAnalyserSession cas;

	@Mock
	ContainerAnalyser containerAnalyser;
	@Mock
	FFprobe ffprobe;
	@Mock
	ExecutableFinder executableFinder;
	@Mock
	ExecutableToolRunning executableToolRunning;
	@Mock
	ProcesslauncherLifecycle processLifecycle;
	@Mock
	Processlauncher processlauncher;
	@Mock
	FFprobeXMLProgressWatcher ffprobeXMLProgressWatcher;
	@Mock
	ProgressConsumer progressConsumer;

	String source;
	String commandLine;
	File sourceFile;
	@Captor
	ArgumentCaptor<Consumer<LineEntry>> lineEntryCaptor;

	@BeforeEach
	void init() throws Exception {
		openMocks(this).close();
		source = faker.numerify("source###");
		sourceFile = new File(source);
		cas = new ContainerAnalyserSession(containerAnalyser, source, sourceFile, ffprobeXMLProgressWatcher);

		when(executableToolRunning.getLifecyle()).thenReturn(processLifecycle);
		when(processLifecycle.getLauncher()).thenReturn(processlauncher);
		commandLine = faker.numerify("commandLine###");
		when(processlauncher.getFullCommandLine()).thenReturn(commandLine);
	}

	@AfterEach
	void end() {
		verifyNoMoreInteractions(
				containerAnalyser,
				ffprobe,
				executableFinder,
				executableToolRunning,
				processLifecycle,
				processlauncher,
				ffprobeXMLProgressWatcher,
				progressConsumer);
	}

	@Test
	void testProcess() {
		when(containerAnalyser.createFFprobe()).thenReturn(ffprobe);
		when(containerAnalyser.getExecutableFinder()).thenReturn(executableFinder);
		when(ffprobe.executeDirectStdout(eq(executableFinder), any())).thenReturn(executableToolRunning);

		final var result = cas.process();
		assertNotNull(result);
		assertEquals(new ContainerAnalyserResult(
				cas, List.of(), List.of(), List.of(), null, null, List.of(), List.of(), commandLine), result);

		verify(ffprobe, times(1)).setHidebanner();
		verify(ffprobe, times(1)).setShowFrames();
		verify(ffprobe, times(1)).setShowPackets();
		verify(ffprobe, times(1)).setPrintFormat(XML);
		verify(ffprobe, times(1)).addSimpleInputSource(source);
		verify(ffprobe, times(1)).fixIOParametredVars(APPEND_PARAM_AT_END, APPEND_PARAM_AT_END);
		verify(ffprobe, times(1)).executeDirectStdout(eq(executableFinder), any());

		verify(containerAnalyser, times(1)).getExecutableFinder();
		verify(containerAnalyser, times(1)).createFFprobe();
		verify(executableToolRunning, times(1)).waitForEndAndCheckExecution();

		verify(executableToolRunning, atLeastOnce()).getLifecyle();
		verify(processLifecycle, atLeastOnce()).getLauncher();
		verify(processlauncher, atLeastOnce()).getFullCommandLine();
	}

	@Test
	void testProcess_sourceFile() {
		cas = new ContainerAnalyserSession(containerAnalyser, null, sourceFile, ffprobeXMLProgressWatcher);
		when(containerAnalyser.createFFprobe()).thenReturn(ffprobe);
		when(containerAnalyser.getExecutableFinder()).thenReturn(executableFinder);
		when(ffprobe.executeDirectStdout(eq(executableFinder), any())).thenReturn(executableToolRunning);

		final var result = cas.process();
		assertNotNull(result);
		assertEquals(new ContainerAnalyserResult(
				cas, List.of(), List.of(), List.of(), null, null, List.of(), List.of(), commandLine), result);

		verify(ffprobe, times(1)).setHidebanner();
		verify(ffprobe, times(1)).setShowFrames();
		verify(ffprobe, times(1)).setShowPackets();
		verify(ffprobe, times(1)).setPrintFormat(XML);
		verify(ffprobe, times(1)).addSimpleInputSource(sourceFile);
		verify(ffprobe, times(1)).fixIOParametredVars(APPEND_PARAM_AT_END, APPEND_PARAM_AT_END);
		verify(ffprobe, times(1)).executeDirectStdout(eq(executableFinder), any());

		verify(containerAnalyser, times(1)).getExecutableFinder();
		verify(containerAnalyser, times(1)).createFFprobe();
		verify(executableToolRunning, times(1)).waitForEndAndCheckExecution();

		verify(executableToolRunning, atLeastOnce()).getLifecyle();
		verify(processLifecycle, atLeastOnce()).getLauncher();
		verify(processlauncher, atLeastOnce()).getFullCommandLine();
	}

	@Test
	void testImportFromOffline() throws IOException {
		try (var ffprobeStdOut = openExampleFile()) {
			final var result = importFromOffline(ffprobeStdOut, commandLine);

			assertEquals(2522, result.audioFrames().size());
			final var audioPktSizeStats = result.audioFrames().stream()
					.mapToInt(f -> f.frame().pktSize())
					.summaryStatistics();
			assertEquals(576, audioPktSizeStats.getMax());
			assertEquals(6, audioPktSizeStats.getMin());
			assertEquals(372, Math.round(audioPktSizeStats.getAverage()));

			assertEquals(1753, result.videoFrames().size());
			final var vStats = result.videoFrames().stream()
					.collect(groupingBy(FFprobeVideoFrame::pictType, counting()));
			assertEquals(20, vStats.get(I));
			assertEquals(1733, vStats.get(P));

			assertEquals(4275, result.packets().size());
			assertEquals(result.packets().size(), result.audioFrames().size() + result.videoFrames().size());

			final var fileSize = 85014050l;
			final var payloadSize = result.packets().stream().mapToLong(FFprobePacket::size).sum();
			final var containerOverloadSize = fileSize - payloadSize;
			assertEquals(51037, containerOverloadSize);

			assertEquals(
					new FFprobeVideoFrameConst(
							result.videoFrames().get(0),
							2160, 3840, "yuv420p", "1:1", false, false,
							"tv", "bt709", "bt709", "bt709"),
					result.videoConst());
			assertEquals(
					new FFprobeAudioFrameConst(result.audioFrames().get(0), "fltp", 2, STEREO),
					result.audioConst());

			assertEquals(List.of(), result.olderAudioConsts());
			assertEquals(List.of(), result.olderVideoConsts());
			assertNull(result.session());
		}
	}

	@Test
	void testProcess_NoSourceFile() {
		assertThrows(IllegalArgumentException.class,
				() -> new ContainerAnalyserSession(containerAnalyser, null, null, ffprobeXMLProgressWatcher));
	}

	@Test
	void testExtract() {
		when(containerAnalyser.createFFprobe()).thenReturn(ffprobe);
		when(containerAnalyser.getExecutableFinder()).thenReturn(executableFinder);
		when(ffprobe.execute(eq(executableFinder), any())).thenReturn(processLifecycle);
		when(processLifecycle.isCorrectlyDone()).thenReturn(true);
		when(ffprobeXMLProgressWatcher.createProgress(cas)).thenReturn(progressConsumer);

		final var sysOutList = new ArrayList<String>();
		final var cmdLine = cas.extract(sysOutList::add);
		assertEquals(commandLine, cmdLine);

		verify(ffprobe, times(1)).setHidebanner();
		verify(ffprobe, times(1)).setShowFrames();
		verify(ffprobe, times(1)).setShowPackets();
		verify(ffprobe, times(1)).setPrintFormat(XML);
		verify(ffprobe, times(1)).addSimpleInputSource(source);
		verify(ffprobe, times(1)).fixIOParametredVars(APPEND_PARAM_AT_END, APPEND_PARAM_AT_END);
		verify(ffprobe, times(1)).execute(eq(executableFinder), lineEntryCaptor.capture());

		lineEntryCaptor.getValue().accept(makeStdErr(faker.numerify("stderr###"), processLifecycle));

		final var lineOut = faker.numerify("stdout###");
		lineEntryCaptor.getValue().accept(makeStdOut(lineOut, processLifecycle));

		assertEquals(List.of(lineOut), sysOutList);

		verify(containerAnalyser, times(1)).getExecutableFinder();
		verify(containerAnalyser, times(1)).createFFprobe();

		verify(processLifecycle, atLeastOnce()).getLauncher();
		verify(processlauncher, atLeastOnce()).getFullCommandLine();
		verify(processLifecycle, atLeastOnce()).isCorrectlyDone();
		verify(processLifecycle, atLeastOnce()).waitForEnd();

		verify(ffprobeXMLProgressWatcher, times(1)).createProgress(cas);
		verify(progressConsumer, times(1)).accept(lineOut);
		verify(progressConsumer, times(1)).waitForEnd();
	}

}
