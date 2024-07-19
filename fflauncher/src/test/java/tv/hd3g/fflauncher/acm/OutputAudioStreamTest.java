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

import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tv.hd3g.fflauncher.acm.InputAudioChannelSelector.IN_CH0;
import static tv.hd3g.fflauncher.acm.InputAudioChannelSelector.IN_CH1;
import static tv.hd3g.fflauncher.acm.InputAudioChannelSelector.IN_CH2;
import static tv.hd3g.fflauncher.acm.OutputAudioChannelSelector.OUT_CH0;
import static tv.hd3g.fflauncher.acm.OutputAudioChannelSelector.OUT_CH1;
import static tv.hd3g.fflauncher.acm.OutputAudioChannelSelector.OUT_CH2;
import static tv.hd3g.fflauncher.enums.ChannelLayout.CH5_1_SIDE;
import static tv.hd3g.fflauncher.enums.ChannelLayout.MONO;
import static tv.hd3g.fflauncher.enums.ChannelLayout.STEREO;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import tv.hd3g.fflauncher.acm.OutputAudioStream.OutputAudioChannel;

class OutputAudioStreamTest {
	static final InputAudioStream inputAudioStream = new InputAudioStream(STEREO, 0, 1);

	OutputAudioStream audioStream;

	@BeforeEach
	void init() {
		audioStream = new OutputAudioStream(STEREO, 0, 1);
	}

	@Test
	void testEqualsObject() {
		assertEquals(new OutputAudioStream(CH5_1_SIDE, 0, 1),
				new OutputAudioStream(CH5_1_SIDE, 0, 1));
	}

	@Test
	void testOutputAudioStream() {
		assertEquals(STEREO, audioStream.getLayout());
		assertEquals(0, audioStream.getFileIndex());
		assertEquals(1, audioStream.getStreamIndex());
	}

	@Test
	void testGetChannels() {
		assertNotNull(audioStream.getChannels());
		assertTrue(audioStream.getChannels().isEmpty());
	}

	@Test
	void testMapReferenceAsInput() {
		assertNull(audioStream.toMapReferenceAsInput());
		final var mapReference = String.valueOf(System.nanoTime());
		audioStream.setMapReference(mapReference);
		assertEquals(mapReference, audioStream.toMapReferenceAsInput());
	}

	@Test
	void testToString() {
		assertEquals("0:1(stereo)", audioStream.toString());

		final var mapReference = String.valueOf(System.nanoTime());
		audioStream.setMapReference(mapReference);
		assertEquals("0:1(stereo)" + mapReference, audioStream.toString());
	}

	@Test
	void testAddChannel() {
		final var channel = Mockito.mock(OutputAudioChannel.class);
		audioStream.addChannel(channel);
		Mockito.verifyNoInteractions(channel);
		assertEquals(1, audioStream.getChannels().size());
		assertEquals(channel, audioStream.getChannels().stream().findFirst().orElse(null));
	}

	@Test
	void testCompareTo() {
		final var streams = List.of(new OutputAudioStream(STEREO, 1, 1),
				new OutputAudioStream(STEREO, 0, 0),
				new OutputAudioStream(MONO, 0, 1),
				new OutputAudioStream(STEREO, 1, 0)).stream()
				.sorted()
				.map(OutputAudioStream::toString)
				.collect(joining(","));
		assertEquals("0:0(stereo),0:1(mono),1:0(stereo),1:1(stereo)", streams);
	}

	@Test
	void testMapChannel_full() {
		final var s = new InputAudioStream(STEREO, 0, 1);
		assertThrows(IllegalArgumentException.class, () -> audioStream.mapChannel(s, IN_CH2, OUT_CH0));
		assertEquals(audioStream, audioStream.mapChannel(s, IN_CH0, OUT_CH0));
		assertEquals(audioStream, audioStream.mapChannel(s, IN_CH1, OUT_CH1));
		assertThrows(IllegalArgumentException.class, () -> audioStream.mapChannel(s, IN_CH1, OUT_CH2));
		assertEquals(2, audioStream.getChannels().size());
		final var channels = audioStream.getChannels().stream()
				.sorted()
				.map(OutputAudioChannel::toString)
				.collect(joining(","));
		assertEquals("0:1(stereo).CHIN_0->0:1(stereo).CHOUT_0,0:1(stereo).CHIN_1->0:1(stereo).CHOUT_1", channels);
	}

	@Test
	void testMapChannel_simpleordered() {
		final var s = new InputAudioStream(STEREO, 0, 1);
		assertThrows(IllegalArgumentException.class, () -> audioStream.mapChannel(s, IN_CH2));
		assertEquals(audioStream, audioStream.mapChannel(s, IN_CH1));
		assertEquals(audioStream, audioStream.mapChannel(s, IN_CH0));
		assertThrows(IllegalArgumentException.class, () -> audioStream.mapChannel(s, IN_CH1));
		assertEquals(2, audioStream.getChannels().size());
		final var channels = audioStream.getChannels().stream()
				.sorted()
				.map(OutputAudioChannel::toString)
				.collect(joining(","));
		assertEquals("0:1(stereo).CHIN_1->0:1(stereo).CHOUT_0,0:1(stereo).CHIN_0->0:1(stereo).CHOUT_1", channels);
	}

	@Nested
	class TestOutputAudioChannel {

		OutputAudioChannel channel;

		@BeforeEach
		void init() {
			channel = audioStream.new OutputAudioChannel(inputAudioStream, IN_CH0, audioStream, OUT_CH0);
		}

		@Test
		void testOutputAudioChannel() {
			assertThrows(IllegalArgumentException.class,
					() -> audioStream.new OutputAudioChannel(inputAudioStream, IN_CH2, audioStream, OUT_CH0));
			assertThrows(IllegalArgumentException.class,
					() -> audioStream.new OutputAudioChannel(inputAudioStream, IN_CH0, audioStream, OUT_CH2));
		}

		@Test
		void testCompareTo() {
			final var audioStream2 = new OutputAudioStream(MONO, 0, 0);

			final var channels = List.of(
					audioStream.new OutputAudioChannel(inputAudioStream, IN_CH1, audioStream, OUT_CH1),
					audioStream.new OutputAudioChannel(inputAudioStream, IN_CH1, audioStream, OUT_CH0),
					audioStream2.new OutputAudioChannel(inputAudioStream, IN_CH0, audioStream2, OUT_CH0),
					audioStream.new OutputAudioChannel(inputAudioStream, IN_CH0, audioStream, OUT_CH1),
					audioStream.new OutputAudioChannel(inputAudioStream, IN_CH0, audioStream, OUT_CH0)).stream()
					.sorted()
					.map(OutputAudioChannel::toString)
					.toList();
			assertEquals(
					List.of("0:1(stereo).CHIN_0->0:0(mono).CHOUT_0",
							"0:1(stereo).CHIN_1->0:1(stereo).CHOUT_0",
							"0:1(stereo).CHIN_0->0:1(stereo).CHOUT_0",
							"0:1(stereo).CHIN_1->0:1(stereo).CHOUT_1",
							"0:1(stereo).CHIN_0->0:1(stereo).CHOUT_1"),
					channels);
		}

		@Test
		void testGetInputAudioStream() {
			assertEquals(inputAudioStream, channel.getInputAudioStream());
		}

		@Test
		void testGetChInIndex() {
			assertEquals(IN_CH0, channel.getChInIndex());
		}

		@Test
		void testGetOutputAudioStream() {
			assertEquals(audioStream, channel.getOutputAudioStream());
		}

		@Test
		void testGetChOutIndex() {
			assertEquals(OUT_CH0, channel.getChOutIndex());
		}

		@Test
		void testEquals() {
			final var inputAudioStream0 = new InputAudioStream(STEREO, 0, 1);
			final var audioStream0 = new OutputAudioStream(STEREO, 0, 1);
			final var inputAudioStream1 = new InputAudioStream(STEREO, 1, 1);
			final var audioStream1 = new OutputAudioStream(STEREO, 1, 1);

			assertEquals(audioStream0.new OutputAudioChannel(inputAudioStream0, IN_CH0, audioStream0, OUT_CH0),
					channel);
			assertNotEquals(audioStream0.new OutputAudioChannel(
					inputAudioStream0, IN_CH1, audioStream0, OUT_CH0), channel);
			assertNotEquals(audioStream0.new OutputAudioChannel(
					inputAudioStream0, IN_CH0, audioStream0, OUT_CH1), channel);
			assertNotEquals(audioStream0.new OutputAudioChannel(
					inputAudioStream1, IN_CH0, audioStream0, OUT_CH0), channel);
			assertNotEquals(audioStream0.new OutputAudioChannel(
					inputAudioStream0, IN_CH0, audioStream1, OUT_CH0), channel);
		}

		@Test
		void testToString() {
			assertEquals("0:1(stereo).CHIN_0->0:1(stereo).CHOUT_0", channel.toString());
		}

		@Test
		void testGetEnclosingInstance() {
			assertEquals(audioStream, channel.getEnclosingInstance());
		}
	}
}
