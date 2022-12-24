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

class VideoFilterFreezedetectTest {

	private static final String RAW_LINES = """
			frame:66 pts:2757 pts_time:2.757
			lavfi.freezedetect.freeze_start=0.757
			frame:97 pts:4049 pts_time:4.049
			lavfi.freezedetect.freeze_duration=3.292
			lavfi.freezedetect.freeze_end=4.049
			""";

	VideoFilterFreezedetect f;

	@BeforeEach
	void init() throws Exception {
		f = new VideoFilterFreezedetect();
	}

	@Test
	void testToFilter() {
		assertEquals("freezedetect", f.toFilter().toString());
		f.setNoiseToleranceDb(60);
		assertEquals("freezedetect=noise=-60dB", f.toFilter().toString());
		f.setNoiseToleranceDb(-5);
		assertEquals("freezedetect=noise=-5dB", f.toFilter().toString());
	}

	@Test
	void testGetEvents() {
		final var events = f.getEvents(getFramesFromString(RAW_LINES));
		assertNotNull(events);
		assertEquals(List.of(
				new LavfiMtdEvent("freeze", null, 0.757f, 4.049f)),
				events.getEvents());
	}
}
