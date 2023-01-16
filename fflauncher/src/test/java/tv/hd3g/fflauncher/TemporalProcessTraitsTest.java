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
package tv.hd3g.fflauncher;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.datafaker.Faker;
import tv.hd3g.processlauncher.cmdline.Parameters;

class TemporalProcessTraitsTest {

	static Faker faker = net.datafaker.Faker.instance();

	FFmpeg ffmpeg;
	String value;

	@BeforeEach
	void init() {
		value = faker.numerify("value###");

		ffmpeg = new FFmpeg("ffmpeg", new Parameters());
	}

	@Test
	void testAddDuration() {
		ffmpeg.addDuration(value);
		assertEquals("-t " + value, ffmpeg.getInternalParameters().toString());
	}

	@Test
	void testAddToDuration() {
		ffmpeg.addToDuration(value);
		assertEquals("-to " + value, ffmpeg.getInternalParameters().toString());
	}

	@Test
	void testAddStartPosition() {
		ffmpeg.addStartPosition(value);
		assertEquals("-ss " + value, ffmpeg.getInternalParameters().toString());
	}

	@Test
	void testAddEndPosition() {
		ffmpeg.addEndPosition(value);
		assertEquals("-sseof " + value, ffmpeg.getInternalParameters().toString());
	}
}
