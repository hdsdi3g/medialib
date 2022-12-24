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

import tv.hd3g.fflauncher.filtering.AudioFilterAstats.LavfiMtdAstats;
import tv.hd3g.fflauncher.filtering.AudioFilterAstats.LavfiMtdAstatsChannel;
import tv.hd3g.fflauncher.filtering.lavfimtd.LavfiMtdPosition;

class AudioFilterAstatsTest {

	private static final String RAW_LINES = """
			frame:87883 pts:84367728 pts_time:1757.66
			lavfi.astats.1.DC_offset=0.000001
			lavfi.astats.1.Peak_level=-0.622282
			lavfi.astats.1.Flat_factor=0.000000
			lavfi.astats.1.Peak_count=2.000000
			lavfi.astats.1.Noise_floor=-78.266739
			lavfi.astats.1.Noise_floor_count=708.000000
			lavfi.astats.1.Entropy=0.788192
			lavfi.astats.2.DC_offset=0.000002
			lavfi.astats.2.Peak_level=-0.622282
			lavfi.astats.2.Flat_factor=0.000000
			lavfi.astats.2.Peak_count=2.000000
			lavfi.astats.2.Noise_floor=-78.266739
			lavfi.astats.2.Noise_floor_count=1074.000000
			lavfi.astats.2.Entropy=0.788152
			""";

	AudioFilterAstats f;

	@BeforeEach
	void init() throws Exception {
		f = new AudioFilterAstats();
	}

	@Test
	void testToFilter() {
		assertEquals("astats", f.toFilter().toString());
	}

	@Test
	void testSetSelectedMetadatas() {
		f.setSelectedMetadatas();
		assertEquals(
				"astats=metadata=1:measure_perchannel=DC_offset+Peak_level+Flat_factor+Peak_count+Noise_floor+Noise_floor_count+Entropy:measure_overall=none",
				f.toFilter().toString());
	}

	@Test
	void testGetMetadatas() {
		final var frames = f.getMetadatas(getFramesFromString(RAW_LINES));
		assertNotNull(frames);
		assertEquals(Map.of(
				new LavfiMtdPosition(87883, 84367728, 1757.66f),
				new LavfiMtdAstats(List.of(
						new LavfiMtdAstatsChannel(
								0.000001f,
								-0.622282f,
								0.000000f,
								2l,
								-78.266739f,
								708l,
								0.788192f,
								Map.of()),
						new LavfiMtdAstatsChannel(
								0.000002f,
								-0.622282f,
								0.000000f,
								2l,
								-78.266739f,
								1074l,
								0.788152f,
								Map.of())))),
				frames.getFrames());
	}

}
