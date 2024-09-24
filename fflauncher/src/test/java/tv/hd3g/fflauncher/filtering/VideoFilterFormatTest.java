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
package tv.hd3g.fflauncher.filtering;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import tv.hd3g.commons.testtools.MockToolsExtendsJunit;

@ExtendWith(MockToolsExtendsJunit.class)
class VideoFilterFormatTest {

	VideoFilterFormat f;

	@BeforeEach
	void init() {
		f = new VideoFilterFormat();
	}

	@Test
	void testToFilter() {
		assertEquals("format", f.toFilter().toString());
		f.setPixFmts(List.of("color0"));
		f.setColorSpaces(List.of("color1"));
		f.setColorRanges(List.of("color2"));
		assertEquals("format=pix_fmts=color0:color_spaces=color1:color_ranges=color2", f.toFilter().toString());
	}
}
