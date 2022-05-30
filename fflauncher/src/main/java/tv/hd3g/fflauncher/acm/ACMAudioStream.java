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

import java.util.Objects;

import tv.hd3g.fflauncher.enums.ChannelLayout;

abstract class ACMAudioStream implements ACMExportableMapReference {
	private final ChannelLayout layout;
	private final int fileIndex;
	private final int streamIndex;

	ACMAudioStream(final ChannelLayout layout, final int fileIndex, final int streamIndex) {
		this.layout = Objects.requireNonNull(layout);
		this.fileIndex = fileIndex;
		if (fileIndex < 0) {
			throw new IllegalArgumentException("Invalid negative values: " + fileIndex);
		}
		this.streamIndex = streamIndex;
		if (streamIndex < 0) {
			throw new IllegalArgumentException("Invalid negative values: " + streamIndex);
		}
	}

	int getFileIndex() {
		return fileIndex;
	}

	ChannelLayout getLayout() {
		return layout;
	}

	int getStreamIndex() {
		return streamIndex;
	}

	@Override
	public int hashCode() {
		return Objects.hash(fileIndex, layout, streamIndex);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final var other = (ACMAudioStream) obj;
		return fileIndex == other.fileIndex && layout == other.layout && streamIndex == other.streamIndex;
	}

}
