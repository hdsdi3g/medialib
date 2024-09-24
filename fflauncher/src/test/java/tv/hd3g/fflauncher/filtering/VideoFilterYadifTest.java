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

class VideoFilterYadifTest {

	VideoFilterYadif f;

	@BeforeEach
	void init() {
		f = new VideoFilterYadif();
	}

	@Test
	void testToFilter() {
		assertEquals("yadif", f.toFilter().toString());
		f.setMode(VideoFilterYadif.Mode.SEND_FIELD_NO_SPATIAL);
		assertEquals("yadif=mode=send_field_nospatial", f.toFilter().toString());
		f.setParity(VideoFilterYadif.Parity.BFF);
		assertEquals("yadif=mode=send_field_nospatial:parity=bff", f.toFilter().toString());
		f.setDeint(VideoFilterYadif.Deint.INTERLACED);
		assertEquals("yadif=mode=send_field_nospatial:parity=bff:deint=interlaced", f.toFilter().toString());
	}
}
