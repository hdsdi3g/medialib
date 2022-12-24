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

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tv.hd3g.fflauncher.filtering.VideoFilterBlockdetect.LavfiMtdBlockdetect;
import tv.hd3g.fflauncher.filtering.lavfimtd.LavfiMtdPosition;

class VideoFilterBlockdetectTest {

	private static final String RAW_LINES = """
			frame:111 pts:4632 pts_time:4.632
			lavfi.block=2.204194
			""";

	VideoFilterBlockdetect f;

	@BeforeEach
	void init() throws Exception {
		f = new VideoFilterBlockdetect();
	}

	@Test
	void testToFilter() {
		assertEquals("blockdetect", f.toFilter().toString());
	}

	@Test
	void testGetMetadatas() {
		final var frames = f.getMetadatas(getFramesFromString(RAW_LINES));
		assertNotNull(frames);
		assertEquals(Map.of(
				new LavfiMtdPosition(111, 4632, 4.632f), new LavfiMtdBlockdetect(2.204194f)),
				frames.getFrames());
	}

}
