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
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tv.hd3g.fflauncher.filtering.AudioFilterAPhasemeter.LavfiMtdAPhaseMeter;
import tv.hd3g.fflauncher.filtering.lavfimtd.LavfiMtdEvent;
import tv.hd3g.fflauncher.filtering.lavfimtd.LavfiMtdPosition;

class AudioFilterAPhasemeterTest {

	private static final String RAW_LINES = """
			frame:1022 pts:981168 pts_time:20.441
			lavfi.aphasemeter.phase=1.000000
			lavfi.aphasemeter.mono_start=18.461
			frame:1299 pts:1247088 pts_time:25.981
			lavfi.aphasemeter.phase=0.992454
			lavfi.aphasemeter.mono_end=25.981
			lavfi.aphasemeter.mono_duration=2.94
			""";

	AudioFilterAPhasemeter f;

	@BeforeEach
	void init() throws Exception {
		f = new AudioFilterAPhasemeter();
	}

	@Test
	void testToFilter() {
		assertEquals("aphasemeter", f.toFilter().toString());
	}

	@Test
	void testGetMetadatas() {
		final var frames = f.getMetadatas(getFramesFromString(RAW_LINES));
		assertNotNull(frames);
		assertEquals(Map.of(
				new LavfiMtdPosition(1022, 981168l, 20.441f), new LavfiMtdAPhaseMeter(1.0f),
				new LavfiMtdPosition(1299, 1247088, 25.981f), new LavfiMtdAPhaseMeter(0.992454f)),
				frames.getFrames());
	}

	@Test
	void testGetEvents() {
		final var events = f.getEvents(getFramesFromString(RAW_LINES));
		assertNotNull(events);
		assertEquals(List.of(
				new LavfiMtdEvent("mono", null, 18.461f, 25.981f)),
				events.getEvents());
	}
}
