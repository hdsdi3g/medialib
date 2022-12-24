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

import java.util.DoubleSummaryStatistics;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tv.hd3g.fflauncher.filtering.VideoFilterSiti.LavfiMtdSiti;
import tv.hd3g.fflauncher.filtering.VideoFilterSiti.LavfiMtdSitiSummary;
import tv.hd3g.fflauncher.filtering.lavfimtd.LavfiMtdPosition;

class VideoFilterSitiTest {

	private static final String RAW_LINES = """
			frame:119 pts:4966 pts_time:4.966
			lavfi.siti.si=11.67
			lavfi.siti.ti=5.60
			""";

	VideoFilterSiti f;

	@BeforeEach
	void init() throws Exception {
		f = new VideoFilterSiti();
	}

	@Test
	void testToFilter() {
		assertEquals("siti", f.toFilter().toString());
	}

	@Test
	void testGetMetadatas() {
		final var frames = f.getMetadatas(getFramesFromString(RAW_LINES));
		assertNotNull(frames);
		assertEquals(Map.of(
				new LavfiMtdPosition(119, 4966, 4.966f), new LavfiMtdSiti(11.67f, 5.60f)),
				frames.getFrames());
	}

	@Test
	void testGetMetadatas_noSi() {
		final var frames = f.getMetadatas(getFramesFromString("""
				frame:119 pts:4966 pts_time:4.966
				lavfi.siti.ti=5.60
				"""));
		assertNotNull(frames);
		assertEquals(Map.of(), frames.getFrames());
	}

	@Test
	void testGetMetadatas_noTi() {
		final var frames = f.getMetadatas(getFramesFromString("""
				frame:119 pts:4966 pts_time:4.966
				lavfi.siti.si=5.60
				"""));
		assertNotNull(frames);
		assertEquals(Map.of(), frames.getFrames());
	}

	@Test
	void testComputeSitiStats() {
		final var frames = f.getMetadatas(getFramesFromString("""
				frame:118 pts:4965 pts_time:4.965
				lavfi.siti.si=5
				lavfi.siti.ti=7
				frame:119 pts:4966 pts_time:4.966
				lavfi.siti.si=15
				lavfi.siti.ti=3
				"""));
		final var sum = VideoFilterSiti.computeSitiStats(frames);
		assertNotNull(sum);

		final var dssSi = new DoubleSummaryStatistics();
		dssSi.accept(5);
		dssSi.accept(15);

		final var dssTi = new DoubleSummaryStatistics();
		dssTi.accept(7);
		dssTi.accept(3);

		assertEquals(new LavfiMtdSitiSummary(dssSi, dssTi).toString(), sum.toString());
	}
}
