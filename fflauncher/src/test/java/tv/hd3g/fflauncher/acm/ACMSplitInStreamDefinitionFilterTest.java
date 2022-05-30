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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static tv.hd3g.fflauncher.acm.InputAudioChannelSelector.IN_CH0;
import static tv.hd3g.fflauncher.acm.InputAudioChannelSelector.IN_CH1;
import static tv.hd3g.fflauncher.acm.OutputAudioChannelSelector.OUT_CH0;
import static tv.hd3g.fflauncher.enums.ChannelLayout.MONO;
import static tv.hd3g.fflauncher.enums.ChannelLayout.STEREO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tv.hd3g.fflauncher.acm.OutputAudioStream.OutputAudioChannel;

class ACMSplitInStreamDefinitionFilterTest {

	static final InputAudioStream inputAudioStream = new InputAudioStream(STEREO, 0, 1);
	static final OutputAudioStream firstOutputAudioStream = new OutputAudioStream(MONO, 0, 0);
	static final OutputAudioStream secondOutputAudioStream = new OutputAudioStream(MONO, 0, 1);
	static final OutputAudioChannelSelector chOutIndex = OUT_CH0;
	static final OutputAudioChannel firstOutputAudioChannel = firstOutputAudioStream.new OutputAudioChannel(
	        inputAudioStream, IN_CH0, firstOutputAudioStream, chOutIndex);
	static final OutputAudioChannel secondOutputAudioChannel = secondOutputAudioStream.new OutputAudioChannel(
	        inputAudioStream, IN_CH1, secondOutputAudioStream, chOutIndex);

	ACMSplitInStreamDefinitionFilter acmFilter;

	@BeforeEach
	void init() {
		acmFilter = new ACMSplitInStreamDefinitionFilter(firstOutputAudioChannel, 0);
	}

	@Test
	void testToString() {
		assertNotNull(acmFilter.toString());
	}

	@Test
	void testGetInputAudioStream() {
		assertNotNull(acmFilter.getInputAudioStream());
		assertEquals(inputAudioStream, acmFilter.getInputAudioStream());
	}

	@Test
	void testGetSplittedOut() {
		assertNotNull(acmFilter.getSplittedOut());
		assertEquals(1, acmFilter.getSplittedOut().size());
		assertNotNull(acmFilter.getSplittedOut().get(IN_CH0));
		final var sOut0 = acmFilter.getSplittedOut().get(IN_CH0);

		assertNotNull(sOut0.getInStream());
		assertEquals(inputAudioStream, sOut0.getInStream());

		assertNotNull(sOut0.getOutputAudioChannel());
		assertEquals(firstOutputAudioChannel, sOut0.getOutputAudioChannel());

		assertNotNull(sOut0.getLinkableOutStreamReference());
		assertEquals(firstOutputAudioStream, sOut0.getLinkableOutStreamReference());

		assertNotNull(sOut0.toMapReferenceAsInput());
		assertEquals("split0", sOut0.toMapReferenceAsInput());

		final var sOut1 = acmFilter.new SplittedOut(secondOutputAudioChannel, 0);
		sOut1.setAbsoluteIndex(1);

		assertNotNull(sOut1.getInStream());
		assertEquals(inputAudioStream, sOut1.getInStream());

		assertNotNull(sOut1.getOutputAudioChannel());
		assertEquals(secondOutputAudioChannel, sOut1.getOutputAudioChannel());

		assertNotNull(sOut1.getLinkableOutStreamReference());
		assertEquals(secondOutputAudioStream, sOut1.getLinkableOutStreamReference());

		assertNotNull(sOut1.toMapReferenceAsInput());
		assertEquals("split1", sOut1.toMapReferenceAsInput());
	}

	@Test
	void testToFilter() {
		final var filter0 = acmFilter.toFilter();
		assertEquals("[0:1]channelsplit=channel_layout=stereo:channels=FL[split0]", filter0.toString());

		final var sOut1 = acmFilter.new SplittedOut(secondOutputAudioChannel, 0);
		sOut1.setAbsoluteIndex(1);
		acmFilter.getSplittedOut().put(IN_CH1, sOut1);

		final var filter1 = acmFilter.toFilter();
		assertEquals("[0:1]channelsplit=channel_layout=stereo:channels=FL+FR[split0][split1]", filter1.toString());
	}

	@Test
	void testCantMixSources() {
		final var otherInputAudioStream = new InputAudioStream(STEREO, 1, 1);
		final var thirdOutputAudioChannel = secondOutputAudioStream.new OutputAudioChannel(
		        otherInputAudioStream, IN_CH1, secondOutputAudioStream, chOutIndex);
		assertThrows(IllegalArgumentException.class, () -> acmFilter.new SplittedOut(thirdOutputAudioChannel, 0));
	}

}
