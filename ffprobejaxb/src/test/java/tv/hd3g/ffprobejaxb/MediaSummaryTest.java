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
 * Copyright (C) hdsdi3g for hd3g.tv 2023
 *
 */
package tv.hd3g.ffprobejaxb;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tv.hd3g.ffprobejaxb.MediaSummary.resumeDispositions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import org.apache.commons.io.FileUtils;
import org.ffmpeg.ffprobe.ChapterType;
import org.ffmpeg.ffprobe.ChaptersType;
import org.ffmpeg.ffprobe.FormatType;
import org.ffmpeg.ffprobe.StreamDispositionType;
import org.ffmpeg.ffprobe.StreamType;
import org.junit.jupiter.api.Test;

import net.datafaker.Faker;

class MediaSummaryTest {
	static Faker faker = net.datafaker.Faker.instance();

	MediaSummary ms;

	@Test
	void testCreate() throws IOException {
		final var out0 = FileUtils.readFileToString(new File("examples/out0.xml"), UTF_8);
		final var source = new FFprobeJAXB(out0, s -> {
		});
		ms = MediaSummary.create(source);
		assertNotNull(ms);

		assertEquals("QuickTime / MOV, 00:00:50, 465 MB", ms.format());
		assertEquals(List.of(
				"video: prores 720×576 Standard @ 50 fps [76 Mbps] yuv422p10le/colRange:TV/colSpace:SMPTE170M/colTransfer:BT709/colPrimaries:BT470BG (2500 frms) default stream",
				"audio: pcm_s16le stereo @ 48000 Hz [1536 kbps] default stream",
				"data: tmcd (Time Code Media Handler) 00:00:00:00"),
				ms.streams());
	}

	@Test
	void testCreate_smallFile() throws IOException {
		final var source = new FFprobeJAXB("""
				<?xml version="1.0" encoding="UTF-8"?>
				<ffprobe>
				    <format size="123456" bit_rate="18000" duration="50.000000" format_long_name="QuickTime / MOV" />
				</ffprobe>
				""",
				s -> {
				});
		ms = MediaSummary.create(source);
		assertNotNull(ms);

		assertEquals("QuickTime / MOV, 00:00:50, 123456 bytes, 18 kbps", ms.format());
	}

	@Test
	void testCreate_bigFile() throws IOException {
		final var source = new FFprobeJAXB("""
				<?xml version="1.0" encoding="UTF-8"?>
				<ffprobe>
				    <format size="123456789" bit_rate="18000000" duration="50.000000" format_long_name="MOV" />
				</ffprobe>
				""", s -> {
		});
		ms = MediaSummary.create(source);
		assertNotNull(ms);

		assertEquals("MOV, 00:00:50, 117 MB, 18 Mbps", ms.format());
	}

	@Test
	void testCreate_programs_chapters() throws IOException {
		final var source = new FFprobeJAXB("""
				<?xml version="1.0" encoding="UTF-8"?>
				<ffprobe></ffprobe>
				""", s -> {
		});
		final var programs = faker.random().nextInt(2, 1000);
		final var format = new FormatType();
		format.setNbPrograms(programs);
		format.setDuration(1f);
		format.setSize(1l);
		source.probeResult.setFormat(format);

		final var chapters = new ChaptersType();
		final var chapCount = faker.random().nextInt(1, 1000);
		IntStream.range(0, chapCount)
				.forEach(i -> chapters.getChapter().add(new ChapterType()));
		source.probeResult.setChapters(chapters);

		ms = MediaSummary.create(source);
		assertNotNull(ms);

		assertEquals("null, 00:00:01, 1 bytes, " + programs + " program(s), " + chapCount + " chapter(s)",
				ms.format());
		assertEquals(List.of(), ms.streams());
	}

	@Test
	void testGetValue() {
		assertEquals(Optional.empty(), MediaSummary.getValue(null));
		assertEquals(Optional.empty(), MediaSummary.getValue(""));
		final var value = faker.numerify("value###");
		assertEquals(Optional.ofNullable(value), MediaSummary.getValue(value));
	}

	@Test
	void testAddDisposition_null() {
		final var entries = new ArrayList<String>();
		resumeDispositions(null).forEach(entries::add);
		assertTrue(entries.isEmpty());
	}

	@Test
	void testAddDisposition_default_noPic() {
		final var s = new StreamDispositionType();
		s.setDefault(1);
		s.setAttachedPic(0);
		final var entries = new ArrayList<String>();
		resumeDispositions(s).forEach(entries::add);
		assertEquals(List.of("default stream"), entries);
	}

	@Test
	void testAddDisposition_NoDefault_isPic() {
		final var s = new StreamDispositionType();
		s.setDefault(0);
		final var entries = new ArrayList<String>();
		resumeDispositions(s).forEach(entries::add);
		assertEquals(List.of(), entries);

		entries.clear();
		s.setAttachedPic(1);
		resumeDispositions(s).forEach(entries::add);
		assertEquals(List.of("attached picture"), entries);
	}

	@Test
	void testGetAudioSummary() {
		final var s = new StreamType();
		s.setCodecType("type");
		s.setCodecName("name");
		s.setProfile("profile");
		s.setSampleFmt("sample");
		s.setChannels(8);
		s.setChannelLayout("layout");
		s.setSampleRate(48000);
		s.setBitRate(1_000_000);
		assertEquals("type: name profile sample layout (8 channels) @ 48000 Hz [1000 kbps]",
				MediaSummary.getAudioSummary(s));

		s.setSampleFmt(null);
		assertEquals("type: name profile layout (8 channels) @ 48000 Hz [1000 kbps]",
				MediaSummary.getAudioSummary(s));

		s.setSampleFmt("fltp");
		assertEquals("type: name profile layout (8 channels) @ 48000 Hz [1000 kbps]",
				MediaSummary.getAudioSummary(s));

		s.setChannelLayout(null);
		assertEquals("type: name profile 8 channels @ 48000 Hz [1000 kbps]",
				MediaSummary.getAudioSummary(s));

		s.setChannels(2);
		s.setChannelLayout("layout");
		assertEquals("type: name profile layout @ 48000 Hz [1000 kbps]",
				MediaSummary.getAudioSummary(s));

		s.setChannelLayout(null);
		assertEquals("type: name profile 2 channels @ 48000 Hz [1000 kbps]",
				MediaSummary.getAudioSummary(s));

		s.setChannels(1);
		assertEquals("type: name profile mono @ 48000 Hz [1000 kbps]",
				MediaSummary.getAudioSummary(s));
	}

	@Test
	void testGetVideoSummary() {
		final var s = new StreamType();
		s.setCodecType("type");
		s.setCodecName("name");
		s.setWidth(2);
		s.setHeight(3);
		s.setHasBFrames(1);
		s.setProfile("profile");
		s.setLevel(10);
		s.setAvgFrameRate("25");
		s.setNbFrames(100);

		s.setBitRate(1_000_000);
		assertEquals("type: name 2×3 profile/L10 with B frames @ 25 fps [1000 kbps] (100 frms)",
				MediaSummary.getVideoSummary(s));

		s.setProfile(null);
		s.setLevel(0);
		assertEquals("type: name 2×3 with B frames @ 25 fps [1000 kbps] (100 frms)",
				MediaSummary.getVideoSummary(s));

		s.setLevel(10);
		assertEquals("type: name 2×3 L10 with B frames @ 25 fps [1000 kbps] (100 frms)",
				MediaSummary.getVideoSummary(s));

		s.setProfile("0");
		assertEquals("type: name 2×3 L10 with B frames @ 25 fps [1000 kbps] (100 frms)",
				MediaSummary.getVideoSummary(s));

		s.setProfile("0");
		s.setLevel(0);
		assertEquals("type: name 2×3 with B frames @ 25 fps [1000 kbps] (100 frms)",
				MediaSummary.getVideoSummary(s));
	}

	@Test
	void testComputePixelsFormat() {
		final var s = new StreamType();
		s.setPixFmt("pix");
		s.setColorRange("range");
		s.setColorSpace("space");
		s.setColorTransfer("transfert");
		s.setColorPrimaries("primaries");
		assertEquals("pix/colRange:RANGE/colSpace:SPACE/colTransfer:TRANSFERT/colPrimaries:PRIMARIES",
				MediaSummary.computePixelsFormat(s));

		s.setColorPrimaries(null);
		assertEquals("pix/colRange:RANGE/colSpace:SPACE/colTransfer:TRANSFERT",
				MediaSummary.computePixelsFormat(s));
		s.setColorTransfer(null);
		assertEquals("pix/colRange:RANGE/colSpace:SPACE",
				MediaSummary.computePixelsFormat(s));
		s.setColorSpace(null);
		assertEquals("pix/colRange:RANGE",
				MediaSummary.computePixelsFormat(s));
		s.setColorRange(null);
		assertEquals("pix",
				MediaSummary.computePixelsFormat(s));
		s.setPixFmt(null);
		assertEquals("",
				MediaSummary.computePixelsFormat(s));
	}

	@Test
	void testAddZeros() {
		var sb = new StringBuilder();

		MediaSummary.addZeros(0, sb);
		assertEquals("00", sb.toString());

		sb = new StringBuilder();
		MediaSummary.addZeros(1, sb);
		assertEquals("01", sb.toString());

		sb = new StringBuilder();
		MediaSummary.addZeros(9, sb);
		assertEquals("09", sb.toString());

		sb = new StringBuilder();
		MediaSummary.addZeros(99, sb);
		assertEquals("99", sb.toString());

		sb = new StringBuilder();
		MediaSummary.addZeros(999, sb);
		assertEquals("999", sb.toString());
	}

	@Test
	void testComputeDuration() {
		final var format = new FormatType();

		format.setDuration(1f);
		assertEquals("00:00:01", MediaSummary.computeDuration(format));

		format.setDuration(60f);
		assertEquals("00:01:00", MediaSummary.computeDuration(format));

		format.setDuration(3600f);
		assertEquals("01:00:00", MediaSummary.computeDuration(format));

		format.setDuration(36061f);
		assertEquals("10:01:01", MediaSummary.computeDuration(format));

		format.setDuration(0.5f);
		assertEquals("00:00:00", MediaSummary.computeDuration(format));
	}

	@Test
	void testToString() {
		ms = new MediaSummary("format", List.of(
				"video: [10 kbps]",
				"audio: [11 kbps]",
				"data: [12 kbps]"));
		assertEquals("format, video: [10 kbps], audio: [11 kbps], data: [12 kbps]",
				ms.toString());
	}

	@Test
	void testToString_Sort() {
		ms = new MediaSummary("format", List.of(
				"audio: [20 kbps]",
				"video: [30 kbps]",
				"audio: has bis [40 kbps]",
				"video: [50 kbps]",
				"audio: nope bitrate",
				"video: [1 Mbps]",
				"audio: has bis [40 kbps]",
				"data: [12 kbps]"));
		assertEquals(
				"format, video: [1 Mbps], video: [50 kbps], video: [30 kbps], 2× audio: has bis [40 kbps], audio: [20 kbps], audio: nope bitrate, data: [12 kbps]",
				ms.toString());
	}

	@Test
	void testNoBitRate() throws IOException {
		final var out0 = FileUtils.readFileToString(new File("examples/out2-nobitrate.xml"), UTF_8);
		final var source = new FFprobeJAXB(out0, s -> {
		});
		ms = MediaSummary.create(source);
		assertNotNull(ms);

		assertEquals("Matroska / WebM, 00:01:00, 2 MB, 415 kbps", ms.format());
		assertEquals(List.of(
				"video: vp9 480×480 Profile 0 @ 29.97 fps yuv420p/colRange:TV/BT709 default stream",
				"audio: opus mono @ 48000 Hz default stream"),
				ms.streams());
	}

	@Test
	void testGetLevelTag() {
		final var level = faker.random().nextInt(1000, 100000);
		assertEquals("L" + level, MediaSummary.getLevelTag(faker.numerify("codec###"), level));
		assertEquals("L" + level, MediaSummary.getLevelTag("mpeg2video", level));
		assertEquals("L" + level, MediaSummary.getLevelTag("h264", level));
		assertEquals("L" + level, MediaSummary.getLevelTag("hevc", level));
		assertEquals("L" + level, MediaSummary.getLevelTag("av1", level));

		assertEquals("Main", MediaSummary.getLevelTag("mpeg2video", 8));
		assertEquals("2.2", MediaSummary.getLevelTag("h264", 22));
		assertEquals("3.1", MediaSummary.getLevelTag("hevc", 93));
		assertEquals("6.1", MediaSummary.getLevelTag("av1", 61));
	}

}
