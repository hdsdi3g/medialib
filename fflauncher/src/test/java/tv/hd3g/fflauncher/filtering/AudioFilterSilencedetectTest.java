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

class AudioFilterSilencedetectTest {

	private static final String RAW_LINES = """
			frame:3306 pts:3173808 pts_time:66.121
			lavfi.silence_start.1=65.1366
			lavfi.silence_start.2=65.1367
			frame:3332 pts:3198768 pts_time:66.641
			lavfi.silence_end.1=66.6474
			lavfi.silence_duration.1=1.51079
			lavfi.silence_end.2=66.6479
			lavfi.silence_duration.2=1.51079 *
			""";

	AudioFilterSilencedetect f;

	@BeforeEach
	void init() throws Exception {
		f = new AudioFilterSilencedetect();
	}

	@Test
	void testToFilter() {
		assertEquals("silencedetect", f.toFilter().toString());
	}

	@Test
	void testGetEvents() {
		final var events = f.getEvents(getFramesFromString(RAW_LINES));
		assertNotNull(events);
		assertEquals(List.of(
				new LavfiMtdEvent("silence", "1", 65.1366f, 66.6474f),
				new LavfiMtdEvent("silence", "2", 65.1367f, 66.6479f)),
				events.getEvents());
	}
}
