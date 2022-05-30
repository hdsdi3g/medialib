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
import static tv.hd3g.fflauncher.enums.ChannelLayout.CH5_1;
import static tv.hd3g.fflauncher.enums.ChannelLayout.STEREO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ACMRemapDefinitionFilterTest {

	static final InputAudioStream inputAudioStream = new InputAudioStream(STEREO, 0, 1);
	OutputAudioStream outputAudioStream;
	ACMRemapDefinitionFilter acmFilter;

	@BeforeEach
	void init() {
		outputAudioStream = new OutputAudioStream(STEREO, 0, 0)
		        .mapChannel(inputAudioStream, IN_CH1)
		        .mapChannel(inputAudioStream, IN_CH0);
		acmFilter = new ACMRemapDefinitionFilter(inputAudioStream, outputAudioStream);
	}

	@Test
	void testGetLinkableOutStreamReference() {
		assertEquals(outputAudioStream, acmFilter.getLinkableOutStreamReference());
	}

	@Test
	void testToMapReferenceAsInput() {
		acmFilter.setAbsoluteIndex(42);
		assertEquals("remap" + 42, acmFilter.toMapReferenceAsInput());
	}

	@Test
	void testToFilter() {
		final var filter = acmFilter.toFilter();
		assertNotNull(filter);
		assertEquals("[0:1]channelmap=map=FR-FL|FL-FR:channel_layout=stereo[remap0]", filter.toString());
	}

	@Test
	void testNotSameSource() {
		final var otherInputAudioStream = new InputAudioStream(STEREO, 0, 2);
		outputAudioStream = new OutputAudioStream(STEREO, 0, 0)
		        .mapChannel(inputAudioStream, IN_CH1)
		        .mapChannel(otherInputAudioStream, IN_CH0);
		assertThrows(IllegalArgumentException.class,
		        () -> new ACMRemapDefinitionFilter(inputAudioStream, outputAudioStream));
	}

	@Test
	void testNotSameLayoutSize() {
		outputAudioStream = new OutputAudioStream(CH5_1, 0, 0)
		        .mapChannel(inputAudioStream, IN_CH1)
		        .mapChannel(inputAudioStream, IN_CH0);
		assertThrows(IllegalArgumentException.class,
		        () -> new ACMRemapDefinitionFilter(inputAudioStream, outputAudioStream));
	}

}
