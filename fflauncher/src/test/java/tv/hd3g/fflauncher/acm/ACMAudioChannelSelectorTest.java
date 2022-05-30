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

import static java.util.stream.Collectors.toUnmodifiableList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

class ACMAudioChannelSelectorTest {

	static class AudioChannelSelector extends ACMAudioChannelSelector {
		AudioChannelSelector(final int posInStream) {
			super(posInStream);
		}
	}

	@Test
	void testHashCode() {
		assertEquals(new AudioChannelSelector(42).hashCode(), new AudioChannelSelector(42).hashCode());
	}

	@Test
	void testACMAudioChannelSelector_negativeValue() {
		assertThrows(IllegalArgumentException.class, () -> new AudioChannelSelector(-1));
	}

	@Test
	void testGetPosInStream() {
		assertEquals(42, new AudioChannelSelector(42).getPosInStream());
	}

	@Test
	void testCompareTo() {
		final var compared = List.of(
		        new AudioChannelSelector(3),
		        new AudioChannelSelector(1),
		        new AudioChannelSelector(2)).stream()
		        .sorted()
		        .map(AudioChannelSelector::getPosInStream)
		        .collect(toUnmodifiableList());
		assertEquals(List.of(1, 2, 3), compared);
	}

	@Test
	void testEqualsObject() {
		assertEquals(new AudioChannelSelector(42), new AudioChannelSelector(42));
	}
}
