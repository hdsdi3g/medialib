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
 * Copyright (C) hdsdi3g for hd3g.tv 2024
 *
 */
package tv.hd3g.fflauncher.recipes;

import static tv.hd3g.fflauncher.enums.FFLogLevel.ERROR;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

import tv.hd3g.fflauncher.FFmpeg;
import tv.hd3g.fflauncher.filtering.FilterChains;
import tv.hd3g.fflauncher.filtering.VideoFilterScale;
import tv.hd3g.fflauncher.filtering.VideoFilterSetSAR;
import tv.hd3g.fflauncher.filtering.VideoFilterThumbnail;
import tv.hd3g.fflauncher.filtering.VideoFilterYadif;
import tv.hd3g.fflauncher.processingtool.FFmpegToolBuilder;
import tv.hd3g.processlauncher.ProcesslauncherLifecycle;
import tv.hd3g.processlauncher.cmdline.ExecutableFinder;
import tv.hd3g.processlauncher.cmdline.Parameters;
import tv.hd3g.processlauncher.processingtool.DirectStdoutGetStderrWatcher;

public class ImageSnapshotExtractor {

	private final String execName;
	private final ExecutableFinder executableFinder;

	public ImageSnapshotExtractor(final ExecutableFinder executableFinder) {
		this("ffmpeg", executableFinder);
	}

	public ImageSnapshotExtractor(final String execName, final ExecutableFinder executableFinder) {
		this.execName = execName;
		this.executableFinder = executableFinder;
	}

	private record Conf(File source,
						String startTime,
						boolean preciseTiming,
						int searchBestFrameOnCount) {
	}

	/**
	 * Stateless (only I frames)
	 */
	public byte[] bestEffortTimedSnapshot(final File source, final String startTime, final int searchBestFrameOnCount) {
		final var builder = new Builder(execName);
		builder.setExecutableFinder(executableFinder);
		return builder.process(new Conf(source, startTime, false, searchBestFrameOnCount))
				.getResult();
	}

	/**
	 * Stateless (all frames)
	 */
	public byte[] preciseTimedSnapshot(final File source, final String startTime, final int searchBestFrameOnCount) {
		final var builder = new Builder(execName);
		builder.setExecutableFinder(executableFinder);
		return builder.process(new Conf(source, startTime, true, searchBestFrameOnCount))
				.getResult();
	}

	private class Builder extends FFmpegToolBuilder<Conf, byte[], DirectStdoutGetStderrWatcher> {

		private final ByteArrayOutputStream outputStream;

		protected Builder(final String execName) {
			super(new FFmpeg(execName), new DirectStdoutGetStderrWatcher());
			outputStream = new ByteArrayOutputStream(0xFFFF);

			executorWatcher.setStdOutConsumer((processInputStream, source) -> {
				try {
					int readed;
					final var buffer = new byte[0xFFFF];
					while ((readed = processInputStream.read(buffer)) > 0) {
						outputStream.write(buffer, 0, readed);
					}
				} catch (final IOException e) {
					throw new UncheckedIOException("Can't read from stdout", e);
				}
			});
		}

		@Override
		protected byte[] compute(final Conf sourceOrigin, final ProcesslauncherLifecycle lifeCycle) {
			return outputStream.toByteArray();
		}

		@Override
		protected FFmpeg getParametersProvider(final Conf sourceOrigin) {
			final var inputParameters = new Parameters();

			if (sourceOrigin.startTime != null) {
				inputParameters.addParameters("-ss", sourceOrigin.startTime);

				if (sourceOrigin.preciseTiming == false) {
					inputParameters.addBulkParameters("-skip_frame nokey");
				}
			}
			ffmpeg.addSimpleInputSource(sourceOrigin.source, inputParameters.getParameters());
			ffmpeg.setLogLevel(ERROR, false, false);

			final var parameters = ffmpeg.getInternalParameters();
			parameters.addBulkParameters("-map_metadata -1 -an -frames:v 1 -qscale:v 1 -huffman optimal");

			if (sourceOrigin.preciseTiming == false) {
				inputParameters.addBulkParameters("-vsync vfr");
			}

			final var vfChain = new FilterChains();
			final var chain = vfChain.createChain();

			if (sourceOrigin.searchBestFrameOnCount > 0) {
				final var thum = new VideoFilterThumbnail();
				thum.setN(sourceOrigin.searchBestFrameOnCount);
				chain.add(thum.toFilter());
			}

			final var yadif = new VideoFilterYadif();
			yadif.setMode(VideoFilterYadif.Mode.SEND_FRAME_NO_SPATIAL);
			yadif.setDeint(VideoFilterYadif.Deint.INTERLACED);
			chain.add(yadif.toFilter());

			final var scale = new VideoFilterScale();
			scale.setWidth("trunc(ih*dar/2)*2");
			scale.setHeight("trunc(ih/2)*2");
			scale.setOutColorMatrix("bt709");
			scale.setOutRange("jpeg");
			chain.add(scale.toFilter());

			final var sar = new VideoFilterSetSAR();
			sar.setRatio("1/1");
			chain.add(sar.toFilter());

			vfChain.pushFilterChainTo("-vf", ffmpeg);

			ffmpeg.addVideoCodecName("mjpeg", -1);
			ffmpeg.addSimpleOutputDestination("-", "image2");
			return ffmpeg;
		}
	}

}
