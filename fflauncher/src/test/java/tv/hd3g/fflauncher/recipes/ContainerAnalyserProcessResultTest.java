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
package tv.hd3g.fflauncher.recipes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.mockito.internal.verification.VerificationModeFactory.atLeastOnce;
import static org.mockito.internal.verification.VerificationModeFactory.atMostOnce;
import static tv.hd3g.fflauncher.ffprobecontainer.FFprobePictType.B;
import static tv.hd3g.fflauncher.ffprobecontainer.FFprobePictType.I;
import static tv.hd3g.fflauncher.ffprobecontainer.FFprobePictType.P;
import static tv.hd3g.fflauncher.ffprobecontainer.FFprobePictType.UNKNOWN;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import tv.hd3g.commons.testtools.Fake;
import tv.hd3g.commons.testtools.MockToolsExtendsJunit;
import tv.hd3g.fflauncher.ffprobecontainer.FFprobeAudioFrame;
import tv.hd3g.fflauncher.ffprobecontainer.FFprobeAudioFrameConst;
import tv.hd3g.fflauncher.ffprobecontainer.FFprobeBaseFrame;
import tv.hd3g.fflauncher.ffprobecontainer.FFprobePacket;
import tv.hd3g.fflauncher.ffprobecontainer.FFprobeVideoFrame;
import tv.hd3g.fflauncher.ffprobecontainer.FFprobeVideoFrameConst;

@ExtendWith(MockToolsExtendsJunit.class)
class ContainerAnalyserProcessResultTest {

	ContainerAnalyserProcessResult r;

	@Mock
	List<FFprobePacket> packets;
	@Mock
	List<FFprobeAudioFrame> audioFrames;
	@Mock
	List<FFprobeVideoFrame> videoFrames;
	@Mock
	FFprobeVideoFrameConst videoConst;
	@Mock
	FFprobeAudioFrameConst audioConst;
	@Mock
	List<FFprobeVideoFrameConst> olderVideoConsts;
	@Mock
	List<FFprobeAudioFrameConst> olderAudioConsts;
	@Mock
	FFprobeBaseFrame frameI;
	@Mock
	FFprobeBaseFrame frameP;
	@Mock
	FFprobeBaseFrame frameB;

	@Fake
	String ffprobeCommandLine;

	@BeforeEach
	void init() throws Exception {
		openMocks(this).close();
		when(packets.isEmpty()).thenReturn(true);
		when(audioFrames.isEmpty()).thenReturn(true);
		when(videoFrames.isEmpty()).thenReturn(true);
		when(olderVideoConsts.isEmpty()).thenReturn(true);
		when(olderAudioConsts.isEmpty()).thenReturn(true);

		r = new ContainerAnalyserProcessResult(
				packets,
				audioFrames,
				videoFrames,
				null,
				null,
				olderVideoConsts,
				olderAudioConsts,
				null);
	}

	@AfterEach
	void ends() {
		verify(packets, atMostOnce()).isEmpty();
		verify(audioFrames, atMostOnce()).isEmpty();
		verify(videoFrames, atMostOnce()).isEmpty();
		verify(olderVideoConsts, atMostOnce()).isEmpty();
		verify(olderAudioConsts, atMostOnce()).isEmpty();
	}

	@Test
	void testIsEmpty_realEmpty() {
		assertTrue(r.isEmpty());
	}

	@Test
	void testIsEmpty_partial_packets() {
		when(packets.isEmpty()).thenReturn(false);

		r = new ContainerAnalyserProcessResult(
				packets,
				audioFrames,
				videoFrames,
				null,
				null,
				olderVideoConsts,
				olderAudioConsts,
				null);
		assertFalse(r.isEmpty());
	}

	@Test
	void testIsEmpty_partial_audioFrames() {
		when(audioFrames.isEmpty()).thenReturn(false);

		r = new ContainerAnalyserProcessResult(
				packets,
				audioFrames,
				videoFrames,
				null,
				null,
				olderVideoConsts,
				olderAudioConsts,
				null);
		assertFalse(r.isEmpty());
	}

	@Test
	void testIsEmpty_partial_videoFrames() {
		when(videoFrames.isEmpty()).thenReturn(false);

		r = new ContainerAnalyserProcessResult(
				packets,
				audioFrames,
				videoFrames,
				null,
				null,
				olderVideoConsts,
				olderAudioConsts,
				null);
		assertFalse(r.isEmpty());
	}

	@Test
	void testIsEmpty_partial_olderVideoConsts() {
		when(olderVideoConsts.isEmpty()).thenReturn(false);

		r = new ContainerAnalyserProcessResult(
				packets,
				audioFrames,
				videoFrames,
				null,
				null,
				olderVideoConsts,
				olderAudioConsts,
				null);
		assertFalse(r.isEmpty());
	}

	@Test
	void testIsEmpty_partial_olderAudioConsts() {
		when(olderAudioConsts.isEmpty()).thenReturn(false);

		r = new ContainerAnalyserProcessResult(
				packets,
				audioFrames,
				videoFrames,
				null,
				null,
				olderVideoConsts,
				olderAudioConsts,
				null);
		assertFalse(r.isEmpty());
	}

	@Test
	void testIsEmpty_partial_videoConst() {
		r = new ContainerAnalyserProcessResult(
				packets,
				audioFrames,
				videoFrames,
				videoConst,
				null,
				olderVideoConsts,
				olderAudioConsts,
				null);
		assertFalse(r.isEmpty());
	}

	@Test
	void testIsEmpty_partial_audioConst() {
		r = new ContainerAnalyserProcessResult(
				packets,
				audioFrames,
				videoFrames,
				null,
				audioConst,
				olderVideoConsts,
				olderAudioConsts,
				null);
		assertFalse(r.isEmpty());
	}

	@Nested
	class ExtractGOPStats {

		@Test
		void test_empty() {
			assertNotNull(r.extractGOPStats());
			assertTrue(r.extractGOPStats().isEmpty());
			verify(videoFrames, times(2)).isEmpty();
		}

		@Test
		void test_onlyI() {
			final var v0 = new FFprobeVideoFrame(frameI, I, false);
			final var v1 = new FFprobeVideoFrame(frameI, UNKNOWN, false);
			when(videoFrames.isEmpty()).thenReturn(false);
			when(videoFrames.stream()).then(i -> Stream.of(v0, v0, v1));

			assertNotNull(r.extractGOPStats());
			assertTrue(r.extractGOPStats().isEmpty());

			verify(videoFrames, times(2)).isEmpty();
			verify(videoFrames, atLeastOnce()).stream();
		}

		@Test
		void test_IPB() {
			final var vI = new FFprobeVideoFrame(frameI, I, false);
			final var vP = new FFprobeVideoFrame(frameP, P, false);
			final var vB = new FFprobeVideoFrame(frameB, B, false);
			when(videoFrames.isEmpty()).thenReturn(false);
			when(videoFrames.stream()).then(i -> Stream.of(vI, vP, vB));
			when(frameI.pktSize()).thenReturn(300);
			when(frameP.pktSize()).thenReturn(200);
			when(frameB.pktSize()).thenReturn(100);

			assertEquals(List.of(
					new GOPStatItem(3, 600, 1, 300, 100, List.of(vI, vP, vB))),
					r.extractGOPStats());

			verify(frameI, atLeastOnce()).pktSize();
			verify(frameP, atLeastOnce()).pktSize();
			verify(frameB, atLeastOnce()).pktSize();
			verify(videoFrames, times(1)).isEmpty();
			verify(videoFrames, atLeastOnce()).stream();
		}

		@Test
		void test_IPPIBB() {
			final var vI = new FFprobeVideoFrame(frameI, I, false);
			final var vP = new FFprobeVideoFrame(frameP, P, false);
			final var vB = new FFprobeVideoFrame(frameB, B, false);
			when(videoFrames.isEmpty()).thenReturn(false);
			when(videoFrames.stream()).then(i -> Stream.of(vI, vP, vP, vI, vB, vB));
			when(frameI.pktSize()).thenReturn(300);
			when(frameP.pktSize()).thenReturn(200);
			when(frameB.pktSize()).thenReturn(100);

			assertEquals(List.of(
					new GOPStatItem(3, 700, 0, 300, 0, List.of(vI, vP, vP)),
					new GOPStatItem(3, 500, 2, 300, 200, List.of(vI, vB, vB))),
					r.extractGOPStats());

			verify(frameI, atLeastOnce()).pktSize();
			verify(frameP, atLeastOnce()).pktSize();
			verify(frameB, atLeastOnce()).pktSize();
			verify(videoFrames, times(1)).isEmpty();
			verify(videoFrames, atLeastOnce()).stream();
		}

		@AfterEach
		void ends() {
			reset((Object) videoFrames);
		}

	}

	@Test
	void testImportFromOffline() {
		final var xml = """
				<?xml version="1.0" encoding="UTF-8"?>
				<ffprobe>
				    <packets_and_frames>
				        <packet codec_type="video" stream_index="0" pts="0" pts_time="0.000000" dts="0" dts_time="0.000000" duration="640" duration_time="0.040000" size="678" pos="36" flags="K__"/>
				        <frame media_type="video" stream_index="0" key_frame="1" pts="0" pts_time="0.000000" pkt_dts="0" pkt_dts_time="0.000000" best_effort_timestamp="0" best_effort_timestamp_time="0.000000" duration="640" duration_time="0.040000" pkt_pos="36" pkt_size="678" width="352" height="288" crop_top="0" crop_bottom="0" crop_left="0" crop_right="0" pix_fmt="yuv420p" sample_aspect_ratio="1:1" pict_type="I" interlaced_frame="0" top_field_first="0" repeat_pict="0"/>
				        <packet codec_type="audio" stream_index="1" pts="0" pts_time="0.000000" dts="0" dts_time="0.000000" duration="1024" duration_time="0.021333" size="4096" pos="714" flags="K__"/>
				        <frame media_type="audio" stream_index="1" key_frame="1" pts="0" pts_time="0.000000" pkt_dts="0" pkt_dts_time="0.000000" best_effort_timestamp="0" best_effort_timestamp_time="0.000000" duration="1024" duration_time="0.021333" pkt_pos="714" pkt_size="4096" sample_fmt="s16" nb_samples="1024" channels="2" channel_layout="stereo"/>
				    </packets_and_frames>
				</ffprobe>
				""";
		final var bais = new ByteArrayInputStream(xml.getBytes());
		final var result = ContainerAnalyserProcessResult.importFromOffline(bais, ffprobeCommandLine);
		assertNotNull(result);
		assertThat(result.audioFrames()).size().isEqualTo(1);
		assertThat(result.videoFrames()).size().isEqualTo(1);
		assertThat(result.packets()).size().isEqualTo(2);
	}

}
