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
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.datafaker.Faker;

class RawStdErrFilterEventTest {

	static Faker faker = instance();

	String filterName;
	int filterChainPos;

	RawStdErrFilterEvent fe;

	@BeforeEach
	void init() {
		filterName = faker.bothify("?#?#???#??#???#??");
		filterChainPos = Math.abs(faker.random().nextInt());

		fe = new RawStdErrFilterEvent(
				"[Parsed_" + filterName
									  + "_" + filterChainPos
									  + " @ " + faker.random().hex(12)
									  + "] t: 1.80748    TARGET:-23 LUFS    M: -25.5 S:-120.7     I: -19.2 LUFS       LRA:   0.0 LU  SPK:  -5.5  -5.6 dBFS  FTPK: -19.1 -21.6 dBFS  TPK:  -5.5  -5.6 dBFS");
	}

	@Test
	void testGetFilterName() {
		assertEquals(filterName, fe.getFilterName());
	}

	@Test
	void testGetFilterChainPos() {
		assertEquals(filterChainPos, fe.getFilterChainPos());
	}

	@Test
	void testGetContent() {
		assertEquals(
				"t: 1.80748    TARGET:-23 LUFS    M: -25.5 S:-120.7     I: -19.2 LUFS       LRA:   0.0 LU  SPK:  -5.5  -5.6 dBFS  FTPK: -19.1 -21.6 dBFS  TPK:  -5.5  -5.6 dBFS",
				fe.getLineValue());
	}

	@Test
	void testInvalidLine_missingAt() {
		assertThrows(IllegalArgumentException.class,
				() -> new RawStdErrFilterEvent("[Parsed_ebur128_0 0x55c6a78d7580]"));
	}

	@Test
	void testInvalidLine_missingHook() {
		assertThrows(IllegalArgumentException.class,
				() -> new RawStdErrFilterEvent("[Parsed_ebur128_0 @ 0x55c6a78d7580"));
	}

}
