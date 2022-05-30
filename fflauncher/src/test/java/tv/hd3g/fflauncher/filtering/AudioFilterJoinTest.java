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
import static tv.hd3g.fflauncher.enums.ChannelLayout.CH5_1;
import static tv.hd3g.fflauncher.enums.ChannelLayout.STEREO;

import java.util.Map;

import org.junit.jupiter.api.Test;

import tv.hd3g.fflauncher.enums.Channel;

class AudioFilterJoinTest {

	@Test
	void testInvalidCount() {
		final var map = Map.of(Channel.FL, "0.0", Channel.FR, "1.0");
		assertThrows(IllegalArgumentException.class,
		        () -> new AudioFilterJoin(6, CH5_1, map));
	}

	@Test
	void testMissingC() {
		final var map = Map.of(Channel.LFE2, "0.0", Channel.FR, "1.0");
		assertThrows(IllegalArgumentException.class,
		        () -> new AudioFilterJoin(2, STEREO, map));
	}

	@Test
	void testToFilter() {
		final var afj = new AudioFilterJoin(2, STEREO, Map.of(Channel.FL, "0.0", Channel.FR, "1.0"));
		final var f = afj.toFilter();
		assertNotNull(f);
		assertEquals("join=inputs=2:channel_layout=stereo:map=0.0-FL|1.0-FR", f.toString());
	}
}
