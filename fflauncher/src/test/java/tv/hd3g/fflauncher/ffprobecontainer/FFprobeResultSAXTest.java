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
package tv.hd3g.fflauncher.ffprobecontainer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static tv.hd3g.fflauncher.enums.ChannelLayout.MONO;
import static tv.hd3g.fflauncher.enums.ChannelLayout.STEREO;
import static tv.hd3g.fflauncher.ffprobecontainer.FFprobeCodecType.AUDIO;
import static tv.hd3g.fflauncher.ffprobecontainer.FFprobeCodecType.VIDEO;

import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import net.datafaker.Faker;
import tv.hd3g.fflauncher.recipes.ContainerAnalyserResult;
import tv.hd3g.fflauncher.recipes.ContainerAnalyserSession;
import tv.hd3g.processlauncher.ProcesslauncherLifecycle;

class FFprobeResultSAXTest {
	static Faker faker = net.datafaker.Faker.instance();

	static String XML = """
			<?xml version="1.0" encoding="UTF-8"?>
			<a><b>
			<packet codec_type="audio" stream_index="1" pts="2555904" pts_time="57.957007" dts="2555904" dts_time="57.957007" duration="1024" duration_time="0.023220" size="442" pos="84622585" flags="K_"/>
			<frame media_type="audio" stream_index="1" key_frame="1" pts="2555904" pts_time="57.957007" pkt_dts="2555904" pkt_dts_time="57.957007" best_effort_timestamp="2555904" best_effort_timestamp_time="57.957007" pkt_duration="1024" pkt_duration_time="0.023220" pkt_pos="84622585" pkt_size="442" sample_fmt="fltp" nb_samples="1024" channels="2" channel_layout="stereo"/>
			<packet codec_type="video" stream_index="0" pts="1738737" pts_time="57.957900" dts="1738737" dts_time="57.957900" duration="1001" duration_time="0.033367" size="75214" pos="84623027" flags="__"/>
			<frame media_type="video" stream_index="0" key_frame="0" pts="1738737" pts_time="57.957900" pkt_dts="1738737" pkt_dts_time="57.957900" best_effort_timestamp="1738737" best_effort_timestamp_time="57.957900" pkt_duration="1001" pkt_duration_time="0.033367" pkt_pos="84623027" pkt_size="75214" width="3840" height="2160" pix_fmt="yuv420p" sample_aspect_ratio="1:1" pict_type="P" coded_picture_number="0" display_picture_number="0" interlaced_frame="0" top_field_first="0" repeat_pict="0" color_range="tv" color_space="bt709" color_primaries="bt709" color_transfer="bt709"/>
			</b></a>
			""";

	FFprobeResultSAX s;
	ContainerAnalyserResult r;

	@Mock
	ProcesslauncherLifecycle source;
	@Mock
	ContainerAnalyserSession session;

	@BeforeEach
	void init() throws Exception {
		openMocks(this).close();
		s = new FFprobeResultSAX();
		when(source.toString()).thenReturn(faker.numerify("source###"));
	}

	@AfterEach
	void end() {
		verifyNoMoreInteractions(source, session);
	}

	@Test
	void testFull() {
		s.onProcessStart(IOUtils.toInputStream(XML, UTF_8), source);
		r = s.getResult(session);
		assertNotNull(r);

		assertEquals(List.of(
				new FFprobePacket(
						AUDIO, 1, 2555904l, 57.95701f, 2555904l, 57.95701f, 1024, 0.02322f, 442, 84622585l, "K_"),
				new FFprobePacket(
						VIDEO, 0, 1738737l, 57.9579f, 1738737l, 57.9579f, 1001, 0.033367f, 75214, 84623027l, "__")),
				r.packets());

		assertEquals(List.of(
				new FFprobeAudioFrame(
						new FFprobeBaseFrame(
								AUDIO, 1, true, 2555904l, 57.957007f, 2555904l, 57.957007f, 2555904l,
								57.957007f, 1024, 0.02322f, 84622585l, 442),
						1024)), r.audioFrames());

		assertEquals(List.of(
				new FFprobeVideoFrame(
						new FFprobeBaseFrame(
								VIDEO, 0, false, 1738737l, 57.957900f, 1738737l, 57.957900f, 1738737l,
								57.957900f, 1001, 0.033367f, 84623027l, 75214),
						FFprobePictType.P,
						false)), r.videoFrames());

		assertEquals(
				new FFprobeAudioFrameConst(r.audioFrames().get(0), "fltp", 2, STEREO),
				r.audioConst());

		assertEquals(
				new FFprobeVideoFrameConst(
						r.videoFrames().get(0),
						3840, 2160, "yuv420p", "1:1", 0, 0, false, false,
						"tv", "bt709", "bt709", "bt709"),
				r.videoConst());

		assertEquals(session, r.session());
		assertEquals(List.of(), r.olderAudioConsts());
		assertEquals(List.of(), r.olderVideoConsts());
	}

	@Test
	void testBadXML() {
		s.onProcessStart(IOUtils.toInputStream("NOPE XML !", UTF_8), source);
		verify(source, times(1)).kill();
	}

	@Test
	void testEmpty() {
		final var XML = """
				<?xml version="1.0" encoding="UTF-8"?>
				<a><b>
				<packet nope1="nothing" />
				<frame />
				<frame media_type="video" a0="" a1="" a2="" a3="" a4="" a5="" a6="" a7="" a8="" a9="" a10="" a11="" a12="" a13=""  />
				<frame media_type="audio" a0="" a1="" a2="" a3="" a4="" a5="" a6="" a7="" a8="" a9="" a10="" a11="" a12="" a13=""  />
				<frame media_type="other" a0="" a1="" a2="" a3="" a4="" a5="" a6="" a7="" a8="" a9="" a10="" a11="" a12="" a13=""  />
				</b></a>
				""";

		s.onProcessStart(IOUtils.toInputStream(XML, UTF_8), source);
		r = s.getResult(session);
		assertNotNull(r);

		assertEquals(List.of(), r.packets());
		assertEquals(List.of(), r.audioFrames());
		assertEquals(List.of(), r.videoFrames());
		assertNull(r.audioConst());
		assertNull(r.videoConst());
		assertEquals(session, r.session());
		assertEquals(List.of(), r.olderAudioConsts());
		assertEquals(List.of(), r.olderVideoConsts());
	}

	@Test
	void testUpdConsts() {
		final var XML = """
				<?xml version="1.0" encoding="UTF-8"?>
				<a><b>
				<frame media_type="audio" stream_index="1" key_frame="1" pts="2555904" pts_time="57.957007" pkt_dts="2555904" pkt_dts_time="57.957007" best_effort_timestamp="2555904" best_effort_timestamp_time="57.957007" pkt_duration="1024" pkt_duration_time="0.023220" pkt_pos="84622585" pkt_size="442" sample_fmt="fuuu" nb_samples="1024" channels="1" channel_layout="mono"/>
				<frame media_type="audio" stream_index="1" key_frame="1" pts="2555904" pts_time="57.957007" pkt_dts="2555904" pkt_dts_time="57.957007" best_effort_timestamp="2555904" best_effort_timestamp_time="57.957007" pkt_duration="1024" pkt_duration_time="0.023220" pkt_pos="84622585" pkt_size="442" sample_fmt="fltp" nb_samples="1024" channels="2" channel_layout="stereo"/>
				<frame media_type="video" stream_index="0" key_frame="0" pts="1738737" pts_time="57.957900" pkt_dts="1738737" pkt_dts_time="57.957900" best_effort_timestamp="1738737" best_effort_timestamp_time="57.957900" pkt_duration="1001" pkt_duration_time="0.033367" pkt_pos="84623027" pkt_size="75214" width="1920" height="1080" pix_fmt="yuv422i" sample_aspect_ratio="16:9" pict_type="P" coded_picture_number="0" display_picture_number="1" interlaced_frame="1" top_field_first="1" repeat_pict="0" color_range="cinema" color_space="bt2020" color_primaries="bt2020" color_transfer="bt2020"/>
				<frame media_type="video" stream_index="0" key_frame="0" pts="1738737" pts_time="57.957900" pkt_dts="1738737" pkt_dts_time="57.957900" best_effort_timestamp="1738737" best_effort_timestamp_time="57.957900" pkt_duration="1001" pkt_duration_time="0.033367" pkt_pos="84623027" pkt_size="75214" width="3840" height="2160" pix_fmt="yuv420p" sample_aspect_ratio="1:1" pict_type="P" coded_picture_number="0" display_picture_number="0" interlaced_frame="0" top_field_first="0" repeat_pict="0" color_range="tv" color_space="bt709" color_primaries="bt709" color_transfer="bt709"/>
				</b></a>
				""";

		s.onProcessStart(IOUtils.toInputStream(XML, UTF_8), source);
		r = s.getResult(session);
		assertNotNull(r);

		assertEquals(
				new FFprobeAudioFrameConst(r.audioFrames().get(0), "fltp", 2, STEREO),
				r.audioConst());

		assertEquals(
				new FFprobeVideoFrameConst(
						r.videoFrames().get(0),
						3840, 2160, "yuv420p", "1:1", 0, 0, false, false,
						"tv", "bt709", "bt709", "bt709"),
				r.videoConst());

		assertEquals(session, r.session());
		assertEquals(List.of(
				new FFprobeAudioFrameConst(r.audioFrames().get(0), "fuuu", 1, MONO)),
				r.olderAudioConsts());
		assertEquals(List.of(
				new FFprobeVideoFrameConst(
						r.videoFrames().get(0),
						1920, 1080, "yuv422i", "16:9", 0, 1, true, true,
						"cinema", "bt2020", "bt2020", "bt2020")),
				r.olderVideoConsts());
	}

}
