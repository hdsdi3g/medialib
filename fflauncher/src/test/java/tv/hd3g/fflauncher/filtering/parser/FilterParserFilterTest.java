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
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.Test;

class FilterParserFilterTest {

	@Test
	void testGetFilterName() {
		var f = new FilterParserFilter("aa");
		assertEquals("aa", f.getFilterName());

		f = new FilterParserFilter("aa=bb");
		assertEquals("aa", f.getFilterName());

		f = new FilterParserFilter("yo=aa=bbb:cc=dd");
		assertEquals("yo", f.getFilterName());
	}

	@Test
	void testGetFilterSetup() {
		var fs = new FilterParserFilter("aa").getFilterArguments();
		assertNotNull(fs);
		assertEquals(0, fs.size());

		fs = new FilterParserFilter("yo=aa=bbb:cc=dd").getFilterArguments();
		assertEquals(2, fs.size());
		assertEquals("aa=bbb", fs.get(0).toString());
		assertEquals("cc=dd", fs.get(1).toString());
	}

	@Test
	void testGetSetupKV() {
		var eP = FilterParserFilter.getSetupKV(List.of(new FilterParserChars("a")));
		assertEquals("a", eP.getKey());
		assertNull(eP.getValue());

		eP = FilterParserFilter.getSetupKV(List.of(
		        new FilterParserChars("a"),
		        new FilterParserChars("b"),
		        new FilterParserChars("="),
		        new FilterParserChars("c"),
		        new FilterParserChars("d")));
		assertEquals("ab", eP.getKey());
		assertEquals("cd", eP.getValue());

		eP = FilterParserFilter.getSetupKV(List.of(
		        new FilterParserChars("="),
		        new FilterParserChars("b")));
		assertEquals("b", eP.getKey());
		assertNull(eP.getValue());
	}
}
