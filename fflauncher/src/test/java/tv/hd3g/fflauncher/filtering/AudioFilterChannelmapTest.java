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
 * Copyright (C) hdsdi3g for hd3g.tv 2020
 *
 */
package tv.hd3g.fflauncher.filtering;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static tv.hd3g.fflauncher.enums.Channel.FL;
import static tv.hd3g.fflauncher.enums.Channel.FR;
import static tv.hd3g.fflauncher.enums.Channel.LFE;
import static tv.hd3g.fflauncher.enums.ChannelLayout.STEREO;

import java.util.Map;

import org.junit.jupiter.api.Test;

class AudioFilterChannelmapTest {

	@Test
	void testInvalidChannels() {
		final var map = Map.of(LFE, FL, FR, FL);
		assertThrows(IllegalArgumentException.class, () -> new AudioFilterChannelmap(STEREO, map));
	}

	@Test
	void testToFilter() {
		final var afc = new AudioFilterChannelmap(STEREO, Map.of(FL, FR, FR, FL));
		final var f = afc.toFilter();
		assertNotNull(f);
		assertEquals("channelmap=map=FR-FL|FL-FR:channel_layout=stereo", f.toString());
	}
}
