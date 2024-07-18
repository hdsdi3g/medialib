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
package tv.hd3g.fflauncher;

public interface AudioOutputTrait extends InternalParametersSupplier {

	/**
	 * @param outputAudioStreamIndex -1 by default
	 */
	default void addAudioCodecName(final String codecName, final int outputAudioStreamIndex) {
		if (outputAudioStreamIndex > -1) {
			getInternalParameters().addParameters("-c:a:" + outputAudioStreamIndex, codecName);
		} else {
			getInternalParameters().addParameters("-c:a", codecName);
		}
	}

	/**
	 * @param outputAudioStreamIndex -1 by default
	 */
	default void addAudioChannelCount(final int channel, final int outputAudioStreamIndex) {
		if (outputAudioStreamIndex > -1) {
			getInternalParameters().addParameters("-ac:" + outputAudioStreamIndex, String.valueOf(channel));
		} else {
			getInternalParameters().addParameters("-ac", String.valueOf(channel));
		}
	}

	/**
	 * @param outputAudioStreamIndex -1 by default
	 */
	default void addAudioSamplingRate(final int sr, final int outputAudioStreamIndex) {
		if (outputAudioStreamIndex > -1) {
			getInternalParameters().addParameters("-ar:" + outputAudioStreamIndex, String.valueOf(sr));
		} else {
			getInternalParameters().addParameters("-ar", String.valueOf(sr));
		}
	}

}
