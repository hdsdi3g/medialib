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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class FilterParserTest {

	@Nested
	class GetUnescapeAndUnQuoted {

		@Test
		void testGetUnescapeAndUnQuoted_simple() {
			final var p = new FilterParser("abc");
			final var cleaned = p.getUnescapeAndUnQuoted();
			assertNotNull(p);
			assertEquals(3, cleaned.size());
			var i = 0;
			assertEquals("a", cleaned.get(i++).toString());
			assertEquals("b", cleaned.get(i++).toString());
			assertEquals("c", cleaned.get(i++).toString());
		}

		@Test
		void testGetUnescapeAndUnQuoted_spaces() {
			final var p = new FilterParser(" a  b c ");
			final var cleaned = p.getUnescapeAndUnQuoted();
			assertNotNull(p);
			var i = 0;
			assertEquals(3, cleaned.size());
			assertEquals("a", cleaned.get(i++).toString());
			assertEquals("b", cleaned.get(i++).toString());
			assertEquals("c", cleaned.get(i++).toString());
		}

		@Test
		void testGetUnescapeAndUnQuoted_specials() {
			final var p = new FilterParser(" ] [a : b, ;c ");
			final var cleaned = p.getUnescapeAndUnQuoted();
			assertNotNull(p);
			assertEquals(8, cleaned.size());
			var i = 0;
			assertEquals("]", cleaned.get(i++).toString());
			assertEquals("[", cleaned.get(i++).toString());
			assertEquals("a", cleaned.get(i++).toString());
			assertEquals(":", cleaned.get(i++).toString());
			assertEquals("b", cleaned.get(i++).toString());
			assertEquals(",", cleaned.get(i++).toString());
			assertEquals(";", cleaned.get(i++).toString());
			assertEquals("c", cleaned.get(i++).toString());
		}

		@ParameterizedTest
		@ValueSource(strings = { "a 'd ee f ' b 'c'", "a 'd \\ee f ' b 'c'" })
		void testGetUnescapeAndUnQuoted_quotes(final String value) {
			final var p = new FilterParser(value);
			final var cleaned = p.getUnescapeAndUnQuoted();
			assertNotNull(p);
			assertEquals(4, cleaned.size());
			var i = 0;
			assertEquals("a", cleaned.get(i++).toString());
			assertEquals("'d ee f '", cleaned.get(i++).toString());
			assertEquals("b", cleaned.get(i++).toString());
			assertEquals("'c'", cleaned.get(i++).toString());
		}

		@Test
		void testGetUnescapeAndUnQuoted_escape() {
			final var p = new FilterParser("a\\'b\\c");
			final var cleaned = p.getUnescapeAndUnQuoted();
			assertNotNull(p);
			assertEquals(4, cleaned.size());
			var i = 0;
			assertEquals("a", cleaned.get(i++).toString());
			assertEquals("\\'", cleaned.get(i++).toString());
			assertEquals("b", cleaned.get(i++).toString());
			assertEquals("\\c", cleaned.get(i++).toString());
		}

	}

	@Nested
	class RawGraph {

		@Test
		void testGetRawGraph() {
			final var p = new FilterParser("aa;bb ;ccc ; dd e");
			final var list = p.getGraphBranchs();
			assertNotNull(list);
			assertEquals(4, list.size());
			var i = 0;
			assertEquals("aa", list.get(i++).toString());
			assertEquals("bb", list.get(i++).toString());
			assertEquals("ccc", list.get(i++).toString());
			assertEquals("dde", list.get(i++).toString());
		}

		@Test
		void testGetRawGraph_2() {
			final var p = new FilterParser(";aa;bb");
			final var list = p.getGraphBranchs();
			assertNotNull(list);
			assertEquals(2, list.size());
			var i = 0;
			assertEquals("aa", list.get(i++).toString());
			assertEquals("bb", list.get(i++).toString());
		}

		@Test
		void testGetRawGraph_3() {
			final var p = new FilterParser("aa;bb;");
			final var list = p.getGraphBranchs();
			assertNotNull(list);
			assertEquals(2, list.size());
			var i = 0;
			assertEquals("aa", list.get(i++).toString());
			assertEquals("bb", list.get(i++).toString());
		}

		@Test
		void testGetRawGraph_4() {
			final var p = new FilterParser("aa");
			final var list = p.getGraphBranchs();
			assertNotNull(list);
			assertEquals(1, list.size());
			assertEquals("aa", list.get(0).toString());
		}

	}

}
