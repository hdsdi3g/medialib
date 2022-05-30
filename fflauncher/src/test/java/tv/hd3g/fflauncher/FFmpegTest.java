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

import static java.util.stream.Collectors.toUnmodifiableList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tv.hd3g.fflauncher.ConversionTool.APPEND_PARAM_AT_END;
import static tv.hd3g.fflauncher.ConversionTool.PREPEND_PARAM_AT_START;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import tv.hd3g.fflauncher.FFmpeg.FFHardwareCodec;
import tv.hd3g.fflauncher.FFmpeg.Preset;
import tv.hd3g.fflauncher.FFmpeg.Tune;
import tv.hd3g.fflauncher.enums.FFUnit;
import tv.hd3g.fflauncher.recipes.ProbeMedia;
import tv.hd3g.processlauncher.cmdline.ExecutableFinder;
import tv.hd3g.processlauncher.cmdline.Parameters;

class FFmpegTest {

	final ExecutableFinder executableFinder;
	final ScheduledExecutorService maxExecTimeScheduler;
	final ProbeMedia probeMedia;

	FFmpegTest() {
		executableFinder = new ExecutableFinder();
		maxExecTimeScheduler = Executors.newSingleThreadScheduledExecutor();
		probeMedia = new ProbeMedia(executableFinder, maxExecTimeScheduler);
	}

	private FFmpeg create() {
		return new FFmpeg("ffmpeg", new Parameters());
	}

	@Test
	void testSimpleOutputDestination() {
		final var ffmpeg = create();
		ffmpeg.addSimpleOutputDestination("dest", "container");
		ffmpeg.fixIOParametredVars(PREPEND_PARAM_AT_START, APPEND_PARAM_AT_END);

		assertTrue(ffmpeg.getReadyToRunParameters().toString().endsWith("-f container dest"));
	}

	@Test
	void testParameters() throws FileNotFoundException {
		final var ffmpeg = create();
		final var header = ffmpeg.getInternalParameters().toString().length();

		ffmpeg.addPreset(Preset.PLACEBO).addTune(Tune.SSIM).addBitrate(123, FFUnit.GIGA, 1);
		ffmpeg.addBitrateControl(10, 20, 30, FFUnit.MEGA).addCRF(40).addVideoCodecName("NoPe", 2);
		ffmpeg.addGOPControl(50, 60, 70).addIBQfactor(1.5f, 2.5f).addQMinMax(80, 90);
		ffmpeg.addBitrate(100, FFUnit.MEGA, -1).addVideoCodecName("NoPe2", -1);

		assertEquals(
		        "-preset placebo -tune ssim -b:v:1 123G -minrate 10M -maxrate 20M -bufsize 30M -crf 40 -c:v:2 NoPe -bf 50 -g 60 -ref 70 -i_qfactor 1.5 -b_qfactor 2.5 -qmin 80 -qmax 90 -b:v 100M -c:v NoPe2",
		        ffmpeg.getInternalParameters().toString().substring(header));
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

		// this is buggy... assertTrue("NV Toolkit is not avaliable: " + about.getAvailableHWAccelerationMethods(), about.isNVToolkitIsAvaliable());

		final var cmd = ffmpeg.getInternalParameters();
		cmd.addBulkParameters("-f lavfi -i smptehdbars=duration=" + 5 + ":size=1280x720:rate=25");

		ffmpeg.addHardwareVideoEncoding("h264", -1, FFHardwareCodec.NV, about).addCRF(0);
		assertTrue(cmd.getValues("-c:v").stream()
		        .findFirst()
		        .orElseThrow(() -> new IllegalArgumentException("No codecs was added: " + cmd)).contains("nvenc"));

		final var test_file = File.createTempFile("smptebars", ".mkv");
		ffmpeg.addSimpleOutputDestination(test_file.getPath());

		System.out.println("Generate test file to \"" + test_file.getPath() + "\"");

		ffmpeg.execute(executableFinder).checkExecutionGetText();

		assertTrue(test_file.exists());

		ffmpeg = create();
		ffmpeg.setOverwriteOutputFiles();
		ffmpeg.setOnErrorDeleteOutFiles(true);

		ffmpeg.addHardwareVideoDecoding(test_file.getPath(), probeMedia.doAnalysing(test_file.getPath()),
		        FFHardwareCodec.NV, about);
		ffmpeg.addHardwareVideoEncoding("h264", -1, FFHardwareCodec.NV, about).addCRF(40);

		final var test_file2 = File.createTempFile("smptebars", ".mkv");
		ffmpeg.addSimpleOutputDestination(test_file2.getPath());

		System.out.println("Hardware decoding to \"" + test_file.getPath() + "\"");
		ffmpeg.execute(executableFinder).checkExecutionGetText();

		assertTrue(test_file2.exists());
		assertTrue(test_file.delete());
		assertTrue(test_file2.delete());
	}

	@Test
	void testGetFirstVideoStream() throws IOException, InterruptedException, ExecutionException, MediaException {
		if (System.getProperty("ffmpeg.test.nvidia", "").equals("1") == false) {
			return;
		}

		final var ffmpeg = create();
		ffmpeg.setOverwriteOutputFiles();
		ffmpeg.setOnErrorDeleteOutFiles(true);

		final var cmd = ffmpeg.getInternalParameters();
		cmd.addBulkParameters("-f lavfi -i smptehdbars=duration=" + 5 + ":size=1280x720:rate=25");
		ffmpeg.addVideoCodecName("ffv1", -1);

		final var test_file = File.createTempFile("smptebars", ".mkv");
		ffmpeg.addSimpleOutputDestination(test_file.getPath());

		System.out.println("Generate test file to \"" + test_file.getPath() + "\"");
		ffmpeg.execute(executableFinder).checkExecutionGetText();

		assertTrue(test_file.exists());

		final var s = FFmpeg.getFirstVideoStream(probeMedia.doAnalysing(test_file.getPath())).get();
		assertEquals("ffv1", s.getCodecName());
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
		        .collect(toUnmodifiableList());

		ffmpeg.addSimpleInputSource(files.get(0));
		ffmpeg.addSimpleInputSource(files.get(1).getPath());
		ffmpeg.addSimpleInputSource(files.get(2), List.of());
		ffmpeg.addSimpleInputSource(files.get(3).getPath(), List.of());
		ffmpeg.addSimpleOutputDestination(files.get(4));
		ffmpeg.addSimpleOutputDestination(files.get(5).getPath());
		ffmpeg.addSimpleOutputDestination(files.get(6), "-opts6");
		ffmpeg.addSimpleOutputDestination(files.get(7).getPath(), "-opts7");
		ffmpeg.fixIOParametredVars();

		final var p = ffmpeg.getReadyToRunParameters();
		final var filesNames = files.stream().map(File::getPath).collect(Collectors.toUnmodifiableList());

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

}
