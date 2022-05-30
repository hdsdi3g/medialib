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
 * Copyright (C) hdsdi3g for hd3g.tv 2020
 *
 */
package tv.hd3g.fflauncher.filtering.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class FilterParserSimpleCharTest {

	@Test
	void test() {
		final var efc = new FilterParserSimpleChar('a');
		assertFalse(efc.isEscape());
		assertFalse(efc.isQuoted());
		assertFalse(efc.isSpace());

		final var sb = new StringBuilder();
		efc.write(sb);
		assertEquals("a", sb.toString());

		assertEquals("a", efc.toFilterChars().toString());
	}

	@Test
	void test_isSpace() {
		final var efc = new FilterParserSimpleChar(' ');
		assertFalse(efc.isEscape());
		assertFalse(efc.isQuoted());
		assertTrue(efc.isSpace());
	}

	@Test
	void test_isNL() {
		final var efc = new FilterParserSimpleChar('\n');
		assertFalse(efc.isEscape());
		assertFalse(efc.isQuoted());
		assertTrue(efc.isSpace());
	}

	@Test
	void test_isQuoted() {
		final var efc = new FilterParserSimpleChar('\'');
		assertFalse(efc.isEscape());
		assertTrue(efc.isQuoted());
		assertFalse(efc.isSpace());
	}

	@Test
	void test_isEscape() {
		final var efc = new FilterParserSimpleChar('\\');
		assertTrue(efc.isEscape());
		assertFalse(efc.isQuoted());
		assertFalse(efc.isSpace());
	}

	@Test
	void test_toEscapedFilterChars() {
		final var fc = new FilterParserSimpleChar('a').toEscapedFilterChars();
		assertTrue(fc.isFromEscaped());
	}

}
