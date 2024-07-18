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
package tv.hd3g.fflauncher.processingtool;

import static java.util.Objects.requireNonNull;

import java.time.Duration;

import lombok.Getter;
import tv.hd3g.fflauncher.FFmpeg;
import tv.hd3g.fflauncher.progress.ProgressCallback;
import tv.hd3g.fflauncher.progress.ProgressListener;
import tv.hd3g.fflauncher.progress.ProgressListenerSession;
import tv.hd3g.processlauncher.ProcesslauncherBuilder;
import tv.hd3g.processlauncher.cmdline.Parameters;
import tv.hd3g.processlauncher.processingtool.ExecutorWatcher;
import tv.hd3g.processlauncher.processingtool.ProcessingToolBuilder;
import tv.hd3g.processlauncher.processingtool.ProcessingToolCallback;

/**
 * @param <O> Origin source type
 * @param <T> Output produced type
 */
public abstract class FFmpegToolBuilder<O, T, W extends ExecutorWatcher>
									   extends ProcessingToolBuilder<O, FFmpeg, T, W> {

	public static final Duration statsPeriod = Duration.ofSeconds(1);

	@Getter
	protected final FFmpeg ffmpeg;
	private ProgressListener progressListener;
	private ProgressCallback progressCallback;

	protected FFmpegToolBuilder(final FFmpeg ffmpeg, final W watcher) {
		super(ffmpeg.getExecutableName(), watcher);
		this.ffmpeg = requireNonNull(ffmpeg, "\"ffmpeg\" can't to be null");
		callbacks.add(new PrepareFFmpeg());
		callbacks.add(ffmpeg.makeConversionHooks());
	}

	public void setProgressListener(final ProgressListener progressListener,
									final ProgressCallback progressCallback) {
		this.progressListener = requireNonNull(progressListener, "\"progressListener\" can't to be null");
		this.progressCallback = requireNonNull(progressCallback, "\"progressCallback\" can't to be null");
	}

	public void resetProgressListener() {
		progressListener = null;
		progressCallback = null;
	}

	private class PrepareFFmpeg implements ProcessingToolCallback {

		ProgressListenerSession session;

		@Override
		public void prepareParameters(final Parameters parameters) {
			if (progressListener != null && progressCallback != null) {
				if (parameters.hasParameters("-progress")) {
					throw new IllegalArgumentException(
							"ffmpeg command line as already \"-progress\" option: " + parameters);
				}
				parameters.ifHasNotParameter(
						() -> parameters.prependParameters("-stats_period", String.valueOf(statsPeriod.toSeconds())),
						"-stats_period");

				session = progressListener.createSession(progressCallback, statsPeriod);
				final var port = session.start();
				parameters.prependParameters("-progress", "tcp://127.0.0.1:" + port);
			}
		}

		@Override
		public void beforeRun(final ProcesslauncherBuilder pBuilder) {
			if (session != null) {
				pBuilder.addExecutionCallbacker(processlauncherLifecycle -> session.manualClose());
			}
		}

	}

}
