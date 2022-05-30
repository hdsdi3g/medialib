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

import org.junit.jupiter.api.Test;

class FilterParserBaseChainFilterTest {

	@Test
	void testGetContent() {
		final var rfc = new FilterParserBaseChainFilter("ab");
		final var c = rfc.getContent();
		assertNotNull(c);
		assertEquals("a", c.get(0).toString());
		assertEquals("b", c.get(1).toString());
	}

	@Test
	void testToString() {
		assertEquals("abc", new FilterParserBaseChainFilter("abc").toString());
	}

}
