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
package tv.hd3g.fflauncher.acm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;
import static tv.hd3g.fflauncher.acm.InputAudioChannelSelector.IN_CH12;
import static tv.hd3g.fflauncher.enums.ChannelLayout.CH5_1;
import static tv.hd3g.fflauncher.enums.ChannelLayout.MONO;
import static tv.hd3g.fflauncher.enums.ChannelLayout.STEREO;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import tv.hd3g.ffprobejaxb.FFprobeJAXB;
import tv.hd3g.ffprobejaxb.data.FFProbeStream;

class InputAudioStreamTest {

	InputAudioStream audioStream = new InputAudioStream(STEREO, 0, 1);

	@Test
	void testInputAudioStream() {
		assertEquals(STEREO, audioStream.getLayout());
		assertEquals(0, audioStream.getFileIndex());
		assertEquals(1, audioStream.getStreamIndex());
	}

	@Test
	void testToMapReferenceAsInput() {
		assertEquals("0:1", audioStream.toMapReferenceAsInput());
	}

	@Test
	void testToString() {
		assertEquals("0:1(stereo)", audioStream.toString());
	}

	@Test
	void testGetListFromAnalysisFFprobeJAXBArray() {
		final var file0 = Mockito.mock(FFprobeJAXB.class);
		final var stream00 = Mockito.mock(FFProbeStream.class);
		when(stream00.index()).thenReturn(1);
		when(stream00.channelLayout()).thenReturn("5.1");
		when(stream00.channels()).thenReturn(6);

		Mockito.when(file0.getAudioStreams()).thenReturn(Stream.of(stream00));

		final var streamList = InputAudioStream.getListFromAnalysis(file0);
		assertNotNull(streamList);
		assertEquals(1, streamList.size());
		assertEquals(new InputAudioStream(CH5_1, 0, 1), streamList.get(0));
	}

	@Test
	void testGetListFromAnalysisListOfFFprobeJAXB() {
		final var file0 = Mockito.mock(FFprobeJAXB.class);

		final var stream00 = Mockito.mock(FFProbeStream.class);
		when(stream00.index()).thenReturn(1);
		when(stream00.channelLayout()).thenReturn("stereo");
		when(stream00.channels()).thenReturn(2);

		final var stream01 = Mockito.mock(FFProbeStream.class);
		when(stream01.index()).thenReturn(2);
		when(stream01.channelLayout()).thenReturn("stereo");
		when(stream01.channels()).thenReturn(2);

		Mockito.when(file0.getAudioStreams()).thenReturn(Stream.of(stream00, stream01));

		final var file1 = Mockito.mock(FFprobeJAXB.class);
		final var stream1 = Mockito.mock(FFProbeStream.class);
		when(stream1.index()).thenReturn(0);
		when(stream1.channelLayout()).thenReturn("");
		when(stream1.channels()).thenReturn(1);

		Mockito.when(file1.getAudioStreams()).thenReturn(Stream.of(stream1));

		final var streamList = InputAudioStream.getListFromAnalysis(List.of(file0, file1));
		assertNotNull(streamList);
		assertEquals(3, streamList.size());

		assertEquals(new InputAudioStream(STEREO, 0, 1), streamList.get(0));
		assertEquals(new InputAudioStream(STEREO, 0, 2), streamList.get(1));
		assertEquals(new InputAudioStream(MONO, 1, 0), streamList.get(2));
	}

	@Test
	void testGetFromRelativeIndexes() {
		final var audioStream0 = new InputAudioStream(STEREO, 0, 1);
		final var audioStream1 = new InputAudioStream(STEREO, 0, 2);
		final var audioStream2 = new InputAudioStream(STEREO, 0, 3);
		final var audioStream3 = new InputAudioStream(STEREO, 1, 2);
		final var audioStream4 = new InputAudioStream(STEREO, 1, 3);
		final var audioStream5 = new InputAudioStream(STEREO, 1, 4);
		final var list = List.of(audioStream0, audioStream1, audioStream2, audioStream3, audioStream4, audioStream5);

		assertEquals(audioStream0, InputAudioStream.getFromRelativeIndexes(list, 0, 0));
		assertNull(InputAudioStream.getFromRelativeIndexes(list, 5, 0));
		assertEquals(audioStream4, InputAudioStream.getFromRelativeIndexes(list, 1, 1));
		assertEquals(audioStream2, InputAudioStream.getFromRelativeIndexes(list, 0, 2));
	}

	@Test
	void testSelectedInputChannel() {
		final var sic = new ACMSelectedInputChannel(audioStream, IN_CH12);
		assertEquals(audioStream, sic.inputAudioStream());
		assertEquals(IN_CH12, sic.channelSelector());
	}

	@Test
	void testGetFromAbsoluteIndex() {
		final var audioStream0 = new InputAudioStream(STEREO, 0, 1);
		final var audioStream1 = new InputAudioStream(MONO, 0, 2);
		final var list = List.of(audioStream0, audioStream1);
		final var ch0 = InputAudioStream.getFromAbsoluteIndex(list, 0);
		final var ch1 = InputAudioStream.getFromAbsoluteIndex(list, 1);
		final var ch2 = InputAudioStream.getFromAbsoluteIndex(list, 2);
		final var ch3 = InputAudioStream.getFromAbsoluteIndex(list, 3);

		assertEquals(audioStream0, ch0.inputAudioStream());
		assertEquals(InputAudioChannelSelector.IN_CH0, ch0.channelSelector());
		assertEquals(audioStream0, ch1.inputAudioStream());
		assertEquals(InputAudioChannelSelector.IN_CH1, ch1.channelSelector());
		assertEquals(audioStream1, ch2.inputAudioStream());
		assertEquals(InputAudioChannelSelector.IN_CH0, ch2.channelSelector());
		assertNull(ch3);
	}
}
