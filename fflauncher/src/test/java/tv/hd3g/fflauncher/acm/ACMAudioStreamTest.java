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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static tv.hd3g.fflauncher.enums.ChannelLayout.MONO;
import static tv.hd3g.fflauncher.enums.ChannelLayout.STEREO;

import org.junit.jupiter.api.Test;

import tv.hd3g.fflauncher.enums.ChannelLayout;

class ACMAudioStreamTest {

	static class AudioStream extends ACMAudioStream {
		AudioStream(final ChannelLayout layout, final int fileIndex, final int streamIndex) {
			super(layout, fileIndex, streamIndex);
		}

		@Override
		public String toMapReferenceAsInput() {
			return null;
		}
	}

	@Test
	void testHashCode() {
		assertEquals(new AudioStream(STEREO, 42, 1).hashCode(), new AudioStream(STEREO, 42, 1).hashCode());
	}

	@Test
	void testACMAudioStream() {
		assertThrows(IllegalArgumentException.class, () -> new AudioStream(STEREO, -42, 1));
		assertThrows(IllegalArgumentException.class, () -> new AudioStream(STEREO, 42, -1));
		assertThrows(IllegalArgumentException.class, () -> new AudioStream(STEREO, -42, -1));
	}

	@Test
	void testGetFileIndex() {
		assertEquals(42, new AudioStream(STEREO, 42, 1).getFileIndex());
	}

	@Test
	void testGetLayout() {
		assertEquals(STEREO, new AudioStream(STEREO, 42, 1).getLayout());
	}

	@Test
	void testGetStreamIndex() {
		assertEquals(1, new AudioStream(STEREO, 42, 1).getStreamIndex());
	}

	@Test
	void testEqualsObject() {
		assertEquals(new AudioStream(STEREO, 42, 1), new AudioStream(STEREO, 42, 1));
		assertNotEquals(new AudioStream(MONO, 42, 1), new AudioStream(STEREO, 42, 1));
		assertNotEquals(new AudioStream(STEREO, 0, 1), new AudioStream(STEREO, 42, 1));
	}
}
