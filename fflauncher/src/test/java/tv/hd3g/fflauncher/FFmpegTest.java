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
 * Copyright (C) hdsdi3g for hd3g.tv 2018
 *
 */
package tv.hd3g.fflauncher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static tv.hd3g.fflauncher.ConversionTool.APPEND_PARAM_AT_END;
import static tv.hd3g.fflauncher.ConversionTool.PREPEND_PARAM_AT_START;
import static tv.hd3g.fflauncher.FFbase.filterOutErrorLines;
import static tv.hd3g.fflauncher.processingtool.FFmpegToolBuilder.statsPeriod;
import static tv.hd3g.processlauncher.CapturedStreams.BOTH_STDOUT_STDERR;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import net.datafaker.Faker;
import tv.hd3g.fflauncher.enums.FFHardwareCodec;
import tv.hd3g.fflauncher.enums.FFUnit;
import tv.hd3g.fflauncher.enums.Preset;
import tv.hd3g.fflauncher.enums.Tune;
import tv.hd3g.fflauncher.progress.ProgressCallback;
import tv.hd3g.fflauncher.progress.ProgressListener;
import tv.hd3g.fflauncher.progress.ProgressListenerSession;
import tv.hd3g.fflauncher.recipes.ProbeMedia;
import tv.hd3g.processlauncher.CapturedStdOutErrTextRetention;
import tv.hd3g.processlauncher.ExecutionCallbacker;
import tv.hd3g.processlauncher.InvalidExecution;
import tv.hd3g.processlauncher.ProcessLifeCycleException;
import tv.hd3g.processlauncher.ProcesslauncherBuilder;
import tv.hd3g.processlauncher.cmdline.CommandLine;
import tv.hd3g.processlauncher.cmdline.ExecutableFinder;

class FFmpegTest {
	private static final String FFMPEG_EXEC_NAME = "ffmpeg";

	static Faker faker = net.datafaker.Faker.instance();

	final ExecutableFinder executableFinder;
	final ScheduledExecutorService maxExecTimeScheduler;
	final ProbeMedia probeMedia;

	FFmpegTest() {
		executableFinder = new ExecutableFinder();
		maxExecTimeScheduler = Executors.newSingleThreadScheduledExecutor();
		probeMedia = new ProbeMedia(maxExecTimeScheduler);
		probeMedia.setExecutableFinder(executableFinder);
	}

	private FFmpeg create() {
		return new FFmpeg(FFMPEG_EXEC_NAME);
	}

	@Test
	void testSimpleOutputDestination() {
		final var ffmpeg = create();
		ffmpeg.addSimpleOutputDestination("dest", "container");
		ffmpeg.setFixIOParametredVars(PREPEND_PARAM_AT_START, APPEND_PARAM_AT_END);

		assertTrue(ffmpeg.getReadyToRunParameters().toString().endsWith("-f container dest"));
	}

	@Test
	void testParameters() {
		final var ffmpeg = create();
		final var header = ffmpeg.getInternalParameters().toString().length();

		ffmpeg.addPreset(Preset.PLACEBO);
		ffmpeg.addTune(Tune.SSIM);
		ffmpeg.addBitrate(123, FFUnit.GIGA, 1);
		ffmpeg.addBitrateControl(10, 20, 30, FFUnit.MEGA);
		ffmpeg.addCRF(40);
		ffmpeg.addVideoCodecName("NoPe", 2);
		ffmpeg.addGOPControl(50, 60, 70);
		ffmpeg.addIBQfactor(1.5f, 2.5f);
		ffmpeg.addQMinMax(80, 90);
		ffmpeg.addBitrate(100, FFUnit.MEGA, -1);
		ffmpeg.addVideoCodecName("NoPe2", -1);

		assertEquals(
				"-preset placebo -tune ssim -b:v:1 123G -minrate 10M -maxrate 20M -bufsize 30M -crf 40 -c:v:2 NoPe -bf 50 -g 60 -ref 70 -i_qfactor 1.5 -b_qfactor 2.5 -qmin 80 -qmax 90 -b:v 100M -c:v NoPe2",
				ffmpeg.getInternalParameters().toString().substring(header));
	}

	private void execute(final FFmpeg ffmpeg, final ExecutableFinder executableFinder) {
		final var executableName = ffmpeg.getExecutableName();
		try {
			final var cmd = new CommandLine(executableName, ffmpeg.getReadyToRunParameters(), executableFinder);
			final var builder = new ProcesslauncherBuilder(cmd);

			final var textRetention = new CapturedStdOutErrTextRetention();
			builder.getSetCaptureStandardOutputAsOutputText(BOTH_STDOUT_STDERR).addObserver(textRetention);

			ffmpeg.makeConversionHooks().beforeRun(builder);
			final var lifeCycle = builder.start();

			try {
				lifeCycle.checkExecution();
				textRetention.waitForClosedStreams();
			} catch (final InvalidExecution e) {
				throw e.injectStdErr(textRetention.getStderrLines(false)
						.filter(filterOutErrorLines)
						.map(String::trim).collect(Collectors.joining("|")));
			}
		} catch (final IOException e) {
			throw new ProcessLifeCycleException("Can't start " + executableName, e);
		}
	}

	@Test
	void testNV() throws IOException, MediaException {
		if (System.getProperty("ffmpeg.test.nvidia", "").equals("1") == false) {
			return;
		}

		var ffmpeg = create();
		ffmpeg.setOverwriteOutputFiles();
		ffmpeg.setOnErrorDeleteOutFiles(true);

		final var about = ffmpeg.getAbout(new ExecutableFinder());
		final var cmd = ffmpeg.getInternalParameters();
		cmd.addBulkParameters("-f lavfi -i smptehdbars=duration=" + 5 + ":size=1280x720:rate=25");

		ffmpeg.addHardwareVideoEncoding("h264", -1, FFHardwareCodec.NV, about);
		ffmpeg.addCRF(0);
		assertTrue(cmd.getValues("-c:v").stream()
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("No codecs was added: " + cmd)).contains("nvenc"));

		final var testFile = File.createTempFile("smptebars", ".mkv");
		ffmpeg.addSimpleOutputDestination(testFile.getPath());

		System.out.println("Generate test file to \"" + testFile.getPath() + "\"");

		execute(ffmpeg, executableFinder);

		assertTrue(testFile.exists());

		ffmpeg = create();
		ffmpeg.setOverwriteOutputFiles();
		ffmpeg.setOnErrorDeleteOutFiles(true);

		ffmpeg.addHardwareVideoDecoding(testFile.getPath(), probeMedia.process(testFile.getPath()).getResult(),
				FFHardwareCodec.NV, about);
		ffmpeg.addHardwareVideoEncoding("h264", -1, FFHardwareCodec.NV, about);
		ffmpeg.addCRF(40);

		final var testFile2 = File.createTempFile("smptebars", ".mkv");
		ffmpeg.addSimpleOutputDestination(testFile2.getPath());

		System.out.println("Hardware decoding to \"" + testFile.getPath() + "\"");
		execute(ffmpeg, executableFinder);

		assertTrue(testFile2.exists());
		assertTrue(testFile.delete());
		assertTrue(testFile2.delete());
	}

	@Test
	void testGetFirstVideoStream() throws IOException {
		if (System.getProperty("ffmpeg.test.nvidia", "").equals("1") == false) {
			return;
		}

		final var ffmpeg = create();
		ffmpeg.setOverwriteOutputFiles();
		ffmpeg.setOnErrorDeleteOutFiles(true);

		final var cmd = ffmpeg.getInternalParameters();
		cmd.addBulkParameters("-f lavfi -i smptehdbars=duration=" + 5 + ":size=1280x720:rate=25");
		ffmpeg.addVideoCodecName("ffv1", -1);

		final var testFile = File.createTempFile("smptebars", ".mkv");
		ffmpeg.addSimpleOutputDestination(testFile.getPath());

		System.out.println("Generate test file to \"" + testFile.getPath() + "\"");
		execute(ffmpeg, executableFinder);

		assertTrue(testFile.exists());

		final var s = probeMedia.process(testFile.getPath()).getResult().getFirstVideoStream().get();
		assertEquals("ffv1", s.codecName());
	}

	@Test
	void testSpacesInInputOutputFileNames() {
		final var ffmpeg = create();

		final var files = IntStream.range(0, 8)
				.mapToObj(i -> {
					try {
						return File.createTempFile("temp FF name [" + i + "]", ".ext");
					} catch (final IOException e) {
						throw new UncheckedIOException(e);
					}
				})
				.toList();

		ffmpeg.addSimpleInputSource(files.get(0));
		ffmpeg.addSimpleInputSource(files.get(1).getPath());
		ffmpeg.addSimpleInputSource(files.get(2), List.of());
		ffmpeg.addSimpleInputSource(files.get(3).getPath(), List.of());
		ffmpeg.addSimpleOutputDestination(files.get(4));
		ffmpeg.addSimpleOutputDestination(files.get(5).getPath());
		ffmpeg.addSimpleOutputDestination(files.get(6), "-opts6");
		ffmpeg.addSimpleOutputDestination(files.get(7).getPath(), "-opts7");
		ffmpeg.setFixIOParametredVars(PREPEND_PARAM_AT_START, APPEND_PARAM_AT_END);

		final var p = ffmpeg.getReadyToRunParameters();
		final var filesNames = files.stream().map(File::getPath).toList();

		final var params = new ArrayList<>();
		for (var pos = 0; pos < 4; pos++) {
			params.add("-i");
			params.add(filesNames.get(pos));
		}
		for (var pos = 4; pos < 6; pos++) {
			params.add(filesNames.get(pos));
		}
		for (var pos = 6; pos < 8; pos++) {
			params.add("-f");
			params.add("-opts" + pos);
			params.add(filesNames.get(pos));
		}

		assertEquals(params, p.getParameters());
	}

	@Nested
	class Progress {
		FFmpeg f;
		int port;

		@Mock
		ProgressListener progressListener;
		@Mock
		ProgressCallback progressCallback;
		@Mock
		ProgressListenerSession progressListenerSession;
		@Mock
		ProcesslauncherBuilder builder;
		@Captor
		ArgumentCaptor<ExecutionCallbacker> executionCallbackerCaptor;

		@BeforeEach
		void init() throws Exception {
			openMocks(this).close();
			f = create();
			port = faker.random().nextInt(1, 100_000);

			when(progressListener.createSession(progressCallback, statsPeriod)).thenReturn(progressListenerSession);
			when(progressListenerSession.start()).thenReturn(port);
		}

		@AfterEach
		void end() {
			verifyNoMoreInteractions(progressListener, progressCallback, progressListenerSession, builder);
		}

	}

	@Test
	void testBase() {
		final var b = new FFmpeg(FFMPEG_EXEC_NAME);
		final var about = b.getAbout(executableFinder);

		assertNotNull(about.getVersion(), "version");
		assertFalse(about.getCodecs().isEmpty(), "codecs empty");
		assertFalse(about.getFormats().isEmpty(), "formats empty");
		assertFalse(about.getDevices().isEmpty(), "devices empty");
		assertFalse(about.getBitStreamFilters().isEmpty(), "bitstream empty");
		assertNotNull(about.getProtocols(), "protocols");
		assertFalse(about.getFilters().isEmpty(), "filters empty");
		assertFalse(about.getPixelFormats().isEmpty(), "pixelFormats empty");

		assertTrue(about.isCoderIsAvaliable("ffv1"), "Coder Avaliable");
		assertFalse(about.isCoderIsAvaliable("nonono"), "Coder notAvaliable");
		assertTrue(about.isDecoderIsAvaliable("rl2"), "Decoder Avaliable");
		assertFalse(about.isDecoderIsAvaliable("nonono"), "Decoder notAvaliable");
		assertTrue(about.isFilterIsAvaliable("color"), "Filter Avaliable");
		assertFalse(about.isFilterIsAvaliable("nonono"), "Filter notAvaliable");
		assertTrue(about.isToFormatIsAvaliable("wav"), "Format Avaliable");
		assertFalse(about.isToFormatIsAvaliable("nonono"), "Format notAvaliable");
	}

	@Test
	void testNVPresence() {
		final var b = new FFmpeg(FFMPEG_EXEC_NAME);

		if (System.getProperty("ffmpeg.test.nvidia", "").equals("1")) {
			assertTrue(b.getAbout(executableFinder)
					.isNVToolkitIsAvaliable(), "Can't found NV lib like cuda, cuvid and nvenc");
		}
		if (System.getProperty("ffmpeg.test.libnpp", "").equals("1")) {
			assertTrue(b.getAbout(executableFinder).isHardwareNVScalerFilterIsAvaliable(), "Can't found libnpp");
		}
	}

}
