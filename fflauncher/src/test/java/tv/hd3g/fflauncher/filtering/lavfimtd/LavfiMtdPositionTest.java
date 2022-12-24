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
package tv.hd3g.fflauncher.filtering.lavfimtd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import net.datafaker.Faker;

class LavfiMtdPositionTest {
	static Faker faker = net.datafaker.Faker.instance();

	int lFrame;
	int rFrame;

	@Test
	void testCompareTo() {
		final var max = faker.random().nextInt(1_000, 10_000);
		for (var pos = 0; pos < max; pos++) {
			lFrame = faker.random().nextInt(0, 100_000_000);
			final var l = new LavfiMtdPosition(lFrame, faker.random().nextLong(), faker.random().nextFloat());

			rFrame = faker.random().nextInt(0, 100_000_000);
			final var r = new LavfiMtdPosition(rFrame, faker.random().nextLong(), faker.random().nextFloat());

			assertEquals(Integer.compare(lFrame, rFrame), l.compareTo(r));
		}

		assertEquals(Integer.compare(0, 0),
				new LavfiMtdPosition(0, 0, 0)
						.compareTo(new LavfiMtdPosition(0, 0, 0)));
	}

}
