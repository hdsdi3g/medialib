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
 * Copyright (C) hdsdi3g for hd3g.tv 2023
 *
 */
package tv.hd3g.fflauncher.filtering.lavfimtd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static tv.hd3g.fflauncher.filtering.lavfimtd.LavfiMtdEvent.secFloatToDuration;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import net.datafaker.Faker;

class LavfiMtdEventTest {
	static Faker faker = net.datafaker.Faker.instance();

	String name;
	String scope;

	float lStart;
	float rStart;

	@Test
	void testCompareTo() {
		name = faker.numerify("name###");
		scope = faker.numerify("scope###");

		final var max = faker.random().nextInt(1_000, 10_000);
		final var list = new ArrayList<LavfiMtdEvent>(max);
		for (var pos = 0; pos < max; pos++) {
			list.add(new LavfiMtdEvent(name, scope,
					Math.abs(faker.random().nextFloat()
							 * (float) faker.random().nextInt(1_000, 1_000_000)),
					faker.random().nextFloat()));
		}
		Collections.shuffle(list, faker.random().getRandomInternal());

		final var sorted = list.stream().sorted().toList();
		LavfiMtdEvent previous = null;
		for (var pos = 0; pos < sorted.size(); pos++) {
			if (previous == null) {
				previous = sorted.get(pos);
				continue;
			}
			final var prev = previous.start().toMillis();
			final var actual = sorted.get(pos).start().toMillis();
			assertTrue(prev < actual, "pos:" + pos + " prev:" + prev + " actual:" + actual);
			previous = sorted.get(pos - 1);
		}
	}

	@Test
	void testSecFloatToDuration() {
		assertEquals(1000, secFloatToDuration(1).toMillis());
		assertEquals(10000, secFloatToDuration(10).toMillis());
		assertEquals(100, secFloatToDuration(0.1f).toMillis());
		assertEquals(10, secFloatToDuration(0.01f).toMillis());
	}

	@Test
	void testGetEndOr_default() {
		final var d = Mockito.mock(Duration.class);
		assertEquals(d, new LavfiMtdEvent(name, scope, 1f, 0).getEndOr(d));
		verifyNoInteractions(d);
	}

	@Test
	void testGetEndOr_value() {
		final var d = Mockito.mock(Duration.class);
		assertEquals(1000,
				new LavfiMtdEvent(name, scope, 1f, 1000f).getEndOr(d).toSeconds());
		verifyNoInteractions(d);
	}

}
