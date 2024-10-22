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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static tv.hd3g.fflauncher.TemporalProcessTraits.ffmpegDurationToDuration;
import static tv.hd3g.fflauncher.TemporalProcessTraits.positionToFFmpegPosition;

import java.time.Duration;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.datafaker.Faker;

class TemporalProcessTraitsTest {
	static Faker faker = net.datafaker.Faker.instance();

	FFmpeg ffmpeg;
	String value;
	Duration duration;

	@BeforeEach
	void init() {
		value = faker.numerify("value###");
		duration = Duration.ofSeconds(3 * 3600 + 2 * 60 + 1);

		ffmpeg = new FFmpeg("ffmpeg");
	}

	@Test
	void testPositionToFFmpegPosition() {
		assertEquals("-00:00:00", positionToFFmpegPosition(Duration.ofDays(-1)));
		assertEquals("10:00:00", positionToFFmpegPosition(Duration.ofHours(10)));
		assertEquals("03:25:45", positionToFFmpegPosition(Duration.ofSeconds(12345)));
		assertEquals("00:00:12.345", positionToFFmpegPosition(Duration.ofMillis(12345)));
		assertEquals("-00:00:12.345", positionToFFmpegPosition(Duration.ofMillis(-12345)));
	}

	@Test
	void testFFmpegDurationToDuration() {
		assertThat(ffmpegDurationToDuration("")).hasMillis(0);
		assertThat(ffmpegDurationToDuration("00:01:02.003")).hasMillis(62003);
		assertThat(ffmpegDurationToDuration("00:01:02")).hasMillis(62000);
		assertThat(ffmpegDurationToDuration("00:02")).hasSeconds(120);
		assertThat(ffmpegDurationToDuration("03:02")).hasSeconds(3600 * 3 + 60 * 2);

		assertThat(ffmpegDurationToDuration("2")).hasSeconds(2);
		assertThat(ffmpegDurationToDuration("1.234")).hasMillis(1234);
		assertThat(ffmpegDurationToDuration("2s")).hasSeconds(2);
		assertThat(ffmpegDurationToDuration("1.234s")).hasMillis(1234);

		assertThat(ffmpegDurationToDuration("2ms")).hasMillis(2);
		assertThat(ffmpegDurationToDuration("1.234ms")).hasNanos(1_234_000);

		assertThat(ffmpegDurationToDuration("2us")).hasNanos(2_000);
		assertThat(ffmpegDurationToDuration("1.234us")).hasNanos(1_000);

		assertThat(ffmpegDurationToDuration("-12345.678901s")).hasNanos(-12345678901000l);
	}

	@Test
	void testFuzzyFFmpegDuration() {
		Stream.of(Duration.ofDays(1).minusSeconds(1),
				Duration.ofHours(1),
				Duration.ofSeconds(1))
				.map(Duration::toMillis)
				.forEach(maxDuration -> {
					IntStream.range(0, 100)
							.mapToLong(i -> faker.random().nextLong(-maxDuration, maxDuration))
							.mapToObj(Duration::ofMillis)
							.forEach(pos -> {
								final var ffDuration = TemporalProcessTraits.positionToFFmpegPosition(pos);
								final var d = TemporalProcessTraits.ffmpegDurationToDuration(ffDuration);
								assertEquals(pos, d, "Invalid with: " + ffDuration);
							});
				});
	}

	@Test
	void testAddDurationString() {
		ffmpeg.addDuration(value);
		assertEquals("-t " + value, ffmpeg.getInternalParameters().toString());
	}

	@Test
	void testAddDuration() {
		ffmpeg.addDuration(duration);
		assertEquals("-t 03:02:01", ffmpeg.getInternalParameters().toString());
	}

	@Test
	void testAddToDurationString() {
		ffmpeg.addToDuration(value);
		assertEquals("-to " + value, ffmpeg.getInternalParameters().toString());
	}

	@Test
	void testAddToDuration() {
		ffmpeg.addToDuration(duration);
		assertEquals("-to 03:02:01", ffmpeg.getInternalParameters().toString());
	}

	@Test
	void testAddEndPositionString() {
		ffmpeg.addEndPosition(value);
		assertEquals("-sseof " + value, ffmpeg.getInternalParameters().toString());
	}

	@Test
	void testAddEndPosition() {
		ffmpeg.addEndPosition(duration);
		assertEquals("-sseof 03:02:01", ffmpeg.getInternalParameters().toString());
	}
}
