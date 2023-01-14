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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.mockito.internal.verification.VerificationModeFactory.atMostOnce;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import tv.hd3g.fflauncher.ffprobecontainer.FFprobeAudioFrame;
import tv.hd3g.fflauncher.ffprobecontainer.FFprobeAudioFrameConst;
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
				olderAudioConsts);
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
				olderAudioConsts);
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
				olderAudioConsts);
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
				olderAudioConsts);
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
				olderAudioConsts);
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
				olderAudioConsts);
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
				olderAudioConsts);
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
				olderAudioConsts);
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
				olderAudioConsts);
		assertFalse(r.isEmpty());
	}
}
