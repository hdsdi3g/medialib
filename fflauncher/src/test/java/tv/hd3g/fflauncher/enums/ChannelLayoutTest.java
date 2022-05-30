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
package tv.hd3g.fflauncher.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static tv.hd3g.fflauncher.enums.Channel.BC;
import static tv.hd3g.fflauncher.enums.Channel.BL;
import static tv.hd3g.fflauncher.enums.Channel.BR;
import static tv.hd3g.fflauncher.enums.Channel.FC;
import static tv.hd3g.fflauncher.enums.Channel.FL;
import static tv.hd3g.fflauncher.enums.Channel.FR;
import static tv.hd3g.fflauncher.enums.Channel.SL;
import static tv.hd3g.fflauncher.enums.Channel.SR;

import java.util.List;

import org.junit.jupiter.api.Test;

class ChannelLayoutTest {

	@Test
	void testParse() {
		final var values = ChannelLayout.values();
		for (int pos = 0; pos < values.length; pos++) {
			final var v = values[pos];
			assertEquals(v, ChannelLayout.parse(v.toString()));
		}
	}

	@Test
	void testGetChannelList() {
		assertEquals(List.of(FL, FR, FC, BL, BR, BC, SL, SR), ChannelLayout.OCTAGONAL.getChannelList());
	}

	@Test
	void testToString() {
		assertEquals("5.0(side)", ChannelLayout.CH5_0_SIDE.toString());
	}
}
