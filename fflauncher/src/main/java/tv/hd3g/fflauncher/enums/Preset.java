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
 * Copyright (C) hdsdi3g for hd3g.tv 2022
 *
 */
package tv.hd3g.fflauncher.enums;

public enum Preset {
	ULTRAFAST,
	SUPERFAST,
	VERYFAST,
	FASTER,
	FAST,
	MEDIUM,
	SLOW,
	SLOWER,
	VERYSLOW,
	PLACEBO;

	@Override
	public String toString() {
		return name().toLowerCase();
	}
}
