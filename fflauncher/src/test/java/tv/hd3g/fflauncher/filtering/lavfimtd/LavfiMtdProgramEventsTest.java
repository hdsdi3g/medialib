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
package tv.hd3g.fflauncher.filtering.lavfimtd;

import static java.lang.Float.NaN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static tv.hd3g.fflauncher.filtering.lavfimtd.Utility.getFramesFromString;

import java.util.List;

import org.junit.jupiter.api.Test;

class LavfiMtdProgramEventsTest {
	final static String RAW_LINES_OK = """
			frame:0 pts:7 pts_time:0.007
			lavfi.black_start=0.007
			frame:2 pts:90 pts_time:0.09
			lavfi.black_end=0.09
			frame:66 pts:2757 pts_time:2.757
			lavfi.freezedetect.freeze_start=0.757
			frame:97 pts:4049 pts_time:4.049
			lavfi.freezedetect.freeze_duration=3.292
			lavfi.freezedetect.freeze_end=4.049
			frame:108 pts:4507 pts_time:4.507
			lavfi.black_start=4.507
			frame:116 pts:4841 pts_time:4.841
			lavfi.black_end=4.841
			frame:1022 pts:981168 pts_time:20.441
			lavfi.aphasemeter.phase=1.000000
			lavfi.aphasemeter.mono_start=18.461
			frame:1299 pts:1247088 pts_time:25.981
			lavfi.aphasemeter.phase=0.992454
			lavfi.aphasemeter.mono_end=25.981
			lavfi.aphasemeter.mono_duration=2.94
			frame:3306 pts:3173808 pts_time:66.121
			lavfi.silence_start.1=65.1366
			lavfi.silence_start.2=65.1366
			frame:3332 pts:3198768 pts_time:66.641
			lavfi.silence_end.1=66.6474
			lavfi.silence_duration.1=1.51079
			lavfi.silence_end.2=66.6474
			lavfi.silence_duration.2=1.51079
			""";

	final static String RAW_LINES_MISSING_START = """
			frame:0 pts:7 pts_time:0.007
			lavfi.black_start=0.007
			frame:2 pts:90 pts_time:0.09
			lavfi.black_end=0.09
			frame:116 pts:4841 pts_time:4.841
			lavfi.test_end=4.841
			""";

	final static String RAW_LINES_MISSING_END = """
			frame:0 pts:7 pts_time:0.007
			lavfi.black_start=0.007
			frame:2 pts:90 pts_time:0.09
			lavfi.black_end=0.09
			frame:116 pts:4841 pts_time:4.841
			lavfi.test_start=4.841
			""";

	LavfiMtdProgramEvents pe;

	@Test
	void testGetEvents_freeze() {
		pe = new LavfiMtdProgramEvents(getFramesFromString(RAW_LINES_OK), "freezedetect", "freeze");
		assertNotNull(pe.getEvents());
		assertEquals(List.of(new LavfiMtdEvent("freeze", null, 0.757f, 4.049f)), pe.getEvents());
	}

	@Test
	void testGetEvents_mono() {
		pe = new LavfiMtdProgramEvents(getFramesFromString(RAW_LINES_OK), "aphasemeter", "mono");
		assertNotNull(pe.getEvents());
		assertEquals(List.of(new LavfiMtdEvent("mono", null, 18.461f, 25.981f)), pe.getEvents());
	}

	@Test
	void testGetEvents_silence() {
		pe = new LavfiMtdProgramEvents(getFramesFromString(RAW_LINES_OK), "silence");
		assertNotNull(pe.getEvents());
		assertEquals(List.of(
				new LavfiMtdEvent("silence", "1", 65.1366f, 66.6474f),
				new LavfiMtdEvent("silence", "2", 65.1366f, 66.6474f)),
				pe.getEvents());
	}

	@Test
	void testGetEvents_black() {
		pe = new LavfiMtdProgramEvents(getFramesFromString(RAW_LINES_OK), "black");
		assertNotNull(pe.getEvents());
		assertEquals(List.of(
				new LavfiMtdEvent("black", null, 0.007f, 0.09f),
				new LavfiMtdEvent("black", null, 4.507f, 4.841f)),
				pe.getEvents());
	}

	@Test
	void testGetEvents_missingStart() {
		pe = new LavfiMtdProgramEvents(getFramesFromString(RAW_LINES_MISSING_START), "test");
		assertNotNull(pe.getEvents());
		assertEquals(List.of(), pe.getEvents());
	}

	@Test
	void testGetEvents_missingEnd() {
		pe = new LavfiMtdProgramEvents(getFramesFromString(RAW_LINES_MISSING_END), "test");
		assertNotNull(pe.getEvents());
		assertEquals(List.of(new LavfiMtdEvent("test", null, 4.841f, NaN)), pe.getEvents());
	}

}
