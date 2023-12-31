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
 * Copyright (C) hdsdi3g for hd3g.tv 2024
 *
 */
package tv.hd3g.ffprobejaxb;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.function.Predicate.not;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import tv.hd3g.ffprobejaxb.data.FFProbeKeyValue;
import tv.hd3g.ffprobejaxb.data.FFProbeLibraryVersion;
import tv.hd3g.ffprobejaxb.data.FFProbePixelFormat;
import tv.hd3g.ffprobejaxb.data.FFProbeStream;
import tv.hd3g.ffprobejaxb.data.FFProbeStreamDisposition;

class FFprobeJAXBE2ETest {

	@TestFactory
	Stream<DynamicTest> test() {
		return Stream.of(new File("src/test/resources")
				.listFiles((dir, name) -> name.startsWith("test-") && name.endsWith(".xml")))
				.sorted()
				.map(CheckE2EXML::new)
				.flatMap(CheckE2EXML::toDynamicTests);
	}

	class CheckE2EXML {
		final File xml;
		String xmlContent;
		FFprobeJAXB ffprobe;
		FFProbeStream s;
		MediaSummary ms;

		CheckE2EXML(final File xml) {
			this.xml = xml;
			try {
				xmlContent = FileUtils.readFileToString(xml, UTF_8);
				ffprobe = FFprobeJAXB.load(xmlContent);
			} catch (final IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		void preCheck() throws Exception {
			assertThat(ffprobe).isNotNull();
			assertThat(ffprobe.getXmlContent()).isEqualTo(xmlContent);

			assertThat(ffprobe.getChapters()).isNotNull();

			assertThat(ffprobe.getError()).isNotNull();

			assertThat(ffprobe.getFormat()).isNotNull();
			assertThat(ffprobe.getFormat()).isPresent();

			assertThat(ffprobe.getLibraryVersions()).isNotNull();
			assertThat(ffprobe.getLibraryVersions()).hasSize(7);

			assertThat(ffprobe.getMediaSummary()).isNotNull();
			assertThat(ffprobe.getPrograms()).isNotNull();

			assertThat(ffprobe.getProgramVersion()).isNotNull();

			assertThat(ffprobe.getStreams()).isNotNull();
			assertThat(ffprobe.getStreams()).hasSize(2);

			assertThat(ffprobe.getFirstVideoStream()).isNotNull();
			assertThat(ffprobe.getFirstVideoStream()).isPresent();

			assertThat(ffprobe.getVideoStreams()).isNotNull();
			assertThat(ffprobe.getVideoStreams()).hasSize(1);

			assertThat(ffprobe.getFirstVideoStream()).contains(ffprobe.getVideoStreams().findFirst().get());

			assertThat(ffprobe.getAudioStreams()).isNotNull();
			assertThat(ffprobe.getAudioStreams()).hasSize(1);

			assertThat(ffprobe.getStreams()).contains(
					ffprobe.getAudioStreams().findFirst().get(),
					ffprobe.getVideoStreams().findFirst().get());

			assertThat(ffprobe.getXSDVersionReference()).isNotNull();
			assertThat(ffprobe.toString()).isNotNull().isNotEmpty();

			assertThat(ffprobe.getPixelFormats()).isNotNull();
			assertThat(ffprobe.getPixelFormats()).isNotEmpty();
		}

		private void testGetFormat() {
			final var format = ffprobe.getFormat().get();
			assertThat(format.filename()).endsWith(xml.getName().substring(0, xml.getName().indexOf("-", 6)));
		}

		private boolean isXMLNameContains(final String... orValues) {
			final var xmlName = xml.getName();
			return Stream.of(orValues)
					.filter(Objects::nonNull)
					.anyMatch(xmlName::contains);
		}

		private void testGetPrograms() {
			if (isXMLNameContains("mpeg2.ts")) {
				assertThat(ffprobe.getPrograms()).hasSize(1);
				final var p = ffprobe.getPrograms().get(0);
				assertThat(p.streams()).hasSize(2);
				assertThat(p.programId()).isEqualTo(1);
				assertThat(p.programNum()).isEqualTo(1);
				assertThat(p.nbStreams()).isEqualTo(2);
				assertThat(p.pcrPid()).isLessThanOrEqualTo(4096);
				assertThat(p.pmtPid()).isLessThanOrEqualTo(4096);
				assertThat(p.tags()).contains(new FFProbeKeyValue("service_name", "Demo render"));
				assertThat(p.tags()).contains(new FFProbeKeyValue("service_provider", "Media ex Machina"));
				assertThat(p.streams()).isEqualTo(ffprobe.getStreams());
			} else {
				assertThat(ffprobe.getPrograms()).isEmpty();
			}
		}

		private void testGetStreams() {
			final var streams = ffprobe.getStreams();
			assertThat(streams).hasSize(2);

			for (var pos = 0; pos < streams.size(); pos++) {
				assertEquals(pos, streams.get(pos).index(), "Bad stream pos index");
			}
			assertEquals("video", streams.get(0).codecType());
			assertEquals("audio", streams.get(1).codecType());
		}

		private void testVideoStream() {
			s = ffprobe.getStreams().get(0);
			assertThat(s.extradata()).isNull();
			assertThat(s.extradataHash()).isNull();
			assertThat(s.width()).isEqualTo(352);
			assertThat(s.height()).isEqualTo(288);
			assertThat(s.closedCaptions()).isFalse();
			assertThat(s.filmGrain()).isFalse();
			assertThat(s.sampleAspectRatio()).isEqualTo("1:1");
			assertThat(s.displayAspectRatio()).isEqualTo("11:9");
			assertThat(s.pixFmt()).isEqualTo("yuv420p");
			assertThat(s.colorSpace()).isNull();
			assertThat(s.colorTransfer()).isNull();
			assertThat(s.colorPrimaries()).isNull();

			assertThat(s.refs()).isEqualTo(1);
			assertThat(s.sampleFmt()).isNull();
			assertThat(s.sampleRate()).isZero();
			assertThat(s.channels()).isZero();
			assertThat(s.channelLayout()).isNull();
			assertThat(s.bitsPerSample()).isZero();
			assertThat(s.initialPadding()).isZero();
			assertThat(s.rFrameRate()).isEqualTo("25/1");
			assertThat(s.avgFrameRate()).isEqualTo("25/1");
			assertThat(s.bitsPerRawSample()).isZero();
			assertThat(s.nbReadFrames()).isZero();
			assertThat(s.nbReadPackets()).isZero();

			if (isXMLNameContains("mpeg2.ts")) {
				if (isXMLNameContains("6.2")) {
					assertThat(s.sideDataList()).hasSize(1);
					assertThat(s.sideDataList().get(0).sideDatum()).hasSize(6);
					assertThat(s.sideDataList().get(0).type()).isEqualTo("CPB properties");
					assertThat(s.extradataSize()).isEqualTo(22);
				}

				assertThat(s.codecName()).isEqualTo("mpeg2video");
				assertThat(s.codecLongName()).isEqualTo("MPEG-2 video");
				assertThat(s.profile()).isEqualTo("Main");
				assertThat(s.disposition()).isEqualTo(FFProbeStreamDisposition.getByNames());
				assertThat(s.tags()).isEmpty();
				assertThat(s.codecTag()).isEqualTo("0x0002");
				assertThat(s.codecTagString()).isEqualTo("[2][0][0][0]");
				assertThat(s.codedWidth()).isZero();
				assertThat(s.codedHeight()).isZero();
				assertThat(s.hasBFrames()).isTrue();
				assertThat(s.level()).isEqualTo(8);
				assertThat(s.colorRange()).isEqualTo("tv");
				assertThat(s.chromaLocation()).isEqualTo("left");
				assertThat(s.id()).isEqualTo("0x100");
				assertThat(s.timeBase()).isEqualTo("1/90000");
				assertThat(s.startPts()).isEqualTo(129600);
				assertThat(s.startTime()).isEqualTo(1.44f);
				assertThat(s.durationTs()).isEqualTo(450000);
				assertThat(s.bitRate()).isZero();
				assertThat(s.nbFrames()).isZero();
				assertThat(s.duration()).isEqualTo(5.0f);
			} else {
				assertThat(s.sideDataList()).isEmpty();
				assertThat(s.extradataSize()).isZero();
				assertThat(s.codedWidth()).isEqualTo(352);
				assertThat(s.codedHeight()).isEqualTo(288);
				assertThat(s.hasBFrames()).isFalse();
				assertThat(s.level()).isEqualTo(-99);
				assertThat(s.chromaLocation()).isNull();
				assertThat(s.startPts()).isZero();
				assertThat(s.startTime()).isEqualTo(0f);
			}

			if (isXMLNameContains("ffv1.mov")) {
				assertThat(s.codecName()).isEqualTo("ffv1");
				assertThat(s.codecLongName()).isEqualTo("FFmpeg video codec #1");
				assertThat(s.profile()).isNull();
				assertThat(s.disposition()).isEqualTo(FFProbeStreamDisposition.getByNames("asDefault"));

				if (isXMLNameContains("-2.", "-3.", "-4.0", "-4.1", "-4.2", "-4.3")) {
					assertThat(s.tags()).doesNotContain(new FFProbeKeyValue("vendor_id", "FFMP"));
				} else {
					assertThat(s.tags()).contains(new FFProbeKeyValue("vendor_id", "FFMP"));
				}

				if (isXMLNameContains("-2.", "-3.", "-4.")) {
					assertThat(s.id()).isNull();
				} else {
					assertThat(s.id()).isEqualTo("0x1");
				}

				assertThat(s.codecTag()).isEqualTo("0x31564646");
				assertThat(s.codecTagString()).isEqualTo("FFV1");
				assertThat(s.colorRange()).isNull();
				assertThat(s.timeBase()).isEqualTo("1/12800");
				assertThat(s.durationTs()).isEqualTo(64000);
				assertThat(s.duration()).isEqualTo(5.0f);
				assertThat(s.bitRate()).isEqualTo(8568404);
				assertThat(s.nbFrames()).isEqualTo(125);
			}

			if (isXMLNameContains("vp8.mkv")) {
				if (isXMLNameContains("2.8")) {
					assertThat(s.profile()).isNull();
					assertThat(s.colorRange()).isNull();
				} else {
					assertThat(s.profile()).isEqualTo("0");
					assertThat(s.colorRange()).isEqualTo("tv");
				}

				assertThat(s.codecName()).isEqualTo("vp8");
				assertThat(s.codecLongName()).isEqualTo("On2 VP8");
				assertThat(s.disposition()).isEqualTo(FFProbeStreamDisposition.getByNames());
				assertThat(s.tags()).contains(new FFProbeKeyValue("AKEY", "avalue"));
				assertThat(s.codecTag()).isEqualTo("0x0000");
				assertThat(s.codecTagString()).isEqualTo("[0][0][0][0]");
				assertThat(s.id()).isNull();
				assertThat(s.timeBase()).isEqualTo("1/1000");
				assertThat(s.durationTs()).isZero();
				assertThat(s.duration()).isZero();
				assertThat(s.bitRate()).isZero();
				assertThat(s.nbFrames()).isZero();
			}

			if (isXMLNameContains("2.8") == false) {
				assertThat(s.fieldOrder()).isEqualTo("progressive");
				assertThat(s.maxBitRate()).isZero();
			}
		}

		private void testAudioStream() {
			s = ffprobe.getStreams().get(1);

			assertThat(s.avgFrameRate()).isEqualTo("0/0");
			assertThat(s.bitsPerRawSample()).isZero();
			assertThat(s.channelLayout()).isEqualTo("stereo");
			assertThat(s.channels()).isEqualTo(2);
			assertThat(s.chromaLocation()).isNull();
			assertThat(s.closedCaptions()).isFalse();
			assertThat(s.extradata()).isNull();
			assertThat(s.extradataHash()).isNull();

			if (isXMLNameContains("mpeg2.ts")) {
				assertThat(s.id()).isEqualTo("0x101");
				assertThat(s.bitRate()).isEqualTo(256000);
				assertThat(s.codecTagString()).isEqualTo("[3][0][0][0]");
				assertThat(s.bitsPerSample()).isZero();
				assertThat(s.codecLongName()).isEqualTo("MP2 (MPEG audio layer 2)");
				assertThat(s.codecName()).isEqualTo("mp2");
				assertThat(s.codecTag()).isEqualTo("0x0003");
				assertThat(s.durationTs()).isEqualTo(449280);
				assertThat(s.nbFrames()).isZero();

				if (isXMLNameContains("-2.", "-3.")) {
					assertThat(s.sampleFmt()).isEqualTo("s16p");
				} else {
					assertThat(s.sampleFmt()).isEqualTo("fltp");
				}

				assertThat(s.startPts()).isEqualTo(128698);
				assertThat(s.startTime()).isBetween(0f, 2.0f);
				assertThat(s.timeBase()).isEqualTo("1/90000");
				assertThat(s.duration()).isCloseTo(5.0f, offset(0.1f));
				assertThat(s.profile()).isNull();
				assertThat(s.extradataSize()).isZero();
				assertThat(s.initialPadding()).isZero();
				assertThat(s.tags()).isEmpty();
			}

			if (isXMLNameContains("ffv1.mov")) {
				if (isXMLNameContains("-2.", "-3.", "-4.")) {
					assertThat(s.id()).isNull();
				} else {
					assertThat(s.id()).isEqualTo("0x2");
				}

				assertThat(s.bitRate()).isEqualTo(1536000);
				assertThat(s.codecTagString()).isEqualTo("sowt");
				assertThat(s.disposition()).isEqualTo(FFProbeStreamDisposition.getByNames("asDefault"));
				assertThat(s.bitsPerSample()).isEqualTo(16);
				assertThat(s.codecLongName()).isEqualTo("PCM signed 16-bit little-endian");
				assertThat(s.codecName()).isEqualTo("pcm_s16le");
				assertThat(s.codecTag()).isEqualTo("0x74776f73");
				assertThat(s.durationTs()).isEqualTo(240000);
				assertThat(s.nbFrames()).isEqualTo(240000);
				assertThat(s.sampleFmt()).isEqualTo("s16");
				assertThat(s.startPts()).isZero();
				assertThat(s.startTime()).isEqualTo(0f);
				assertThat(s.timeBase()).isEqualTo("1/48000");
				assertThat(s.duration()).isCloseTo(5.0f, offset(0.1f));
				assertThat(s.profile()).isNull();
				assertThat(s.extradataSize()).isZero();
				assertThat(s.initialPadding()).isZero();
				assertThat(s.tags()).isNotEmpty();
			}

			if (isXMLNameContains("vp8.mkv")) {
				assertThat(s.id()).isNull();
				assertThat(s.bitRate()).isZero();
				assertThat(s.codecTagString()).isEqualTo("[0][0][0][0]");
				assertThat(s.bitsPerSample()).isZero();
				assertThat(s.codecLongName()).isEqualTo("AAC (Advanced Audio Coding)");
				assertThat(s.codecName()).isEqualTo("aac");
				assertThat(s.codecTag()).isEqualTo("0x0000");
				assertThat(s.nbFrames()).isZero();
				assertThat(s.sampleFmt()).isEqualTo("fltp");
				assertThat(s.startPts()).isBetween(-100l, 1l);
				assertThat(s.startTime()).isCloseTo(0f, offset(0.1f));
				assertThat(s.timeBase()).isEqualTo("1/1000");
				assertThat(s.duration()).isZero();
				assertThat(s.profile()).isEqualTo("LC");

				if (isXMLNameContains("-2.", "-3.", "-4.", "-5.", "-6.0", "-6.1-")) {
					assertThat(s.extradataSize()).isZero();
					assertThat(s.initialPadding()).isZero();
				} else {
					assertThat(s.extradataSize()).isEqualTo(5);
					assertThat(s.initialPadding()).isEqualTo(1024);
				}
				assertThat(s.tags()).isNotEmpty();
			}

			assertThat(s.codedHeight()).isZero();
			assertThat(s.codedWidth()).isZero();
			assertThat(s.colorPrimaries()).isNull();
			assertThat(s.colorRange()).isNull();
			assertThat(s.colorSpace()).isNull();
			assertThat(s.colorTransfer()).isNull();
			assertThat(s.displayAspectRatio()).isNull();
			assertThat(s.fieldOrder()).isNull();
			assertThat(s.filmGrain()).isFalse();
			assertThat(s.sideDataList()).isEmpty();
			assertThat(s.hasBFrames()).isFalse();
			assertThat(s.height()).isZero();
			assertThat(s.level()).isZero();
			assertThat(s.maxBitRate()).isZero();
			assertThat(s.nbReadFrames()).isZero();
			assertThat(s.nbReadPackets()).isZero();
			assertThat(s.pixFmt()).isNull();
			assertThat(s.rFrameRate()).isEqualTo("0/0");
			assertThat(s.refs()).isZero();
			assertThat(s.sampleAspectRatio()).isNull();
			assertThat(s.sampleRate()).isEqualTo(48000);
			assertThat(s.width()).isZero();
		}

		private void testGetChapters() {
			if (isXMLNameContains("vp8.mkv")) {
				assertThat(ffprobe.getChapters())
						.hasSize(1)
						.allMatch(c -> List.of(new FFProbeKeyValue("title", "Chap One")).equals(c.tags()))
						.allMatch(c -> c.id() == 1)
						.allMatch(c -> Math.round(c.startTime()) == 0)
						.allMatch(c -> Math.round(c.endTime()) == 3);
			} else {
				assertThat(ffprobe.getChapters()).isEmpty();
			}
		}

		private void testGetMediaSummary() {
			ms = ffprobe.getMediaSummary();
			assertThat(ms).isNotNull();
			assertThat(ms.format()).isNotBlank();
			assertThat(ms.streams()).hasSize(2);

			final var vStream = ms.streams().get(0);
			final var aStream = ms.streams().get(1);

			if (isXMLNameContains("mpeg2.ts")) {
				assertThat(ms.format()).isEqualTo("MPEG-TS (MPEG-2 Transport Stream), 00:00:05, 2 MB, 1 program");
				assertThat(vStream).isEqualTo(
						"video: mpeg2video 352×288 Main/Main with B frames @ 25 fps yuv420p/colRange:TV");
				assertThat(aStream).isEqualTo(
						"audio: mp2 stereo @ 48000 Hz [256 kbps]");
			}

			if (isXMLNameContains("ffv1.mov")) {
				assertThat(ms.format()).isEqualTo("QuickTime / MOV, 00:00:05, 6 MB");
				assertThat(vStream).isEqualTo(
						"video: ffv1 352×288 @ 25 fps [8568 kbps] yuv420p default");
				assertThat(aStream).isEqualTo(
						"audio: pcm_s16le stereo @ 48000 Hz [1536 kbps] default");
			}

			if (isXMLNameContains("vp8.mkv")) {
				assertThat(ms.format()).isEqualTo("Matroska / WebM, 00:00:05, 240334 bytes, 1 chapter, 383 kbps");

				if (isXMLNameContains("-2.")) {
					assertThat(vStream).isEqualTo(
							"video: vp8 352×288 @ 25 fps yuv420p");
				} else {
					assertThat(vStream).isEqualTo(
							"video: vp8 352×288 @ 25 fps yuv420p/colRange:TV");
				}

				assertThat(aStream).isEqualTo(
						"audio: aac LC stereo @ 48000 Hz");
			}
		}

		private void testToString() {
			assertThat(ffprobe).hasToString(ffprobe.getMediaSummary().toString());
		}

		private void testGetProgramVersion() {
			final var v = ffprobe.getProgramVersion().get();
			assertThat(v.configuration()).contains("--disable-ffmpeg", "--disable-ffplay");
			assertThat(v.version()).isNotEmpty();
			assertThat(v.copyright()).contains("FFmpeg");
			assertThat(v.compilerIdent()).isNotEmpty();
		}

		private void testGetLibraryVersions() {
			assertThat(ffprobe.getLibraryVersions().stream()
					.map(FFProbeLibraryVersion::name)
					.toList()).contains(
							"libavutil",
							"libavcodec",
							"libavformat",
							"libavdevice",
							"libavfilter",
							"libswscale",
							"libswresample");

			for (final var lv : ffprobe.getLibraryVersions()) {
				assertThat(lv.ident()).isNotBlank();
				assertThat(lv.major()).isPositive();
				assertThat(lv.minor()).isPositive();
				assertThat(lv.micro()).isPositive();
				assertThat(lv.version()).isPositive();
			}
		}

		private void testGetPixelFormats() {
			final var pf = ffprobe.getPixelFormats();
			assertThat(pf)
					.hasSize((int) pf.stream()
							.map(FFProbePixelFormat::name)
							.distinct()
							.count());

			assertTrue(pf.stream().allMatch(p -> Objects.nonNull(p.bitDepthByComponent())));
			assertTrue(pf.stream()
					.filter(not(FFProbePixelFormat::hwaccel))
					.allMatch(p -> p.bitDepthByComponent().size() > 0));
			assertTrue(pf.stream()
					.map(FFProbePixelFormat::bitDepthByComponent)
					.flatMap(List::stream)
					.allMatch(Objects::nonNull));
		}

		Stream<DynamicTest> toDynamicTests() {
			return Stream.of(
					dynamicTest(xml.getName() + " preCheck", this::preCheck),
					dynamicTest(xml.getName() + " testGetFormat", this::testGetFormat),
					dynamicTest(xml.getName() + " testGetStreams", this::testGetStreams),
					dynamicTest(xml.getName() + " testVideoStream", this::testVideoStream),
					dynamicTest(xml.getName() + " testAudioStream", this::testAudioStream),
					dynamicTest(xml.getName() + " testGetPrograms", this::testGetPrograms),
					dynamicTest(xml.getName() + " testAudioStream", this::testGetChapters),
					dynamicTest(xml.getName() + " testGetMediaSummary", this::testGetMediaSummary),
					dynamicTest(xml.getName() + " testToString", this::testToString),
					dynamicTest(xml.getName() + " testGetProgramVersion", this::testGetProgramVersion),
					dynamicTest(xml.getName() + " testGetLibraryVersions", this::testGetLibraryVersions),
					dynamicTest(xml.getName() + " testGetPixelFormats", this::testGetPixelFormats));
		}

	}

}
