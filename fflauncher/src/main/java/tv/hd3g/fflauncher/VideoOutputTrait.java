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
package tv.hd3g.fflauncher;

import tv.hd3g.fflauncher.enums.FFUnit;
import tv.hd3g.fflauncher.enums.Preset;
import tv.hd3g.fflauncher.enums.Tune;

public interface VideoOutputTrait extends InternalParametersSupplier {

	default void addPreset(final Preset preset) {
		getInternalParameters().addParameters("-preset", preset.toString());
	}

	default void addTune(final Tune tune) {
		getInternalParameters().addParameters("-tune", tune.toString());
	}

	/**
	 * Video bitrate
	 * @param outputVideoStreamIndex -1 by default
	 */
	default void addBitrate(final int bitrate, final FFUnit bitrateUnit, final int outputVideoStreamIndex) {
		if (outputVideoStreamIndex > -1) {
			getInternalParameters().addParameters("-b:v:" + outputVideoStreamIndex, bitrate + bitrateUnit
					.toString());
		} else {
			getInternalParameters().addParameters("-b:v", bitrate + bitrateUnit.toString());
		}
	}

	/**
	 * Video bitrate
	 * @param minRate set -1 for default
	 * @param maxRate set -1 for default
	 * @param bufsize set -1 for default
	 */
	default void addBitrateControl(final int minRate,
								   final int maxRate,
								   final int bufsize,
								   final FFUnit bitrateUnit) {
		if (minRate > 0) {
			getInternalParameters().addParameters("-minrate", minRate + bitrateUnit.toString());
		}
		if (maxRate > 0) {
			getInternalParameters().addParameters("-maxrate", maxRate + bitrateUnit.toString());
		}
		if (bufsize > 0) {
			getInternalParameters().addParameters("-bufsize", bufsize + bitrateUnit.toString());
		}
	}

	/**
	 * Constant bitrate factor, 0=lossless.
	 */
	default void addCRF(final int crf) {
		getInternalParameters().addParameters("-crf", String.valueOf(crf));
	}

	/**
	 * No checks will be done.
	 * See FFmpeg.addVideoEncoding for hardware use
	 * @param outputVideoStreamIndex -1 by default
	 */
	default void addVideoCodecName(final String codecName, final int outputVideoStreamIndex) {
		if (outputVideoStreamIndex > -1) {
			getInternalParameters().addParameters("-c:v:" + outputVideoStreamIndex, codecName);
		} else {
			getInternalParameters().addParameters("-c:v", codecName);
		}
	}

	/**
	 * @param b_frames set 0 for default
	 * @param gop_size set 0 for default
	 * @param ref_frames set 0 for default
	 */
	default void addGOPControl(final int b_frames, final int gop_size, final int ref_frames) {
		if (b_frames > 0) {
			getInternalParameters().addParameters("-bf", String.valueOf(b_frames));
		}
		if (gop_size > 0) {
			getInternalParameters().addParameters("-g", String.valueOf(gop_size));
		}
		if (ref_frames > 0) {
			getInternalParameters().addParameters("-ref", String.valueOf(ref_frames));
		}
	}

	/**
	 * @param i_qfactor set 0 for default
	 * @param b_qfactor set 0 for default
	 */
	default void addIBQfactor(final float i_qfactor, final float b_qfactor) {
		if (i_qfactor > 0f) {
			getInternalParameters().addParameters("-i_qfactor", String.valueOf(i_qfactor));
		}
		if (b_qfactor > 0f) {
			getInternalParameters().addParameters("-b_qfactor", String.valueOf(b_qfactor));
		}
	}

	/**
	 * @param qmin set 0 for default
	 * @param qmax set 0 for default
	 */
	default void addQMinMax(final int qmin, final int qmax) {
		if (qmin > 0) {
			getInternalParameters().addParameters("-qmin", String.valueOf(qmin));
		}
		if (qmax > 0) {
			getInternalParameters().addParameters("-qmax", String.valueOf(qmax));
		}
	}

	/**
	 * No checks will be done.
	 * like -vsync value
	 */
	default void addVsync(final int value) {
		getInternalParameters().addParameters("-vsync", String.valueOf(value));
	}

}
