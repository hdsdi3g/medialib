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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VideoFilterSetSARTest {

	VideoFilterSetSAR f;

	@BeforeEach
	void init() {
		f = new VideoFilterSetSAR();
	}

	@Test
	void testToFilter() {
		assertEquals("setsar", f.toFilter().toString());
		f.setRatio("value");
		assertEquals("setsar=ratio=value", f.toFilter().toString());
	}
}
