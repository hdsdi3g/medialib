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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.mockito.internal.verification.VerificationModeFactory.atLeastOnce;
import static org.mockito.internal.verification.VerificationModeFactory.atMostOnce;
import static tv.hd3g.fflauncher.ffprobecontainer.FFprobePictType.B;
import static tv.hd3g.fflauncher.ffprobecontainer.FFprobePictType.I;
import static tv.hd3g.fflauncher.ffprobecontainer.FFprobePictType.P;
import static tv.hd3g.fflauncher.ffprobecontainer.FFprobePictType.UNKNOWN;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import tv.hd3g.fflauncher.ffprobecontainer.FFprobeAudioFrame;
import tv.hd3g.fflauncher.ffprobecontainer.FFprobeAudioFrameConst;
import tv.hd3g.fflauncher.ffprobecontainer.FFprobeBaseFrame;
import tv.hd3g.fflauncher.ffprobecontainer.FFprobePacket;
import tv.hd3g.fflauncher.ffprobecontainer.FFprobeVideoFrame;
import tv.hd3g.fflauncher.ffprobecontainer.FFprobeVideoFrameConst;

class ContainerAnalyserResultTest {

	ContainerAnalyserResult r;

	@Mock
	ContainerAnalyserSession session;
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

	@BeforeEach
	void init() throws Exception {
		openMocks(this).close();
		when(packets.isEmpty()).thenReturn(true);
		when(audioFrames.isEmpty()).thenReturn(true);
		when(videoFrames.isEmpty()).thenReturn(true);
		when(olderVideoConsts.isEmpty()).thenReturn(true);
		when(olderAudioConsts.isEmpty()).thenReturn(true);

		r = new ContainerAnalyserResult(
				session,
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

		verifyNoMoreInteractions(
				session,
				packets,
				audioFrames,
				videoFrames,
				videoConst,
				audioConst,
				olderVideoConsts,
				olderAudioConsts,
				frameI,
				frameP,
				frameB);
	}

	@Test
	void testIsEmpty_realEmpty() {
		assertTrue(r.isEmpty());
	}

	@Test
	void testIsEmpty_partial_packets() {
		when(packets.isEmpty()).thenReturn(false);

		r = new ContainerAnalyserResult(
				session,
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

		r = new ContainerAnalyserResult(
				session,
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

		r = new ContainerAnalyserResult(
				session,
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

		r = new ContainerAnalyserResult(
				session,
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

		r = new ContainerAnalyserResult(
				session,
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
		r = new ContainerAnalyserResult(
				session,
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
		r = new ContainerAnalyserResult(
				session,
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
			verifyNoInteractions(session);
			reset(videoFrames, session);
		}

	}

}
