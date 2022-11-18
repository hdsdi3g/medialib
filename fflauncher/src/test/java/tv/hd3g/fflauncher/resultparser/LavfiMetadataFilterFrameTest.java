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
package tv.hd3g.fflauncher.resultparser;

import static net.datafaker.Faker.instance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.datafaker.Faker;

class LavfiMetadataFilterFrameTest {

	static Faker faker = instance();

	LavfiMetadataFilterFrame f;
	int frame;
	long pts;
	float ptsTime;

	@BeforeEach
	void init() throws Exception {
		frame = faker.random().nextInt();
		pts = faker.random().nextLong();
		ptsTime = faker.random().nextFloat();
	}

	Map<String, Map<String, String>> getValues(final Stream<String> sLines) {
		f = new LavfiMetadataFilterFrame(frame, pts, ptsTime);
		f.setRawLines(sLines);
		final var result = f.getValuesByFilterKeysByFilterName();
		assertNotNull(result);
		assertFalse(result.isEmpty());

		result.forEach((k, v) -> {
			assertNotNull(k);
			assertFalse(k.isEmpty());
			assertNotNull(v);
			assertFalse(v.isEmpty());
			v.forEach((k1, v1) -> {
				assertNotNull(k1);
				assertFalse(k1.isEmpty());
				assertNotNull(v1);
				assertFalse(v1.isEmpty());
			});
		});

		return result;
	}

	@Test
	void test_empty() {
		final var empty = Stream.of("");
		assertThrows(IllegalArgumentException.class, () -> getValues(empty));
	}

	@Test
	void test0() {
		final var v = getValues(Stream.of(
				"lavfi.aphasemeter.phase=1.000000",
				"lavfi.astats.1.DC_offset=0.000001",
				"lavfi.aphasemeter.mono_end=25.981",
				"lavfi.filter name.filter Key=filter value"));
		final var aphasemeter = v.get("aphasemeter");
		assertNotNull(aphasemeter);
		assertEquals("1.000000", aphasemeter.get("phase"));
		assertEquals("25.981", aphasemeter.get("mono_end"));

		final var astats = v.get("astats");
		assertNotNull(astats);
		assertEquals("0.000001", astats.get("1.DC_offset"));

		final var filtername = v.get("filter name");
		assertNotNull(filtername);
		assertEquals("filter value", filtername.get("filter Key"));
	}

}
