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
import static tv.hd3g.fflauncher.filtering.VideoFilterIdet.LavfiMtdIdetRepeatedFrameType.NEITHER;
import static tv.hd3g.fflauncher.filtering.VideoFilterIdet.LavfiMtdIdetSingleFrameType.PROGRESSIVE;
import static tv.hd3g.fflauncher.filtering.lavfimtd.Utility.getFramesFromString;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tv.hd3g.fflauncher.filtering.VideoFilterIdet.LavfiMtdIdet;
import tv.hd3g.fflauncher.filtering.VideoFilterIdet.LavfiMtdIdetFrame;
import tv.hd3g.fflauncher.filtering.VideoFilterIdet.LavfiMtdIdetRepeatedFrame;
import tv.hd3g.fflauncher.filtering.lavfimtd.LavfiMtdPosition;

class VideoFilterIdetTest {

	private static final String RAW_LINES = """
			frame:119 pts:4966 pts_time:4.966
			lavfi.idet.repeated.current_frame=neither
			lavfi.idet.repeated.neither=115.00
			lavfi.idet.repeated.top=2.00
			lavfi.idet.repeated.bottom=3.00
			lavfi.idet.single.current_frame=progressive
			lavfi.idet.single.tff=0.00
			lavfi.idet.single.bff=0.00
			lavfi.idet.single.progressive=40.00
			lavfi.idet.single.undetermined=80.00
			lavfi.idet.multiple.current_frame=progressive
			lavfi.idet.multiple.tff=0.00
			lavfi.idet.multiple.bff=0.00
			lavfi.idet.multiple.progressive=120.00
			lavfi.idet.multiple.undetermined=0.00
			""";

	VideoFilterIdet f;

	@BeforeEach
	void init() throws Exception {
		f = new VideoFilterIdet();
	}

	@Test
	void testToFilter() {
		assertEquals("idet", f.toFilter().toString());
	}

	@Test
	void testGetMetadatas() {
		final var frames = f.getMetadatas(getFramesFromString(RAW_LINES));
		assertNotNull(frames);

		final var single = new LavfiMtdIdetFrame(PROGRESSIVE, 0, 0, 40, 80);
		final var multiple = new LavfiMtdIdetFrame(PROGRESSIVE, 0, 0, 120, 0);
		final var repeated = new LavfiMtdIdetRepeatedFrame(NEITHER, 115, 2, 3);

		assertEquals(Map.of(
				new LavfiMtdPosition(119, 4966, 4.966f), new LavfiMtdIdet(single, multiple, repeated)),
				frames.getFrames());
	}
}
