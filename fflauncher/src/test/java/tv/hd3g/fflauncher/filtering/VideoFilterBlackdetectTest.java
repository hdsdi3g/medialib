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
package tv.hd3g.fflauncher.filtering;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static tv.hd3g.fflauncher.filtering.lavfimtd.Utility.getFramesFromString;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tv.hd3g.fflauncher.filtering.lavfimtd.LavfiMtdEvent;

class VideoFilterBlackdetectTest {

	private static final String RAW_LINES = """
			frame:0 pts:7 pts_time:0.007
			lavfi.black_start=0.007
			frame:2 pts:90 pts_time:0.09
			lavfi.black_end=0.09
			frame:108 pts:4507 pts_time:4.507
			lavfi.black_start=4.507
			frame:116 pts:4841 pts_time:4.841
			lavfi.black_end=4.841
			""";

	VideoFilterBlackdetect f;

	@BeforeEach
	void init() throws Exception {
		f = new VideoFilterBlackdetect();
	}

	@Test
	void testToFilter() {
		assertEquals("blackdetect", f.toFilter().toString());
	}

	@Test
	void testGetEvents() {
		final var events = f.getEvents(getFramesFromString(RAW_LINES));
		assertNotNull(events);
		assertEquals(List.of(
				new LavfiMtdEvent("black", null, 0.007f, 0.09f),
				new LavfiMtdEvent("black", null, 4.507f, 4.841f)),
				events.getEvents());
	}

}
