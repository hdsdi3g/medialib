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

import static java.util.Objects.requireNonNull;
import static tv.hd3g.fflauncher.progress.ProgressListenerSession.LOCALHOST_IPV4;

import java.io.File;
import java.time.Duration;
import java.util.Objects;
import java.util.function.Consumer;

import tv.hd3g.fflauncher.progress.ProgressCallback;
import tv.hd3g.fflauncher.progress.ProgressListener;
import tv.hd3g.processlauncher.ProcesslauncherBuilder;
import tv.hd3g.processlauncher.cmdline.Parameters;

public class FFmpeg extends FFbase implements
					InputGeneratorsTraits,
					HardwareProcessTraits,
					VideoOutputTrait,
					TemporalProcessTraits {

	public static final Duration statsPeriod = Duration.ofSeconds(1);

	private int deviceIdToUse = -1;
	protected ProgressListener progressListener;
	protected ProgressCallback progressCallback;

	public FFmpeg(final String execName, final Parameters parameters) {
		super(execName, parameters);
	}

	public FFmpeg setNostats() {
		parameters.ifHasNotParameter(() -> parameters.prependParameters("-nostats"), "-nostats");
		return this;
	}

	public FFmpeg setNoVideo() {
		parameters.ifHasNotParameter(() -> parameters.prependParameters("-vn"), "-vn");
		return this;
	}

	public FFmpeg setNoAudio() {
		parameters.ifHasNotParameter(() -> parameters.prependParameters("-an"), "-an");
		return this;
	}

	/**
	 * Define cmd var name like &lt;%OUT_AUTOMATIC_n%&gt; with "n" the # of setted destination.
	 * Add "-f container destination"
	 * Don't forget to call fixIOParametredVars
	 */
	public FFmpeg addSimpleOutputDestination(final String destinationName, final String destinationContainer) {
		requireNonNull(destinationName, "\"destinationName\" can't to be null");
		requireNonNull(destinationContainer, "\"destinationContainer\" can't to be null");

		final var varname = getInternalParameters()
				.tagVar("OUT_AUTOMATIC_" + outputExpectedDestinations.size());
		addOutputDestination(destinationName, varname, "-f", destinationContainer);
		return this;
	}

	/**
	 * Define cmd var name like &lt;%OUT_AUTOMATIC_n%&gt; with "n" the # of setted destination.
	 * Add "-f container /destination"
	 * Don't forget to call fixIOParametredVars
	 */
	public FFmpeg addSimpleOutputDestination(final File destinationFile, final String destinationContainer) {
		requireNonNull(destinationFile, "\"destinationFile\" can't to be null");
		requireNonNull(destinationContainer, "\"destinationContainer\" can't to be null");

		final var varname = getInternalParameters()
				.tagVar("OUT_AUTOMATIC_" + outputExpectedDestinations.size());
		addOutputDestination(destinationFile, varname, "-f", destinationContainer);
		return this;
	}

	/**
	 * Add "-movflags faststart"
	 * Please, put it a the end of command line, before output stream.
	 */
	public FFmpeg addFastStartMovMp4File() {
		getInternalParameters().addBulkParameters("-movflags faststart");
		return this;
	}

	/**
	 * Used with hardware transcoding.
	 * @param deviceIdToUse -1 by default
	 */
	public FFmpeg setDeviceIdToUse(final int deviceIdToUse) {
		this.deviceIdToUse = deviceIdToUse;
		return this;
	}

	/**
	 * @return -1 by default
	 */
	@Override
	public int getDeviceIdToUse() {
		return deviceIdToUse;
	}

	/**
	 * No checks will be done.
	 * @param outputAudioStreamIndex -1 by default
	 */
	public FFmpeg addAudioCodecName(final String codecName, final int outputAudioStreamIndex) {
		if (outputAudioStreamIndex > -1) {
			getInternalParameters().addParameters("-c:a:" + outputAudioStreamIndex, codecName);
		} else {
			getInternalParameters().addParameters("-c:a", codecName);
		}
		return this;
	}

	/**
	 * No checks will be done.
	 * like -map sourceIndex:streamIndexInSource ; 0 is the first.
	 */
	public FFmpeg addMap(final int sourceIndex, final int streamIndexInSource) {
		getInternalParameters().addParameters("-map", sourceIndex + ":" + streamIndexInSource);
		return this;
	}

	public FFmpeg setProgressListener(final ProgressListener progressListener,
									  final ProgressCallback progressCallback) {
		this.progressListener = Objects.requireNonNull(progressListener, "\"progressListener\" can't to be null");
		this.progressCallback = Objects.requireNonNull(progressCallback, "\"progressCallback\" can't to be null");
		return this;
	}

	public FFmpeg resetProgressListener() {
		progressListener = null;
		progressCallback = null;
		return this;
	}

	@Override
	public Consumer<ProcesslauncherBuilder> beforeExecute() {
		if (progressListener != null && progressCallback != null) {
			if (parameters.hasParameters("-progress")) {
				throw new IllegalArgumentException(
						"ffmpeg command line as already \"-progress\" option: " + parameters);
			}
			parameters.ifHasNotParameter(
					() -> parameters.prependParameters("-stats_period", String.valueOf(statsPeriod.toSeconds())),
					"-stats_period");

			final var session = progressListener.createSession(progressCallback, statsPeriod);
			final var port = session.start();
			parameters.prependParameters("-progress", "tcp://" + LOCALHOST_IPV4 + ":" + port);

			return builder -> {
				super.beforeExecute().accept(builder);
				builder.addExecutionCallbacker(processlauncherLifecycle -> session.manualClose());
			};
		}
		return super.beforeExecute();
	}

}
