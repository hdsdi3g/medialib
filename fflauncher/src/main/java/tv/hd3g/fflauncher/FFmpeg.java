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

import java.io.File;

import tv.hd3g.processlauncher.cmdline.Parameters;

public class FFmpeg extends FFbase implements
					SimpleSourceTraits,
					HardwareProcessTraits,
					VideoOutputTrait,
					AudioOutputTrait,
					TemporalProcessTraits {

	private int deviceIdToUse = -1;

	public FFmpeg(final String execName, final Parameters parameters) {
		super(execName, parameters);
	}

	public FFmpeg(final String execName) {
		super(execName, new Parameters());
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
	 * like -map sourceIndex:streamIndexInSource ; 0 is the first.
	 */
	public FFmpeg addMap(final int sourceIndex, final int streamIndexInSource) {
		getInternalParameters().addParameters("-map", sourceIndex + ":" + streamIndexInSource);
		return this;
	}

}
