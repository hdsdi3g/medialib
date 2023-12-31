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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import net.datafaker.Faker;

class MediaSummaryTest {
	static Faker faker = net.datafaker.Faker.instance();

	MediaSummary ms;

	@Test
	void testCreate() throws IOException {
		final var source = FFprobeJAXB.load(
				"""
						<?xml version="1.0" encoding="UTF-8"?>
						<ffprobe>
						    <streams>
						        <stream index="0" codec_name="prores" codec_long_name="Apple ProRes (iCodec Pro)" profile="Standard" codec_type="video" codec_time_base="1/50" codec_tag_string="apcn" codec_tag="0x6e637061" width="720" height="576" coded_width="720" coded_height="576" has_b_frames="0" sample_aspect_ratio="59:54" display_aspect_ratio="295:216" pix_fmt="yuv422p10le" level="-99" color_range="tv" color_space="smpte170m" color_transfer="bt709" color_primaries="bt470bg" field_order="progressive" refs="1" r_frame_rate="50/1" avg_frame_rate="50/1" time_base="1/50" start_pts="0" start_time="0.000000" duration_ts="2500" duration="50.000000" bit_rate="76328266" bits_per_raw_sample="10" nb_frames="2500">
						            <disposition default="1" dub="0" original="0" comment="0" lyrics="0" karaoke="0" forced="0" hearing_impaired="0" visual_impaired="0" clean_effects="0" attached_pic="0" timed_thumbnails="0"/>
						            <tag key="creation_time" value="2019-11-03T15:08:36.000000Z"/>
						            <tag key="language" value="eng"/>
						            <tag key="handler_name" value="Apple Video Media Handler"/>
						            <tag key="encoder" value="Apple ProRes¬†422"/>
						            <tag key="timecode" value="10:00:00:00"/>
						        </stream>
						        <stream index="1" codec_name="pcm_s16le" codec_long_name="PCM signed 16-bit little-endian" codec_type="audio" codec_time_base="1/48000" codec_tag_string="sowt" codec_tag="0x74776f73" sample_fmt="s16" sample_rate="48000" channels="2" channel_layout="stereo" bits_per_sample="16" r_frame_rate="0/0" avg_frame_rate="0/0" time_base="1/48000" start_pts="0" start_time="0.000000" duration_ts="2400000" duration="50.000000" bit_rate="1536000" nb_frames="2400000">
						            <disposition default="1" dub="0" original="0" comment="0" lyrics="0" karaoke="0" forced="0" hearing_impaired="0" visual_impaired="0" clean_effects="0" attached_pic="0" timed_thumbnails="0"/>
						            <tag key="creation_time" value="2019-11-03T15:08:36.000000Z"/>
						            <tag key="language" value="eng"/>
						            <tag key="handler_name" value="Apple Sound Media Handler"/>
						            <tag key="timecode" value="20:00:00:00"/>
						        </stream>
						        <stream index="2" codec_type="data" codec_tag_string="tmcd" codec_tag="0x64636d74" r_frame_rate="0/0" avg_frame_rate="50/1" time_base="1/50" start_pts="0" start_time="0.000000" duration_ts="2500" duration="50.000000" nb_frames="1">
						            <disposition default="1" dub="0" original="0" comment="0" lyrics="0" karaoke="0" forced="0" hearing_impaired="0" visual_impaired="0" clean_effects="0" attached_pic="0" timed_thumbnails="0"/>
						            <tag key="creation_time" value="2019-11-03T15:08:36.000000Z"/>
						            <tag key="language" value="eng"/>
						            <tag key="handler_name" value="Time Code Media Handler"/>
						            <tag key="timecode" value="30:00:00:00"/>
						        </stream>
						    </streams>

						    <format filename="Test.mov" nb_streams="3" nb_programs="0" format_name="mov,mp4,m4a,3gp,3g2,mj2" format_long_name="QuickTime / MOV" start_time="0.000000" duration="50.000000" size="487700244" bit_rate="78032039" probe_score="100">
						        <tag key="major_brand" value="qt  "/>
						        <tag key="minor_version" value="537199360"/>
						        <tag key="compatible_brands" value="qt  "/>
						        <tag key="creation_time" value="2019-11-03T15:08:36.000000Z"/>
						    </format>
						</ffprobe>
						""");
		ms = MediaSummary.create(source);
		assertNotNull(ms);

		assertEquals("QuickTime / MOV, 00:00:50, TCIN: 10:00:00:00, 465 MB", ms.format());
		assertEquals(List.of(
				"video: prores 720×576 Standard @ 50 fps [76 Mbps] yuv422p10le/colRange:TV/colSpace:SMPTE170M/colTransfer:BT709/colPrimaries:BT470BG default",
				"audio: pcm_s16le stereo @ 48000 Hz [1536 kbps] default",
				"data: tmcd (Time Code Media Handler)"),
				ms.streams());
	}

	@Test
	void testCreate_smallFile() throws IOException {
		final var source = FFprobeJAXB.load(
				"""
						<?xml version="1.0" encoding="UTF-8"?>
						<ffprobe>
						    <format size="123456" bit_rate="18000" duration="50.000000" format_long_name="QuickTime / MOV" />
						</ffprobe>
						""");
		ms = MediaSummary.create(source);
		assertNotNull(ms);

		assertEquals("QuickTime / MOV, 00:00:50, 123456 bytes, 18 kbps", ms.format());
	}

	@Test
	void testCreate_bigFile() throws IOException {
		final var source = FFprobeJAXB.load("""
				<?xml version="1.0" encoding="UTF-8"?>
				<ffprobe>
				    <format size="123456789" bit_rate="18000000" duration="50.000000" format_long_name="MOV" />
				</ffprobe>
				""");
		ms = MediaSummary.create(source);
		assertNotNull(ms);

		assertEquals("MOV, 00:00:50, 117 MB, 18 Mbps", ms.format());
	}

	@Test
	void testCreate_programs_chapters() throws IOException {
		final var source = FFprobeJAXB.load(
				"""
						<?xml version="1.0" encoding="UTF-8"?>
						<ffprobe>
						<programs>
						   <program program_id="1" program_num="1" nb_streams="2" pmt_pid="4096" pcr_pid="256" start_pts="1429978" start_time="1.429978" end_pts="6440000" end_time="6.440000">
						   </program>
						</programs>
						<chapters>
						   <chapter id="1" time_base="1/1000000000" start="1000000" start_time="0.001000" end="3000000000" end_time="3.000000">
						   </chapter>
						</chapters>
						<format nb_programs="9" start_time="0.000000" duration="5.000000" size="2" bit_rate="2000" probe_score="100" />
						</ffprobe>
						""");
		ms = MediaSummary.create(source);
		assertNotNull(ms);

		assertEquals("null, 00:00:05, 2 bytes, 9 programs, 1 chapter, 2 kbps", ms.format());
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
	void testGetAudioSummary() {
		var s = FFprobeJAXB.load(
				"""
						<?xml version="1.0" encoding="UTF-8"?>
						<ffprobe>
						    <streams>
						        <stream index="0" codec_name="name" profile="profile" codec_type="type" sample_fmt="sample" sample_rate="48000" channels="8" channel_layout="layout" bit_rate="1000000" />
						    </streams>
						</ffprobe>
						""")
				.getStreams().stream().findFirst().orElseThrow();

		assertEquals("type: name profile sample layout (8 channels) @ 48000 Hz [1000 kbps]",
				MediaSummary.getAudioSummary(s));

		s = FFprobeJAXB.load(
				"""
						<?xml version="1.0" encoding="UTF-8"?>
						<ffprobe>
						    <streams>
						        <stream index="0" codec_name="name" profile="profile" codec_type="type" sample_rate="48000" channels="8" channel_layout="layout" bit_rate="1000000" />
						    </streams>
						</ffprobe>
						""")
				.getStreams().stream().findFirst().orElseThrow();

		assertEquals("type: name profile layout (8 channels) @ 48000 Hz [1000 kbps]",
				MediaSummary.getAudioSummary(s));

		s = FFprobeJAXB.load(
				"""
						<?xml version="1.0" encoding="UTF-8"?>
						<ffprobe>
						    <streams>
						        <stream index="0" codec_name="name" profile="profile" codec_type="type" sample_fmt="fltp" sample_rate="48000" channels="8" channel_layout="layout" bit_rate="1000000" />
						    </streams>
						</ffprobe>
						""")
				.getStreams().stream().findFirst().orElseThrow();

		assertEquals("type: name profile layout (8 channels) @ 48000 Hz [1000 kbps]",
				MediaSummary.getAudioSummary(s));

		s = FFprobeJAXB.load(
				"""
						<?xml version="1.0" encoding="UTF-8"?>
						<ffprobe>
						    <streams>
						        <stream index="0" codec_name="name" profile="profile" codec_type="type" sample_fmt="fltp" sample_rate="48000" channels="8" bit_rate="1000000" />
						    </streams>
						</ffprobe>
						""")
				.getStreams().stream().findFirst().orElseThrow();

		assertEquals("type: name profile 8 channels @ 48000 Hz [1000 kbps]",
				MediaSummary.getAudioSummary(s));

		s = FFprobeJAXB.load(
				"""
						<?xml version="1.0" encoding="UTF-8"?>
						<ffprobe>
						    <streams>
						        <stream index="0" codec_name="name" profile="profile" codec_type="type" sample_fmt="fltp" sample_rate="48000" channels="2" channel_layout="layout" bit_rate="1000000" />
						    </streams>
						</ffprobe>
						""")
				.getStreams().stream().findFirst().orElseThrow();

		assertEquals("type: name profile layout @ 48000 Hz [1000 kbps]",
				MediaSummary.getAudioSummary(s));

		s = FFprobeJAXB.load(
				"""
						<?xml version="1.0" encoding="UTF-8"?>
						<ffprobe>
						    <streams>
						        <stream index="0" codec_name="name" profile="profile" codec_type="type" sample_fmt="fltp" sample_rate="48000" channels="2" bit_rate="1000000" />
						    </streams>
						</ffprobe>
						""")
				.getStreams().stream().findFirst().orElseThrow();

		assertEquals("type: name profile 2 channels @ 48000 Hz [1000 kbps]",
				MediaSummary.getAudioSummary(s));

		s = FFprobeJAXB.load(
				"""
						<?xml version="1.0" encoding="UTF-8"?>
						<ffprobe>
						    <streams>
						        <stream index="0" codec_name="name" profile="profile" codec_type="type" sample_fmt="fltp" sample_rate="48000" channels="1" bit_rate="1000000" />
						    </streams>
						</ffprobe>
						""")
				.getStreams().stream().findFirst().orElseThrow();

		assertEquals("type: name profile mono @ 48000 Hz [1000 kbps]",
				MediaSummary.getAudioSummary(s));
	}

	@Test
	void testGetVideoSummary() {
		var s = FFprobeJAXB.load(
				"""
						<?xml version="1.0" encoding="UTF-8"?>
						<ffprobe>
						    <streams>
						        <stream index="0" codec_name="name" profile="profile" codec_type="type" bit_rate="1000000"
								width="2" height="3" has_b_frames="1" level="10" nb_frames="100" avg_frame_rate="25"
								/>
						    </streams>
						</ffprobe>
						""")
				.getStreams().stream().findFirst().orElseThrow();

		assertEquals("type: name 2×3 profile/L10 with B frames @ 25 fps [1000 kbps] (100 frms)",
				MediaSummary.getVideoSummary(s));

		s = FFprobeJAXB.load(
				"""
						<?xml version="1.0" encoding="UTF-8"?>
						<ffprobe>
						    <streams>
						        <stream index="0" codec_name="name" codec_type="type" bit_rate="1000000"
								width="2" height="3" has_b_frames="1" nb_frames="100" avg_frame_rate="25"
								/>
						    </streams>
						</ffprobe>
						""")
				.getStreams().stream().findFirst().orElseThrow();

		assertEquals("type: name 2×3 with B frames @ 25 fps [1000 kbps] (100 frms)",
				MediaSummary.getVideoSummary(s));

		s = FFprobeJAXB.load(
				"""
						<?xml version="1.0" encoding="UTF-8"?>
						<ffprobe>
						    <streams>
						        <stream index="0" codec_name="name" codec_type="type" level="10" bit_rate="1000000"
								width="2" height="3" has_b_frames="1" nb_frames="100" avg_frame_rate="25"
								/>
						    </streams>
						</ffprobe>
						""")
				.getStreams().stream().findFirst().orElseThrow();

		assertEquals("type: name 2×3 L10 with B frames @ 25 fps [1000 kbps] (100 frms)",
				MediaSummary.getVideoSummary(s));

		s = FFprobeJAXB.load(
				"""
						<?xml version="1.0" encoding="UTF-8"?>
						<ffprobe>
						    <streams>
						        <stream index="0" codec_name="name" codec_type="type" profile="0" level="10" bit_rate="1000000"
								width="2" height="3" has_b_frames="1" nb_frames="100" avg_frame_rate="25"
								/>
						    </streams>
						</ffprobe>
						""")
				.getStreams().stream().findFirst().orElseThrow();

		assertEquals("type: name 2×3 L10 with B frames @ 25 fps [1000 kbps] (100 frms)",
				MediaSummary.getVideoSummary(s));

		s = FFprobeJAXB.load(
				"""
						<?xml version="1.0" encoding="UTF-8"?>
						<ffprobe>
						    <streams>
						        <stream index="0" codec_name="name" codec_type="type" bit_rate="1000000"
								width="2" height="3" has_b_frames="1" nb_frames="100" avg_frame_rate="25"
								/>
						    </streams>
						</ffprobe>
						""")
				.getStreams().stream().findFirst().orElseThrow();

		assertEquals("type: name 2×3 with B frames @ 25 fps [1000 kbps] (100 frms)",
				MediaSummary.getVideoSummary(s));
	}

	@Test
	void testComputePixelsFormat() {
		var s = FFprobeJAXB.load(
				"""
						<?xml version="1.0" encoding="UTF-8"?>
						<ffprobe>
						    <streams>
						        <stream index="0" pix_fmt="pix"
								color_range="range" color_space="space" color_transfer="transfert" color_primaries="primaries" />
						    </streams>
						</ffprobe>
						""")
				.getStreams().stream().findFirst().orElseThrow();

		assertEquals("pix/colRange:RANGE/colSpace:SPACE/colTransfer:TRANSFERT/colPrimaries:PRIMARIES",
				MediaSummary.computePixelsFormat(s));

		s = FFprobeJAXB.load(
				"""
						<?xml version="1.0" encoding="UTF-8"?>
						<ffprobe>
						    <streams>
						        <stream index="0" pix_fmt="pix"
								color_range="range" color_space="space" color_transfer="transfert" />
						    </streams>
						</ffprobe>
						""")
				.getStreams().stream().findFirst().orElseThrow();

		assertEquals("pix/colRange:RANGE/colSpace:SPACE/colTransfer:TRANSFERT",
				MediaSummary.computePixelsFormat(s));

		s = FFprobeJAXB.load(
				"""
						<?xml version="1.0" encoding="UTF-8"?>
						<ffprobe>
						    <streams>
						        <stream index="0" pix_fmt="pix"
								color_range="range" color_space="space" />
						    </streams>
						</ffprobe>
						""")
				.getStreams().stream().findFirst().orElseThrow();

		assertEquals("pix/colRange:RANGE/colSpace:SPACE",
				MediaSummary.computePixelsFormat(s));

		s = FFprobeJAXB.load(
				"""
						<?xml version="1.0" encoding="UTF-8"?>
						<ffprobe>
						    <streams>
						        <stream index="0" pix_fmt="pix" color_range="range"  />
						    </streams>
						</ffprobe>
						""")
				.getStreams().stream().findFirst().orElseThrow();

		assertEquals("pix/colRange:RANGE",
				MediaSummary.computePixelsFormat(s));

		s = FFprobeJAXB.load(
				"""
						<?xml version="1.0" encoding="UTF-8"?>
						<ffprobe>
						    <streams>
						        <stream index="0" pix_fmt="pix" />
						    </streams>
						</ffprobe>
						""")
				.getStreams().stream().findFirst().orElseThrow();

		assertEquals("pix",
				MediaSummary.computePixelsFormat(s));

		s = FFprobeJAXB.load(
				"""
						<?xml version="1.0" encoding="UTF-8"?>
						<ffprobe>
						    <streams>
						        <stream index="0" />
						    </streams>
						</ffprobe>
						""")
				.getStreams().stream().findFirst().orElseThrow();

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
		var format = FFprobeJAXB.load(
				"""
						<?xml version="1.0" encoding="UTF-8"?>
						<ffprobe>
						<format duration="1" />
						</ffprobe>
						""")
				.getFormat().orElseThrow();

		assertEquals("00:00:01", MediaSummary.computeDuration(format));

		format = FFprobeJAXB.load(
				"""
						<?xml version="1.0" encoding="UTF-8"?>
						<ffprobe>
						<format duration="60" />
						</ffprobe>
						""")
				.getFormat().orElseThrow();

		assertEquals("00:01:00", MediaSummary.computeDuration(format));

		format = FFprobeJAXB.load(
				"""
						<?xml version="1.0" encoding="UTF-8"?>
						<ffprobe>
						<format duration="3600" />
						</ffprobe>
						""")
				.getFormat().orElseThrow();

		assertEquals("01:00:00", MediaSummary.computeDuration(format));

		format = FFprobeJAXB.load(
				"""
						<?xml version="1.0" encoding="UTF-8"?>
						<ffprobe>
						<format duration="36061" />
						</ffprobe>
						""")
				.getFormat().orElseThrow();

		assertEquals("10:01:01", MediaSummary.computeDuration(format));

		format = FFprobeJAXB.load(
				"""
						<?xml version="1.0" encoding="UTF-8"?>
						<ffprobe>
						<format duration="0.5" />
						</ffprobe>
						""")
				.getFormat().orElseThrow();

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
		final var source = FFprobeJAXB.load(
				"""
						<?xml version="1.0" encoding="UTF-8"?>
						<ffprobe>
						    <streams>
						        <stream index="0" codec_name="vp9" codec_long_name="Google VP9" profile="Profile 0" codec_type="video" codec_tag_string="[0][0][0][0]" codec_tag="0x0000" width="480" height="480" coded_width="480" coded_height="480" closed_captions="0" film_grain="0" has_b_frames="0" sample_aspect_ratio="1:1" display_aspect_ratio="1:1" pix_fmt="yuv420p" level="-99" color_range="tv" color_space="bt709" color_transfer="bt709" color_primaries="bt709" refs="1" r_frame_rate="30/1" avg_frame_rate="30000/1001" time_base="1/1000" start_pts="0" start_time="0.000000">
						            <disposition default="1" dub="0" original="0" comment="0" lyrics="0" karaoke="0" forced="0" hearing_impaired="0" visual_impaired="0" clean_effects="0" attached_pic="0" timed_thumbnails="0" captions="0" descriptions="0" metadata="0" dependent="0" still_image="0"/>
						        </stream>
						        <stream index="1" codec_name="opus" codec_long_name="Opus (Opus Interactive Audio Codec)" codec_type="audio" codec_tag_string="[0][0][0][0]" codec_tag="0x0000" sample_fmt="fltp" sample_rate="48000" channels="1" channel_layout="mono" bits_per_sample="0" r_frame_rate="0/0" avg_frame_rate="0/0" time_base="1/1000" start_pts="-7" start_time="-0.007000" extradata_size="19">
						            <disposition default="1" dub="0" original="0" comment="0" lyrics="0" karaoke="0" forced="0" hearing_impaired="0" visual_impaired="0" clean_effects="0" attached_pic="0" timed_thumbnails="0" captions="0" descriptions="0" metadata="0" dependent="0" still_image="0"/>
						        </stream>
						    </streams>
						    <chapters>
						    </chapters>
						    <format filename="aT0ElHA0NP4.webm" nb_streams="2" nb_programs="0" format_name="matroska,webm" format_long_name="Matroska / WebM" start_time="-0.007000" duration="60.081000" size="3113563" bit_rate="414582" probe_score="100">
						    </format>
						</ffprobe>
						""");
		ms = MediaSummary.create(source);
		assertNotNull(ms);

		assertEquals("Matroska / WebM, 00:01:00, 2 MB, 415 kbps", ms.format());
		assertEquals(List.of(
				"video: vp9 480×480 Profile 0 @ 29.97 fps yuv420p/colRange:TV/BT709 default",
				"audio: opus mono @ 48000 Hz default"),
				ms.streams());
	}

	@Test
	void testEmptyTC() throws IOException {
		final var source = FFprobeJAXB.load(
				"""
						<?xml version="1.0" encoding="UTF-8"?>
						<ffprobe>
						    <streams>
						        <stream index="2" codec_type="data" codec_tag_string="tmcd" codec_tag="0x64636d74" r_frame_rate="0/0" avg_frame_rate="50/1" time_base="1/50" start_pts="0" start_time="0.000000" duration_ts="2500" duration="50.000000" nb_frames="1">
						            <disposition default="1" dub="0" original="0" comment="0" lyrics="0" karaoke="0" forced="0" hearing_impaired="0" visual_impaired="0" clean_effects="0" attached_pic="0" timed_thumbnails="0"/>
						            <tag key="timecode" value="00:00:00:00"/>
						        </stream>
						    </streams>
						    <format filename="Test.mov" nb_streams="3" nb_programs="0" format_name="mov,mp4,m4a,3gp,3g2,mj2" format_long_name="QuickTime / MOV" start_time="0.000000" duration="50.000000" size="487700244" bit_rate="78032039" probe_score="100">
						    </format>
						</ffprobe>
						""");
		ms = MediaSummary.create(source);
		assertNotNull(ms);

		assertEquals("QuickTime / MOV, 00:00:50, 465 MB, 78 Mbps", ms.format());
		assertEquals(List.of("data: tmcd"), ms.streams());
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
