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

abstract class ACMAudioChannelSelector implements Comparable<ACMAudioChannelSelector> {
	private final int posInStream;

	ACMAudioChannelSelector(final int posInStream) {
		this.posInStream = posInStream;
		if (posInStream < 0) {
			throw new IllegalArgumentException("Invalid negative values: " + posInStream);
		}
	}

	int getPosInStream() {
		return posInStream;
	}

	@Override
	public int compareTo(final ACMAudioChannelSelector o) {
		return Integer.compare(posInStream, o.posInStream);
	}

	@Override
	public int hashCode() {
		return Objects.hash(posInStream);
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
		final var other = (ACMAudioChannelSelector) obj;
		return posInStream == other.posInStream;
	}

}
