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

abstract class ACMListIndexPositionHandler implements ACMExportableMapReference, ACMLinkableOutStreamReference {
	protected int absolutePosIndex;

	void setAbsoluteIndex(final int pos) {
		if (pos < 0) {
			throw new IllegalArgumentException("Invalid negative values: " + pos);
		}
		absolutePosIndex = pos;
	}

	@Override
	public String toString() {
		return toMapReferenceAsInput();
	}

}