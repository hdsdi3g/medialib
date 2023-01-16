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
 * Copyright (C) hdsdi3g for hd3g.tv 2023
 *
 */
package tv.hd3g.fflauncher;

public interface TemporalProcessTraits extends InternalParametersSupplier {

	/**
	 * When used as an output option (before an output url), stop writing the output after its duration reaches duration.
	 * duration must be a time duration specification, see (ffmpeg-utils)the Time duration section in the ffmpeg-utils(1) manual.
	 **/
	default void addDuration(final String duration) {
		getInternalParameters().addParameters("-t", duration);
	}

	/**
	 * Stop writing the output or reading the input at position.
	 * position must be a time duration specification, see (ffmpeg-utils)the Time duration section in the ffmpeg-utils(1) manual.
	 */
	default void addToDuration(final String position) {
		getInternalParameters().addParameters("-to", position);
	}

	/**
	 * When used as an output option (before an output url), decodes but discards input until the timestamps reach position.
	 * position must be a time duration specification, see (ffmpeg-utils)the Time duration section in the ffmpeg-utils(1) manual.
	 */
	default void addStartPosition(final String position) {
		getInternalParameters().addParameters("-ss", position);
	}

	/**
	 * Like the -ss option but relative to the "end of file". That is negative values are earlier in the file, 0 is at EOF.
	 * position must be a time duration specification, see (ffmpeg-utils)the Time duration section in the ffmpeg-utils(1) manual.
	 */
	default void addEndPosition(final String position) {
		getInternalParameters().addParameters("-sseof", position);
	}

}
