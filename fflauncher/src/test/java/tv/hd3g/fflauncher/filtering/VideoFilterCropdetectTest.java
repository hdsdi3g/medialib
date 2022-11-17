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
 * Copyright (C) hdsdi3g for hd3g.tv 2022
 *
 */
package tv.hd3g.fflauncher.filtering;

import static net.datafaker.Faker.instance;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.datafaker.Faker;
import tv.hd3g.fflauncher.filtering.VideoFilterCropdetect.Mode;

class VideoFilterCropdetectTest {
	static Faker faker = instance();

	VideoFilterCropdetect f;
	Mode mode;

	@BeforeEach
	void init() throws Exception {
		mode = faker.options().option(Mode.class);
		f = new VideoFilterCropdetect(mode);
	}

	@Test
	void testToFilter() {
		assertEquals("cropdetect=mode=" + mode.toString(), f.toFilter().toString());
	}

}
