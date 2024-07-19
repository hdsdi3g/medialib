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
 * Copyright (C) hdsdi3g for hd3g.tv 2023
 *
 */
package tv.hd3g.fflauncher.filtering.lavfimtd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.datafaker.Faker;

class NumberParserTraitsTest {

	static Faker faker = net.datafaker.Faker.instance();

	class T implements NumberParserTraits {
	}

	T number;
	float flt;
	long lgn;
	int intg;
	double dlb;

	@BeforeEach
	void init() {
		number = new T();
		flt = faker.random().nextFloat();
		lgn = faker.random().nextLong();
		intg = faker.random().nextInt();
		dlb = faker.random().nextDouble();
	}

	@Test
	void testStringNullOrBlank() {
		assertFalse(number.stringNullOrBlank(faker.numerify("###")));
		assertTrue(number.stringNullOrBlank(" "));
		assertTrue(number.stringNullOrBlank(""));
		assertTrue(number.stringNullOrBlank(null));
	}

	@Test
	void testParseFloat() {
		assertEquals(Float.NaN, number.parseFloat(" "));
		assertEquals(Float.NaN, number.parseFloat(""));
		assertEquals(Float.NaN, number.parseFloat(null));
		assertEquals(Float.NaN, number.parseFloat("?"));
		assertEquals(Float.NEGATIVE_INFINITY, number.parseFloat("-inf"));
		assertEquals(Float.POSITIVE_INFINITY, number.parseFloat("inf"));
		assertEquals(flt, number.parseFloat(faker.numerify(String.valueOf(flt))));
	}

	@Test
	void testParseDouble() {
		assertEquals(Double.NaN, number.parseDouble(" "));
		assertEquals(Double.NaN, number.parseDouble(""));
		assertEquals(Double.NaN, number.parseDouble(null));
		assertEquals(Double.NaN, number.parseDouble("?"));
		assertEquals(Double.NEGATIVE_INFINITY, number.parseDouble("-inf"));
		assertEquals(Double.POSITIVE_INFINITY, number.parseDouble("inf"));
		assertEquals(dlb, number.parseDouble(faker.numerify(String.valueOf(dlb))));
	}

	@Test
	void testParseInt() {
		assertFalse(number.parseInt(" ").isPresent());
		assertFalse(number.parseInt("").isPresent());
		assertFalse(number.parseInt(null).isPresent());
		assertFalse(number.parseInt("?").isPresent());
		assertEquals(Math.round(flt), number.parseInt(String.valueOf(flt)).get());
		assertEquals(intg, number.parseInt(String.valueOf(intg)).get());
	}

	@Test
	void testParseLong() {
		assertFalse(number.parseLong(" ").isPresent());
		assertFalse(number.parseLong("").isPresent());
		assertFalse(number.parseLong(null).isPresent());
		assertFalse(number.parseLong("?").isPresent());
		assertEquals(Math.round(dlb), number.parseLong(String.valueOf(dlb)).get());
		assertEquals(lgn, number.parseLong(String.valueOf(lgn)).get());
	}

	@Test
	void testParseIntOrNeg1() {
		assertEquals(-1, number.parseIntOrNeg1(" "));
		assertEquals(-1, number.parseIntOrNeg1(""));
		assertEquals(-1, number.parseIntOrNeg1(null));
		assertEquals(-1, number.parseIntOrNeg1("?"));
		assertEquals(-1, number.parseIntOrNeg1("-inf"));
		assertEquals(-1, number.parseIntOrNeg1("inf"));
		assertEquals(Math.round(flt), number.parseIntOrNeg1(String.valueOf(flt)));
		assertEquals(intg, number.parseIntOrNeg1(String.valueOf(intg)));
	}

	@Test
	void testParseLongOrNeg1() {
		assertEquals(-1, number.parseLongOrNeg1(" "));
		assertEquals(-1, number.parseLongOrNeg1(""));
		assertEquals(-1, number.parseLongOrNeg1(null));
		assertEquals(-1, number.parseLongOrNeg1("?"));
		assertEquals(-1, number.parseLongOrNeg1("-inf"));
		assertEquals(-1, number.parseLongOrNeg1("inf"));
		assertEquals(Math.round(dlb), number.parseLongOrNeg1(String.valueOf(dlb)));
		assertEquals(lgn, number.parseLongOrNeg1(String.valueOf(lgn)));
	}

	@Test
	void testParseFloatOrNeg1() {
		assertEquals(-1f, number.parseFloatOrNeg1(" "));
		assertEquals(-1f, number.parseFloatOrNeg1(""));
		assertEquals(-1f, number.parseFloatOrNeg1(null));
		assertEquals(-1f, number.parseFloatOrNeg1("?"));
		assertEquals(-1f, number.parseFloatOrNeg1("-inf"));
		assertEquals(-1f, number.parseFloatOrNeg1("inf"));
		assertEquals(flt, number.parseFloatOrNeg1(String.valueOf(flt)));
	}

}
