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

class FilterParserCharsTest {

	@Test
	void testFilterCharsChar() {
		final var fc = new FilterParserChars('a');
		assertEquals("a", fc.toString());
		assertFalse(fc.isFromEscaped());
	}

	@Test
	void testFilterCharsCharSequence() {
		final var fc = new FilterParserChars("a b c");
		assertEquals("a b c", fc.toString());
		assertFalse(fc.isFromEscaped());
	}

	@Test
	void testFilterCharsChar_escaped() {
		var fc = new FilterParserChars('a', true);
		assertEquals("\\a", fc.toString());
		assertTrue(fc.isFromEscaped());

		fc = new FilterParserChars('a', false);
		assertEquals("a", fc.toString());
		assertFalse(fc.isFromEscaped());
	}

	@Test
	void testWrite() {
		final var fc = new FilterParserChars("abc");
		final var sb = new StringBuilder();
		fc.write(sb);
		assertEquals("abc", sb.toString());
	}

	@Test
	void testEscapedCondition() {
		var fc = new FilterParserChars(',', true);
		assertFalse(fc.isComma());

		fc = new FilterParserChars(']', true);
		assertFalse(fc.isBracketClose());

		fc = new FilterParserChars('[', true);
		assertFalse(fc.isBracketOpen());

		fc = new FilterParserChars(',', true);
		assertFalse(fc.isColon());

		fc = new FilterParserChars(';', true);
		assertFalse(fc.isSemicolon());

		fc = new FilterParserChars('=', true);
		assertFalse(fc.isEquals());
	}

	@Test
	void testIsComma() {
		var fc = new FilterParserChars(",abc");
		assertTrue(fc.isComma());
		assertFalse(fc.isBracketClose());
		assertFalse(fc.isBracketOpen());
		assertFalse(fc.isColon());
		assertFalse(fc.isSemicolon());
		assertFalse(fc.isEquals());

		fc = new FilterParserChars(',');
		assertTrue(fc.isComma());
		assertFalse(fc.isBracketClose());
		assertFalse(fc.isBracketOpen());
		assertFalse(fc.isColon());
		assertFalse(fc.isSemicolon());
		assertFalse(fc.isEquals());
	}

	@Test
	void testIsColon() {
		var fc = new FilterParserChars(":abc");
		assertFalse(fc.isComma());
		assertFalse(fc.isBracketClose());
		assertFalse(fc.isBracketOpen());
		assertTrue(fc.isColon());
		assertFalse(fc.isSemicolon());
		assertFalse(fc.isEquals());

		fc = new FilterParserChars(':');
		assertFalse(fc.isComma());
		assertFalse(fc.isBracketClose());
		assertFalse(fc.isBracketOpen());
		assertTrue(fc.isColon());
		assertFalse(fc.isSemicolon());
		assertFalse(fc.isEquals());
	}

	@Test
	void testIsSemicolon() {
		var fc = new FilterParserChars(";abc");
		assertFalse(fc.isComma());
		assertFalse(fc.isBracketClose());
		assertFalse(fc.isBracketOpen());
		assertFalse(fc.isColon());
		assertTrue(fc.isSemicolon());
		assertFalse(fc.isEquals());

		fc = new FilterParserChars(';');
		assertFalse(fc.isComma());
		assertFalse(fc.isBracketClose());
		assertFalse(fc.isBracketOpen());
		assertFalse(fc.isColon());
		assertTrue(fc.isSemicolon());
		assertFalse(fc.isEquals());
	}

	@Test
	void testIsBracketOpen() {
		var fc = new FilterParserChars("[abc");
		assertFalse(fc.isComma());
		assertFalse(fc.isBracketClose());
		assertTrue(fc.isBracketOpen());
		assertFalse(fc.isColon());
		assertFalse(fc.isSemicolon());
		assertFalse(fc.isEquals());

		fc = new FilterParserChars('[');
		assertFalse(fc.isComma());
		assertFalse(fc.isBracketClose());
		assertTrue(fc.isBracketOpen());
		assertFalse(fc.isColon());
		assertFalse(fc.isSemicolon());
		assertFalse(fc.isEquals());

	}

	@Test
	void testIsBracketClose() {
		var fc = new FilterParserChars("]abc");
		assertFalse(fc.isComma());
		assertTrue(fc.isBracketClose());
		assertFalse(fc.isBracketOpen());
		assertFalse(fc.isColon());
		assertFalse(fc.isSemicolon());
		assertFalse(fc.isEquals());

		fc = new FilterParserChars(']');
		assertFalse(fc.isComma());
		assertTrue(fc.isBracketClose());
		assertFalse(fc.isBracketOpen());
		assertFalse(fc.isColon());
		assertFalse(fc.isSemicolon());
		assertFalse(fc.isEquals());
	}

	@Test
	void testIsEquals() {
		var fc = new FilterParserChars("=abc");
		assertFalse(fc.isComma());
		assertFalse(fc.isBracketClose());
		assertFalse(fc.isBracketOpen());
		assertFalse(fc.isColon());
		assertFalse(fc.isSemicolon());
		assertTrue(fc.isEquals());

		fc = new FilterParserChars('=');
		assertFalse(fc.isComma());
		assertFalse(fc.isBracketClose());
		assertFalse(fc.isBracketOpen());
		assertFalse(fc.isColon());
		assertFalse(fc.isSemicolon());
		assertTrue(fc.isEquals());
	}

}
