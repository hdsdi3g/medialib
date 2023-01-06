/*
 * This file is part of ffprobejaxb.
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
 * Copyright (C) hdsdi3g for hd3g.tv 2020
 *
 */
package tv.hd3g.ffprobejaxb;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FFprobeJAXBTest {

	private static final String OUT0_SUMMARY = "QuickTime / MOV, 00:00:50, 465 MB, video: prores 720×576 Standard @ 50 fps [76 Mbps] yuv422p10le/rng:TV/spce:SMPTE170M/tsfer:BT709/prim:BT470BG (2500 frms), audio: pcm_s16le stereo @ 48000 Hz [1536 kbps], data: tmcd (Time Code Media Handler) 00:00:00:00";
	static String out0;
	static String out1;
	static String outNope;
	static String outErr;

	@BeforeAll
	static void load() throws IOException {
		out0 = FileUtils.readFileToString(new File("examples/out0.xml"), UTF_8);
		out1 = FileUtils.readFileToString(new File("examples/out1.xml"), UTF_8);
		outNope = FileUtils.readFileToString(new File("examples/outNope.xml"), UTF_8);
		outErr = FileUtils.readFileToString(new File("examples/outErr.txt"), UTF_8);
	}

	LinkedBlockingQueue<String> warns;
	FFprobeJAXB ffprobeJAXB;

	@BeforeEach
	void init() throws IOException {
		warns = new LinkedBlockingQueue<>();
		ffprobeJAXB = new FFprobeJAXB(out0, warns::add);
	}

	@AfterEach
	void close() {
		assertTrue(warns.isEmpty());
	}

	@Test
	void testBadXml() throws IOException {
		ffprobeJAXB = new FFprobeJAXB(outNope, warns::add);
		assertThrows(NumberFormatException.class, () -> new FFprobeJAXB(out1, warns::add));
		assertThrows(UncheckedIOException.class, () -> new FFprobeJAXB(outErr, warns::add));
	}

	@Test
	void testGetXmlContent() {
		assertEquals(out0, ffprobeJAXB.getXmlContent());
	}

	@Test
	void testGetChapters() {
		assertTrue(ffprobeJAXB.getChapters().isEmpty());
	}

	@Test
	void testGetStreams() {
		assertEquals(3, ffprobeJAXB.getStreams().size());
		assertEquals("video", ffprobeJAXB.getStreams().get(0).getCodecType());
		assertEquals("audio", ffprobeJAXB.getStreams().get(1).getCodecType());
		assertEquals("data", ffprobeJAXB.getStreams().get(2).getCodecType());
	}

	@Test
	void testGetFormat() {
		final var f = ffprobeJAXB.getFormat();
		assertNotNull(f);
		assertEquals("QuickTime / MOV", f.getFormatLongName());
	}

	@Test
	void testGetError() {
		assertNull(ffprobeJAXB.getError());
	}

	@Test
	void testGetProgramVersion() {
		assertNull(ffprobeJAXB.getProgramVersion());
	}

	@Test
	void testGetLibraryVersions() {
		assertTrue(ffprobeJAXB.getLibraryVersions().isEmpty());
	}

	@Test
	void testGetPixelFormats() {
		assertTrue(ffprobeJAXB.getPixelFormats().isEmpty());
	}

	@Test
	void testGetPackets() {
		assertTrue(ffprobeJAXB.getPackets().isEmpty());
	}

	@Test
	void testGetFrames() {
		assertTrue(ffprobeJAXB.getFrames().isEmpty());
	}

	@Test
	void testGetPacketsAndFrames() {
		assertTrue(ffprobeJAXB.getPacketsAndFrames().isEmpty());
	}

	@Test
	void testGetPrograms() {
		assertTrue(ffprobeJAXB.getPrograms().isEmpty());
	}

	@Test
	void testGetVideoStreams() {
		final var streams = ffprobeJAXB.getVideoStreams()
				.collect(toUnmodifiableList());
		assertEquals(1, streams.size());
		assertEquals("video", streams.get(0).getCodecType());
	}

	@Test
	void testGetAudiosStreams() {
		final var streams = ffprobeJAXB.getAudiosStreams()
				.collect(toUnmodifiableList());
		assertEquals(1, streams.size());
		assertEquals("audio", streams.get(0).getCodecType());
	}

	@Test
	void testGetMediaSummary() {
		assertEquals(OUT0_SUMMARY, ffprobeJAXB.getMediaSummary().toString());
	}

	@Test
	void testToString() {
		assertEquals(OUT0_SUMMARY, ffprobeJAXB.toString());
	}

}
