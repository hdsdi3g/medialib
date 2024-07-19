/*
 * This file is part of processlauncher.
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
 * Copyright (C) hdsdi3g for hd3g.tv 2019
 *
 */
package tv.hd3g.processlauncher.cmdline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ParameterArgTest {

	private ParameterArg pArg;

	@BeforeEach
	void setUp() {
		pArg = new ParameterArg(true);
		pArg.add('a');
		pArg.add('b');
		pArg.add('c');
	}

	@Test
	void testToString() {
		assertEquals("abc", pArg.toString());
	}

	@Test
	void testIsInQuotes() {
		assertTrue(pArg.isInQuotes());
	}

	@Test
	void testIsEmpty() {
		assertFalse(pArg.isEmpty());
	}
}
