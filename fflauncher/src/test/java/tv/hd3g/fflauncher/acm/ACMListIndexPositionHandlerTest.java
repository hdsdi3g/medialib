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
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ACMListIndexPositionHandlerTest {

	@Test
	void testSetAbsoluteIndex() {
		final var liph = new ACMListIndexPositionHandler() {
			@Override
			public OutputAudioStream getLinkableOutStreamReference() {
				return null;
			}

			@Override
			public String toMapReferenceAsInput() {
				return null;
			}
		};
		liph.setAbsoluteIndex(42);
		assertEquals(42, liph.absolutePosIndex);
		assertThrows(IllegalArgumentException.class, () -> liph.setAbsoluteIndex(-1));
		assertEquals(42, liph.absolutePosIndex);
	}

	@Test
	void testToString() {
		final var text = String.valueOf(System.nanoTime());
		final var liph = new ACMListIndexPositionHandler() {
			@Override
			public OutputAudioStream getLinkableOutStreamReference() {
				return null;
			}

			@Override
			public String toMapReferenceAsInput() {
				return text;
			}
		};
		assertEquals(text, liph.toString());
	}
}
