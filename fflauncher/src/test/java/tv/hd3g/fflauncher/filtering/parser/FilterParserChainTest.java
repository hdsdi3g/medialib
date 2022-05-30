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
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class FilterParserChainTest {

	@Test
	void testGetSourceBlocks() {
		var rf = new FilterParserChain("ab");
		assertEquals("[]", rf.getSourceBlocks().toString());

		rf = new FilterParserChain("ab[c]");
		assertEquals("[]", rf.getSourceBlocks().toString());

		rf = new FilterParserChain("[a]bc");
		assertEquals("[a]", rf.getSourceBlocks().toString());

		rf = new FilterParserChain("[aa][bb]c");
		assertEquals("[aa, bb]", rf.getSourceBlocks().toString());

		rf = new FilterParserChain("[aa][bb]c[d][eee]");
		assertEquals("[aa, bb]", rf.getSourceBlocks().toString());

		rf = new FilterParserChain("[a]b[");
		assertEquals("[a]", rf.getSourceBlocks().toString());

		rf = new FilterParserChain("[a]b]");
		assertEquals("[a]", rf.getSourceBlocks().toString());

		rf = new FilterParserChain("[a");
		assertEquals("[]", rf.getSourceBlocks().toString());

		rf = new FilterParserChain("a]");
		assertEquals("[]", rf.getSourceBlocks().toString());

		final var exceRfc = new FilterParserChain("]a");
		assertThrows(IllegalArgumentException.class, () -> {
			exceRfc.getSourceBlocks();
		});
	}

	@Test
	void testGetDestBlocks() {
		var rf = new FilterParserChain("ab");
		assertEquals("[]", rf.getDestBlocks().toString());

		rf = new FilterParserChain("ab[c]");
		assertEquals("[c]", rf.getDestBlocks().toString());

		rf = new FilterParserChain("[a]bc");
		assertEquals("[]", rf.getDestBlocks().toString());

		rf = new FilterParserChain("[aa][bb]c");
		assertEquals("[]", rf.getDestBlocks().toString());

		rf = new FilterParserChain("[aa][bb]c[d][eee]");
		assertEquals("[d, eee]", rf.getDestBlocks().toString());

		rf = new FilterParserChain("[a");
		assertEquals("[]", rf.getDestBlocks().toString());

		final var exceRfc = new FilterParserChain("]a");
		assertThrows(IllegalArgumentException.class, () -> {
			exceRfc.getDestBlocks();
		});

		final var exceRfc0 = new FilterParserChain("[a]b[");
		assertThrows(IllegalArgumentException.class, () -> {
			exceRfc0.getDestBlocks();
		});

		final var exceRfc1 = new FilterParserChain("[a]b]");
		assertThrows(IllegalArgumentException.class, () -> {
			exceRfc1.getDestBlocks();
		});

		final var exceRfc2 = new FilterParserChain("aaa[b]nope");
		assertThrows(IllegalArgumentException.class, () -> {
			exceRfc2.getDestBlocks();
		});
	}

	@Test
	void testGetFilterSetup() {
		var rf = new FilterParserChain("ab");
		assertEquals("ab", rf.getFilter().toString());

		rf = new FilterParserChain("ab[c]");
		assertEquals("ab", rf.getFilter().toString());

		rf = new FilterParserChain("[a]bc");
		assertEquals("bc", rf.getFilter().toString());

		rf = new FilterParserChain("[aa][bb]c");
		assertEquals("c", rf.getFilter().toString());

		rf = new FilterParserChain("[aa][bb]c[d][eee]");
		assertEquals("c", rf.getFilter().toString());

		rf = new FilterParserChain("[a]b[");
		assertEquals("b", rf.getFilter().toString());

		rf = new FilterParserChain("[a]b]");
		assertEquals("b", rf.getFilter().toString());

		final var exceRf4 = new FilterParserChain("[a");
		assertThrows(IllegalArgumentException.class, () -> {
			exceRf4.getFilter();
		});

		final var exceRf = new FilterParserChain("]a");
		assertThrows(IllegalArgumentException.class, () -> {
			exceRf.getFilter();
		});
	}

}
