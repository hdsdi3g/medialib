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

import static tv.hd3g.fflauncher.SimpleSourceTraits.addSineAudioGeneratorAsInputSource;
import static tv.hd3g.fflauncher.SimpleSourceTraits.addSmptehdbarsGeneratorAsInputSource;

import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.UncheckedIOException;
import java.util.Objects;

import tv.hd3g.fflauncher.FFmpeg;
import tv.hd3g.fflauncher.processingtool.FFmpegToolBuilder;
import tv.hd3g.processlauncher.ProcesslauncherLifecycle;
import tv.hd3g.processlauncher.cmdline.ExecutableFinder;
import tv.hd3g.processlauncher.processingtool.CallbackWatcher;

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

	private record Conf(int durationInSec, Point resolution) {
	}

	private class Builder extends FFmpegToolBuilder<Conf, Void, CallbackWatcher> {

		protected Builder(final FFmpeg ffmpeg) {
			super(ffmpeg, new CallbackWatcher());
		}

		@Override
		protected Void compute(final Conf sourceOrigin, final ProcesslauncherLifecycle lifeCycle) {
			return null;
		}

		@Override
		protected FFmpeg getParametersProvider(final Conf sourceOrigin) {
			final var about = ffmpeg.getAbout(executableFinder);
			if (about.isFromFormatIsAvaliable("lavfi") == false) {
				try {
					final var exec = executableFinder.get(execName);
					throw new IllegalArgumentException("This ffmpeg (" + exec + ") can't handle \"lavfi\"");
				} catch (final FileNotFoundException e) {
					throw new UncheckedIOException("Can't found ffmpeg", e);
				}
			}

			ffmpeg.setOverwriteOutputFiles();
			ffmpeg.setOnErrorDeleteOutFiles(true);
			ffmpeg.setFilterForLinesEventsToDisplay(l -> (l.stdErr() == false
														  || l.stdErr()
															 && ffmpeg.filterOutErrorLines().test(l.line())));

			if (about.isCoderIsAvaliable("h264")) {
				ffmpeg.addVideoCodecName("h264", -1);
				ffmpeg.addCRF(1);
			} else {
				ffmpeg.addVideoCodecName("ffv1", -1);
			}

			if (about.isCoderIsAvaliable("aac")) {
				ffmpeg.addAudioCodecName("aac", -1);
			} else {
				ffmpeg.addAudioCodecName("opus", -1);
			}

			addSmptehdbarsGeneratorAsInputSource(ffmpeg, sourceOrigin.resolution, sourceOrigin.durationInSec, "25");
			addSineAudioGeneratorAsInputSource(ffmpeg, 1000, sourceOrigin.durationInSec, 48000);

			return ffmpeg;
		}

	}

	/**
	 * Stateless
	 */
	public FFmpeg generateBarsAnd1k(final String destination,
									final int durationInSec,
									final Point resolution) {
		final var ffmpeg = new FFmpeg(execName);
		final var builder = new Builder(ffmpeg);
		builder.setExecutableFinder(executableFinder);
		ffmpeg.addSimpleOutputDestination(destination);
		builder.process(new Conf(durationInSec, resolution));
		return ffmpeg;
	}

	/**
	 * Stateless
	 */
	public FFmpeg generateBarsAnd1k(final File destination,
									final int durationInSec,
									final Point resolution) {
		final var ffmpeg = new FFmpeg(execName);
		final var builder = new Builder(ffmpeg);
		builder.setExecutableFinder(executableFinder);
		ffmpeg.addSimpleOutputDestination(destination);
		builder.process(new Conf(durationInSec, resolution));
		return ffmpeg;
	}

}
