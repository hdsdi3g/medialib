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
 * Copyright (C) hdsdi3g for hd3g.tv 2020
 *
 */
package tv.hd3g.fflauncher.about;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tv.hd3g.fflauncher.about.FFAboutPixelFormat.BitDepths.BitDepths_16_16_16;
import static tv.hd3g.fflauncher.about.FFAboutPixelFormat.BitDepths.BitDepths_8;
import static tv.hd3g.fflauncher.about.FFAboutPixelFormat.BitDepths.Unknown;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import tv.hd3g.fflauncher.about.FFAboutCodec.CodecType;
import tv.hd3g.fflauncher.enums.FilterConnectorType;

class AboutTest {

	private static List<String> readLinesFromResource(final String resource) {
		try {
			return Files.readAllLines(new File("src/test/resources/" + resource).toPath(), UTF_8);
		} catch (final IOException e) {
			throw new UncheckedIOException("Can't get resource " + resource, e);
		}
	}

	@Test
	void testVersion() {
		final var v = new FFAboutVersion(readLinesFromResource("test-version.txt"));

		assertEquals("3.3.3 Copyright (c) 2000-2017 the FFmpeg developers", v.headerVersion);
		assertEquals("gcc 4.9.2 (Debian 4.9.2-10)", v.builtWith);

		Arrays.stream(
		        "gpl version3 nonfree yasm libmp3lame libbluray libopenjpeg libtheora libvorbis libtwolame libvpx libxvid libgsm libopencore-amrnb libopencore-amrwb libopus librtmp libschroedinger libsmbclient libspeex libssh libvo-amrwbenc libwavpack libwebp libzvbi libx264 libx265 libsmbclient libssh"
		                .split(" ")).forEach(cf -> {
			                assertTrue(v.configuration.contains(cf), "Missing " + cf);
		                });

		assertEquals(
		        "--enable-gpl --enable-version3 --enable-nonfree --as=yasm --enable-libmp3lame --enable-libbluray --enable-libopenjpeg --enable-libtheora --enable-libvorbis --enable-libtwolame --enable-libvpx --enable-libxvid --enable-libgsm --enable-libopencore-amrnb --enable-libopencore-amrwb --enable-libopus --enable-librtmp --enable-libschroedinger --enable-libsmbclient --enable-libspeex --enable-libssh --enable-libvo-amrwbenc --enable-libwavpack --enable-libwebp --enable-libzvbi --enable-libx264 --enable-libx265 --enable-libsmbclient --enable-libssh",
		        v.rawConfiguration);
		assertEquals("55. 58.100 / 55. 58.100", v.libavutilVersion);
		assertEquals("57. 89.100 / 57. 89.100", v.libavcodecVersion);
		assertEquals("57. 71.100 / 57. 71.100", v.libavformatVersion);
		assertEquals("57.  6.100 / 57.  6.100", v.libavdeviceVersion);
		assertEquals("6. 82.100 /  6. 82.100", v.libavfilterVersion);
		assertEquals("4.  6.100 /  4.  6.100", v.libswscaleVersion);
		assertEquals("2.  7.100 /  2.  7.100", v.libswresampleVersion);
		assertEquals("54.  5.100 / 54.  5.100", v.libpostprocVersion);
	}

	@Test
	void testCodecs() {
		final var list = FFAboutCodec.parse(readLinesFromResource("test-codecs.txt"));

		final var test1 = list.stream().filter(c -> (c.type == CodecType.AUDIO & c.encodingSupported
		                                             & c.decodingSupported & c.lossyCompression
		                                             & c.name.equals("adpcm_g722"))).collect(Collectors
		                                                     .toUnmodifiableList());

		assertEquals(1, test1.size());
		assertEquals("G.722 ADPCM", test1.get(0).longName);
		assertTrue(test1.get(0).toString().startsWith(test1.get(0).longName));

		assertEquals(7, list.stream().filter(c -> (c.type == CodecType.DATA)).count());

		assertEquals(10, list.stream().filter(c -> (c.encodingSupported == false & c.decodingSupported == false
		                                            & c.losslessCompression == false
		                                            && c.lossyCompression == false)).count());

		final var t = list.stream().filter(c -> c.name.equals("dirac")).findFirst().get();

		assertEquals("Dirac", t.longName);
		assertTrue(t.decoders.contains("dirac"));
		assertTrue(t.encoders.contains("vc2"));

		assertTrue(t.encoders.contains("libschroedinger"));
		assertTrue(t.decoders.contains("libschroedinger"));

		assertEquals(2, t.encoders.size());
		assertEquals(2, t.decoders.size());
	}

	@Test
	void testFormats() {
		final var list = FFAboutFormat.parseFormats(readLinesFromResource("test-formats.txt"));

		assertEquals(326, list.size());

		final var test1 = list.stream().filter(f -> (f.muxing == false & f.demuxing == true & f.name.equals(
		        "bfi"))).collect(Collectors.toUnmodifiableList());

		assertEquals(1, test1.size());
		assertEquals("Brute Force & Ignorance", test1.get(0).longName);

		assertEquals(2, list.stream().filter(f -> f.name.equals("hls")).count());

		assertEquals(2, list.stream().filter(f -> f.alternateTags.contains("mp4")).count());

	}

	@Test
	void testDevices() {
		final var list = FFAboutDevice.parseDevices(readLinesFromResource("test-devices.txt"));
		assertEquals(7, list.size());

		var i = 0;
		assertEquals("DV1394 A/V grab [dv1394] demuxing only supported", list.get(i++).toString());
		assertEquals("Linux framebuffer [fbdev] muxing and demuxing supported", list.get(i++).toString());
		assertEquals("Libavfilter virtual input device [lavfi] demuxing only supported", list.get(i++).toString());
		assertEquals("OSS (Open Sound System) playback [oss] muxing and demuxing supported", list.get(i++).toString());
		assertEquals("Video4Linux2 output device [v4l2] muxing only supported", list.get(i++).toString());
		assertEquals("Video4Linux2 device grab [video4linux2, v4l2] demuxing only supported", list.get(i++).toString());
		assertEquals("[libcdio] demuxing only supported", list.get(i++).toString());
	}

	@Test
	void testBSFS() {
		final var filters = FFAbout.parseBSFS(readLinesFromResource("test-bsfs.txt").stream());

		assertTrue(filters.contains("noise"));
		assertEquals(17, filters.size());
	}

	@Test
	void testProtocols() {
		final var p = new FFAboutProtocols(readLinesFromResource("test-protocols.txt"));

		assertFalse(p.input.contains("Input:") | p.input.contains("input:"));
		assertFalse(p.input.contains("Output:") | p.input.contains("output:"));
		assertFalse(p.output.contains("Input:") | p.output.contains("input:"));
		assertFalse(p.output.contains("Output:") | p.output.contains("output:"));

		assertEquals(29, p.input.size());
		assertEquals(24, p.output.size());

		assertTrue(p.input.contains("concat"));
		assertFalse(p.output.contains("concat"));

		assertFalse(p.input.contains("icecast"));
		assertTrue(p.output.contains("icecast"));
	}

	@Test
	void testFilters() {
		final var list = FFAboutFilter.parseFilters(readLinesFromResource("test-filters.txt"));

		assertEquals(299, list.size());

		assertTrue(list.stream().anyMatch(f -> f.getTag().equals("afftfilt")));

		assertEquals(3, list.stream().filter(f -> (f.getSourceConnectorsCount() == 2
		                                           && f.getSourceConnector() == FilterConnectorType.AUDIO)).count());

		assertEquals(1, list.stream().filter(f -> (f.getSourceConnectorsCount() == 2
		                                           && f.getSourceConnector() == FilterConnectorType.VIDEO
		                                           && f.getDestConnectorsCount() == 2
		                                           && f.getDestConnector() == FilterConnectorType.VIDEO)).filter(f -> f
		                                                   .getTag()
		                                                   .equals("scale2ref")).filter(f -> f.getLongName().equals(
		                                                           "Scale the input video size and/or convert the image format to the given reference."))
		        .count());

		assertTrue(list.get(0).toString().startsWith(list.get(0).getLongName()));

	}

	@Test
	void testPixelFormats() {
		final var list = FFAboutPixelFormat.parsePixelsFormats(readLinesFromResource(
		        "test-pixelsformats.txt"));

		assertEquals(207, list.size());

		assertEquals(1, list.stream().filter(pf -> (pf.tag.equals("pal8") && pf.supportedInput
		                                            && pf.supportedOutput == false && pf.paletted
		                                            && pf.nbComponents == 1 && pf.bitsPerPixel == 8
		                                            && pf.bitDepths == BitDepths_8)).count());

		assertEquals(1, list.stream().filter(pf -> (pf.tag.equals("yuv444p16le") && pf.supportedInput
		                                            && pf.supportedOutput && pf.paletted == false
		                                            && pf.hardwareAccelerated == false && pf.nbComponents == 3
		                                            && pf.bitsPerPixel == 48
		                                            && pf.bitDepths == BitDepths_16_16_16)).count());

		assertEquals(1, list.stream().filter(pf -> (pf.tag.equals("fake") && pf.supportedInput
		                                            && pf.supportedOutput && pf.paletted == false
		                                            && pf.hardwareAccelerated == false && pf.nbComponents == 1
		                                            && pf.bitsPerPixel == 1
		                                            && pf.bitDepths == Unknown)).count());
	}

	@Test
	void testHwaccels() {
		final var list = FFAbout.parseBSFS(readLinesFromResource("test-hwaccels.txt").stream());
		assertEquals(6, list.size());

		assertTrue(list.contains("cuvid"));
		assertTrue(list.contains("cuda"));
		assertTrue(list.contains("dxva2"));
		assertTrue(list.contains("qsv"));
		assertTrue(list.contains("d3d11va"));
		assertTrue(list.contains("qsv"));
	}

}
