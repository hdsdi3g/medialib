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
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ACMMapDirectlyTest {

	InputAudioStream input;
	OutputAudioStream output;
	ACMMapDirectly mapD;

	@BeforeEach
	void init() {
		input = Mockito.mock(InputAudioStream.class);
		output = Mockito.mock(OutputAudioStream.class);
		mapD = new ACMMapDirectly(input, output);
	}

	@Test
	void testGetLinkableOutStreamReference() {
		assertEquals(output, mapD.getLinkableOutStreamReference());
	}

	@Test
	void testToMapReferenceAsInput() {
		final var text = String.valueOf(System.nanoTime());
		Mockito.when(input.toMapReferenceAsInput()).thenReturn(text);
		assertEquals(text, mapD.toMapReferenceAsInput());
		verify(input, Mockito.times(1)).toMapReferenceAsInput();
	}
}
