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
import tv.hd3g.fflauncher.filtering.VideoFilterMEstimate.Method;

class VideoFilterMEstimateTest {
	static Faker faker = instance();

	VideoFilterMEstimate f;
	Method method;

	@BeforeEach
	void init() throws Exception {
		method = faker.options().option(Method.class);
		f = new VideoFilterMEstimate();
	}

	@Test
	void testToFilter() {
		assertEquals("mestimate", f.toFilter().toString());
		f.setMethod(method);
		assertEquals("mestimate=method=" + method.toString(), f.toFilter().toString());
	}

}
