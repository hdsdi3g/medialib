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
package tv.hd3g.fflauncher.ffprobecontainer;

import java.util.Objects;

import tv.hd3g.fflauncher.enums.ChannelLayout;

public record FFprobeAudioFrameConst(FFprobeAudioFrame updatedWith,
									 String sampleFmt,
									 int channels,
									 ChannelLayout channelLayout) {

	public boolean valuesEquals(final FFprobeAudioFrameConst obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final var other = obj;
		return channelLayout == other.channelLayout
			   && channels == other.channels
			   && Objects.equals(sampleFmt, other.sampleFmt);
	}

}
