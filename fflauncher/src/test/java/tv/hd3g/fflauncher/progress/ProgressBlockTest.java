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
package tv.hd3g.fflauncher.progress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProgressBlockTest {

	ProgressBlock bSimpleLines;
	ProgressBlock bFullLines;
	ProgressBlock bDual;

	@BeforeEach
	void init() {
		bSimpleLines = new ProgressBlock(
				List.of(
						"bitrate=1234",
						"total_size=4567",
						"out_time_us=1582993",
						"out_time_ms=1582993",
						"out_time=00:00:01.582993",
						"dup_frames=2",
						"drop_frames=5",
						"speed=1.58x",
						"progress=continue"));
		bFullLines = new ProgressBlock(
				List.of(
						"frame=619",
						"fps=30.39",
						"stream_0_0_q=-0.0",
						"bitrate=N/A",
						"total_size=N/A",
						"out_time_us=20633313",
						"out_time_ms=20633313",
						"out_time=00:00:20.633313",
						"dup_frames=0",
						"drop_frames=0",
						"speed=1.01",
						"progress=end"));
	}

	@Test
	void testIsEnd() {
		assertFalse(bSimpleLines.isEnd());
		assertTrue(bFullLines.isEnd());
	}

	@Test
	void testGetFrame() {
		assertEquals(Optional.empty(), bSimpleLines.getFrame());
		assertEquals(Optional.ofNullable(619), bFullLines.getFrame());
	}

	@Test
	void testGetFPS() {
		assertEquals(Optional.empty(), bSimpleLines.getFPS());
		assertEquals(Optional.ofNullable(30.39f), bFullLines.getFPS());
	}

	@Test
	void testGetBitrate() {
		assertEquals(Optional.ofNullable(1234f), bSimpleLines.getBitrate());
		assertEquals(Optional.empty(), bFullLines.getBitrate());
	}

	@Test
	void testGetTotalSize() {
		assertEquals(Optional.ofNullable(4567l), bSimpleLines.getTotalSize());
		assertEquals(Optional.empty(), bFullLines.getTotalSize());
	}

	@Test
	void testGetDupFrames() {
		assertEquals(2, bSimpleLines.getDupFrames());
		assertEquals(0, bFullLines.getDupFrames());
	}

	@Test
	void testGetDropFrames() {
		assertEquals(5, bSimpleLines.getDropFrames());
		assertEquals(0, bFullLines.getDropFrames());
	}

	@Test
	void testGetSpeedX() {
		assertEquals(1.58f, bSimpleLines.getSpeedX());
		assertEquals(1.01f, bFullLines.getSpeedX());
	}

	@Test
	void testGetOutTimeUs() {
		assertEquals(1582993, bSimpleLines.getOutTimeUs());
		assertEquals(20633313, bFullLines.getOutTimeUs());
	}

	@Test
	void testGetOutTimeMs() {
		assertEquals(Duration.ofMillis(26 * 60 * 1000 + 22 * 1000 + 993), bSimpleLines.getOutTimeMs());
		assertEquals(Duration.ofMillis(5 * 3600 * 1000 + 43 * 60 * 1000 + 53 * 1000 + 313), bFullLines.getOutTimeMs());
	}

	@Test
	void testGetOutTime() {
		assertEquals("00:00:01.582993", bSimpleLines.getOutTime());
		assertEquals("00:00:20.633313", bFullLines.getOutTime());
	}

	@Test
	void testGetStreamQ() {
		assertEquals(Map.of(), bSimpleLines.getStreamQ());
		assertEquals(Map.of("0_0", -0.0f), bFullLines.getStreamQ());
	}

	@Test
	void testToString() {
		assertNotNull(bSimpleLines.toString());
		assertNotNull(bFullLines.toString());
	}

	@Test
	void testMultipleBlocks() {
		bDual = new ProgressBlock(List.of(
				"dup_frames=1",
				"progress=continue",
				"dup_frames=2",
				"progress=continue",
				"dup_frames=3",
				"progress=continue"));
		assertEquals(3, bDual.getDupFrames());
		assertFalse(bDual.isEnd());
	}

	@Test
	void testMultipleBlocks_withEnd() {
		bDual = new ProgressBlock(List.of(
				"dup_frames=1",
				"progress=continue",
				"dup_frames=2",
				"progress=continue",
				"dup_frames=3",
				"progress=end"));
		assertEquals(3, bDual.getDupFrames());
		assertTrue(bDual.isEnd());
	}

	@Test
	void testBadBlocks_left() {
		final var lines = List.of(
				"dup_frames",
				"progress=continue");
		assertThrows(IllegalArgumentException.class, () -> new ProgressBlock(lines));
	}

	@Test
	void testBadBlocks_right() {
		final var lines = List.of(
				"dup_frames=",
				"progress=continue");
		assertThrows(IllegalArgumentException.class, () -> new ProgressBlock(lines));
	}

}
