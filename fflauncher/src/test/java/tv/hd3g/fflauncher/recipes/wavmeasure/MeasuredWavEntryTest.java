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
 * Copyright (C) hdsdi3g for hd3g.tv 2024
 *
 */
package tv.hd3g.fflauncher.recipes.wavmeasure;

import static java.lang.Math.round;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MeasuredWavEntryTest {

	float position;
	double peakPositive;
	double peakNegative;
	double rmsPositive;
	double rmsNegative;

	MeasuredWavEntry m;

	@BeforeEach
	void init() {
		position = 1f;
		peakPositive = 0d;
		peakNegative = 0.5d;
		rmsPositive = 0.25d;
		rmsNegative = 0.001d;
		m = new MeasuredWavEntry(position, peakPositive, peakNegative, rmsPositive, rmsNegative);
	}

	@Test
	void testGetPeakPositiveDb() {
		assertEquals(-90d, round(m.getPeakPositiveDb()));
	}

	@Test
	void testGetPeakNegativeDb() {
		assertEquals(-6d, round(m.getPeakNegativeDb()));
	}

	@Test
	void testGetRmsPositiveDb() {
		assertEquals(-12, round(m.getRmsPositiveDb()));
	}

	@Test
	void testGetRmsNegativeDb() {
		assertEquals(-60d, round(m.getRmsNegativeDb()));
	}

	@Test
	void testToString() {
		assertEquals("1.00s peak=-90.31/-6.02 rms=-12.04/-60.00", m.toString());
	}

}
