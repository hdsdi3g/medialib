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
package tv.hd3g.fflauncher.recipes;

import static tv.hd3g.fflauncher.ConversionTool.APPEND_PARAM_AT_END;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

import tv.hd3g.fflauncher.FFmpeg;
import tv.hd3g.processlauncher.cmdline.ExecutableFinder;
import tv.hd3g.processlauncher.cmdline.Parameters;

public class GenerateVideoFile {

	private final String execName;
	private final ExecutableFinder executableFinder;

	public GenerateVideoFile(final ExecutableFinder executableFinder) {
		this("ffmpeg", executableFinder);
	}

	public GenerateVideoFile(final String execName, final ExecutableFinder executableFinder) {
		this.execName = Objects.requireNonNull(execName);
		this.executableFinder = Objects.requireNonNull(executableFinder);
	}

	/**
	 * Stateless
	 */
	private FFmpeg internal(final int duration_in_sec, final Point resolution) throws IOException {
		final var parameters = new Parameters();
		final var ffmpeg = new FFmpeg(execName, parameters);

		final var about = ffmpeg.getAbout(executableFinder);
		if (about.isFromFormatIsAvaliable("lavfi") == false) {
			final var exec = executableFinder.get(ffmpeg.getExecutableName());
			throw new IOException("This ffmpeg (" + exec + ") can't handle \"lavfi\"");
		}

		ffmpeg.setOverwriteOutputFiles();
		ffmpeg.setOnErrorDeleteOutFiles(true);
		ffmpeg.setFilterForLinesEventsToDisplay(l -> (l.isStdErr() == false || l.isStdErr() && ffmpeg
		        .filterOutErrorLines().test(l.getLine())));

		parameters.addBulkParameters("-f lavfi -i smptehdbars=duration=" + duration_in_sec + ":size=" + resolution.x
		                             + "x" + resolution.y + ":rate=25");
		parameters.addBulkParameters("-f lavfi -i sine=frequency=1000:sample_rate=48000:duration=" + duration_in_sec);

		if (about.isCoderIsAvaliable("h264")) {
			ffmpeg.addVideoCodecName("h264", -1).addCRF(1);
		} else {
			ffmpeg.addVideoCodecName("ffv1", -1);
		}

		if (about.isCoderIsAvaliable("aac")) {
			ffmpeg.addAudioCodecName("aac", -1);
		} else {
			ffmpeg.addAudioCodecName("opus", -1);
		}

		return ffmpeg;
	}

	/**
	 * Stateless
	 */
	public FFmpeg generateBarsAnd1k(final String destination,
	                                final int duration_in_sec,
	                                final Point resolution) throws IOException {
		final var ffmpeg = internal(duration_in_sec, resolution);
		ffmpeg.addSimpleOutputDestination(destination);
		ffmpeg.fixIOParametredVars(APPEND_PARAM_AT_END, APPEND_PARAM_AT_END);
		ffmpeg.execute(executableFinder).waitForEndAndCheckExecution();
		return ffmpeg;
	}

	/**
	 * Stateless
	 */
	public FFmpeg generateBarsAnd1k(final File destination,
	                                final int duration_in_sec,
	                                final Point resolution) throws IOException {
		final var ffmpeg = internal(duration_in_sec, resolution);
		ffmpeg.addSimpleOutputDestination(destination);
		ffmpeg.fixIOParametredVars(APPEND_PARAM_AT_END, APPEND_PARAM_AT_END);
		ffmpeg.execute(executableFinder).waitForEndAndCheckExecution();
		return ffmpeg;
	}

}
