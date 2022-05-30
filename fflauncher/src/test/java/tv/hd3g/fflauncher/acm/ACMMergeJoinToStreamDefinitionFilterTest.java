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
import static tv.hd3g.fflauncher.enums.ChannelLayout.STEREO;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ACMMergeJoinToStreamDefinitionFilterTest {

	static final OutputAudioStream outputAudioStream = new OutputAudioStream(STEREO, 0, 0);
	static final PseudoInput in0 = new PseudoInput("in0");
	static final PseudoInput in1 = new PseudoInput("in1");
	static final List<ACMExportableMapReference> inputs = List.of(in0, in1);

	static class PseudoInput implements ACMExportableMapReference {
		final String mapref;

		PseudoInput(final String mapref) {
			this.mapref = mapref;
		}

		@Override
		public String toMapReferenceAsInput() {
			return mapref;
		}
	}

	ACMMergeJoinToStreamDefinitionFilter acmFilter;

	@BeforeEach
	void init() {
		acmFilter = new ACMMergeJoinToStreamDefinitionFilter(inputs, outputAudioStream);
	}

	@Test
	void testToString() {
		assertNotNull(acmFilter.toString());
	}

	@Test
	void testToMapReferenceAsInput() {
		acmFilter.setAbsoluteIndex(42);
		assertEquals("mergjoin" + 42, acmFilter.toMapReferenceAsInput());
	}

	@Test
	void testGetLinkableOutStreamReference() {
		assertEquals(outputAudioStream, acmFilter.getLinkableOutStreamReference());
	}

	@Test
	void testGetInputs() {
		assertEquals(inputs, acmFilter.getInputs());
	}

	@Test
	void testToAmergeFilter() {
		final var filter0 = acmFilter.toAmergeFilter();
		assertEquals("[in0][in1]amerge=inputs=2[mergjoin0]", filter0.toString());

		acmFilter.setAbsoluteIndex(42);
		final var filter1 = acmFilter.toAmergeFilter();
		assertEquals("[in0][in1]amerge=inputs=2[mergjoin42]", filter1.toString());
	}

	@Test
	void testToJoinFilter() {
		final var filter0 = acmFilter.toJoinFilter();
		assertEquals("[in0][in1]join=inputs=2:channel_layout=stereo:map=0.0-FL|1.0-FR[mergjoin0]", filter0.toString());

		acmFilter.setAbsoluteIndex(42);
		final var filter1 = acmFilter.toJoinFilter();
		assertEquals("[in0][in1]join=inputs=2:channel_layout=stereo:map=0.0-FL|1.0-FR[mergjoin42]", filter1.toString());
	}

	@Test
	void testInvalid() {
		final var acmFilter0 = new ACMMergeJoinToStreamDefinitionFilter(List.of(), outputAudioStream);
		assertThrows(IllegalArgumentException.class, () -> acmFilter0.toAmergeFilter());
		assertThrows(IllegalArgumentException.class, () -> acmFilter0.toJoinFilter());

		final var acmFilter1 = new ACMMergeJoinToStreamDefinitionFilter(List.of(in0), outputAudioStream);
		assertThrows(IllegalArgumentException.class, () -> acmFilter1.toAmergeFilter());
		assertThrows(IllegalArgumentException.class, () -> acmFilter1.toJoinFilter());
	}
}
