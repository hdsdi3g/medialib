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

import static net.datafaker.Faker.instance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tv.hd3g.fflauncher.resultparser.Ebur128Summary.extractValue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.datafaker.Faker;

class Ebur128SummaryTest {

	static Faker faker = instance();

	Ebur128Summary s;

	float integrated;
	float integratedThreshold;
	float loudnessRange;
	float loudnessRangeThreshold;
	float loudnessRangeLow;
	float loudnessRangeHigh;
	float samplePeak;
	float truePeak;

	@BeforeEach
	void init() throws Exception {
		integrated = -17.6f;
		integratedThreshold = -28.2f;
		loudnessRange = 6.5f;
		loudnessRangeThreshold = -38.2f;
		loudnessRangeLow = -21.6f;
		loudnessRangeHigh = -15.1f;
		samplePeak = -1.4f;
		truePeak = -1.5f;
		s = new Ebur128Summary();

		final var rawSummaryZone = List.of(
				List.of("Integrated loudness"),
				List.of("I", "-17.6 LUFS"),
				List.of("Threshold", "-28.2 LUFS"),
				List.of("Loudness range"),
				List.of("LRA", "6.5 LU"),
				List.of("Threshold", "-38.2 LUFS"),
				List.of("LRA low", "-21.6 LUFS"),
				List.of("LRA high", "-15.1 LUFS"),
				List.of("Sample peak"),
				List.of("Peak", "-1.4 dBFS"),
				List.of("True peak"),
				List.of("Peak", "-1.5 -dBFS"));

		s.setRawLines(rawSummaryZone);
	}

	@Test
	void testExtractValue_0() {
		final var oF = extractValue(List.of(faker.numerify("key###"), " 0.0 LUFS"));
		assertNotNull(oF);
		assertTrue(oF.isPresent());
		assertEquals(0F, oF.get());
	}

	@Test
	void testExtractValue_positiveFloat() {
		final var oF = extractValue(List.of(faker.numerify("key###"), "22.3 CACA"));
		assertNotNull(oF);
		assertTrue(oF.isPresent());
		assertEquals(22.3f, oF.get());
	}

	@Test
	void testExtractValue_negativeFloat() {
		final var oF = extractValue(List.of(faker.numerify("key###"), "-256.6  CACA"));
		assertNotNull(oF);
		assertTrue(oF.isPresent());
		assertEquals(-256.6f, oF.get());
	}

	@Test
	void testExtractValue_negativeInt() {
		final var v = faker.random().nextInt();
		final var oF = extractValue(List.of(faker.numerify("key###"), v + " CACA"));
		assertNotNull(oF);
		assertTrue(oF.isPresent());
		assertEquals(v, oF.get());
	}

	@Test
	void testExtractValue_negInf() {
		final var oF = extractValue(List.of(faker.numerify("key###"), "-inf CACA"));
		assertNotNull(oF);
		assertFalse(oF.isPresent());
	}

	@Test
	void testGetIntegrated() {
		assertEquals(integrated, s.getIntegrated());
	}

	@Test
	void testGetIntegratedThreshold() {
		assertEquals(integratedThreshold, s.getIntegratedThreshold());
	}

	@Test
	void testGetLoudnessRange() {
		assertEquals(loudnessRange, s.getLoudnessRange());
	}

	@Test
	void testGetLoudnessRangeThreshold() {
		assertEquals(loudnessRangeThreshold, s.getLoudnessRangeThreshold());
	}

	@Test
	void testGetLoudnessRangeLow() {
		assertEquals(loudnessRangeLow, s.getLoudnessRangeLow());
	}

	@Test
	void testGetLoudnessRangeHigh() {
		assertEquals(loudnessRangeHigh, s.getLoudnessRangeHigh());
	}

	@Test
	void testGetSamplePeak() {
		assertEquals(samplePeak, s.getSamplePeak());
	}

	@Test
	void testGetTruePeak() {
		assertEquals(truePeak, s.getTruePeak());
	}

	@Test
	void testIsEmpty_empty() {
		assertTrue(new Ebur128Summary().isEmpty());
	}

	@Test
	void testIsEmpty_full() {
		assertFalse(s.isEmpty());
	}

	private void checkPartialEmpty(final String... values) {
		s = new Ebur128Summary();
		s.setRawLines(List.of(
				List.of(values)));
		assertFalse(s.isEmpty());
	}

	@Test
	void testIsEmpty_partial() {
		checkPartialEmpty("I", "-17.6 LUFS");
		checkPartialEmpty("Threshold", "-28.2 LUFS");
		checkPartialEmpty("LRA", "6.5 LU");
		checkPartialEmpty("Threshold", "-38.2 LUFS");
		checkPartialEmpty("LRA low", "-21.6 LUFS");
		checkPartialEmpty("LRA high", "-15.1 LUFS");
		checkPartialEmpty("Peak", "-1.5 -dBFS");
	}

}
