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
package tv.hd3g.fflauncher.resultparser;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RawStdErrEventParserTest {

	RawStdErrEventParser p;
	List<RawStdErrFilterEvent> events;

	final float integrated = -17.6f;
	final float integratedThreshold = -28.2f;
	final float loudnessRange = 6.5f;
	final float loudnessRangeThreshold = -38.2f;
	final float loudnessRangeLow = -21.6f;
	final float loudnessRangeHigh = -15.1f;
	final float samplePeak = -1.4f;
	final float truePeak = -1.5f;

	@BeforeEach
	void init() throws Exception {
		events = new ArrayList<>();
		p = new RawStdErrEventParser(events::add);
	}

	@Test
	void testParse_ebur128() throws IOException {
		final var allStdErrLines = Files.readAllLines(new File("src/test/resources/ebur128.txt").toPath(), UTF_8);
		allStdErrLines.forEach(p::onLine);
		final var s = p.close();
		assertNotNull(s);
		checks(s);

		assertEquals(2999, events.size());
		assertEquals(
				"RawStdErrFilterEvent(filterName=ebur128, filterChainPos=0, content={t=0.106979, TARGET=-23 LUFS, M=-120.7, S=-120.7, I=-70.0 LUFS, LRA=0.0 LU, SPK=-35.2 -35.3 dBFS, FTPK=-35.2 -35.3 dBFS, TPK=-35.2 -35.3 dBFS})",
				events.get(0).toString());
		assertEquals(
				"RawStdErrFilterEvent(filterName=ebur128, filterChainPos=0, content={t=299.907, TARGET=-23 LUFS, M=-14.4, S=-19.1, I=-17.6 LUFS, LRA=6.5 LU, SPK=-1.4  -1.4 dBFS, FTPK=-6.7  -6.7 dBFS, TPK=-1.4  -1.4 dBFS})",
				events.get(2998).toString());
	}

	@Test
	void testParse_ebur128_alt() throws IOException {
		final var allStdErrLines = Files.readAllLines(new File("src/test/resources/ebur128-alt.txt").toPath(), UTF_8);
		allStdErrLines.forEach(p::onLine);
		final var s = p.close();
		assertNotNull(s);
		checks(s);
	}

	private void checks(final Ebur128Summary s) {
		assertEquals(integrated, s.getIntegrated());
		assertEquals(integratedThreshold, s.getIntegratedThreshold());
		assertEquals(loudnessRange, s.getLoudnessRange());
		assertEquals(loudnessRangeThreshold, s.getLoudnessRangeThreshold());
		assertEquals(loudnessRangeLow, s.getLoudnessRangeLow());
		assertEquals(loudnessRangeHigh, s.getLoudnessRangeHigh());
		assertEquals(samplePeak, s.getSamplePeak());
		assertEquals(truePeak, s.getTruePeak());
	}

}
