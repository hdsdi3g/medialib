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
 * Copyright (C) hdsdi3g for hd3g.tv 2018
 *
 */
package tv.hd3g.fflauncher.enums;

public enum FFUnit {

	KILO {
		@Override
		public String toString() {
			return "k";
		}
	},
	MEGA {
		@Override
		public String toString() {
			return "M";
		}
	},
	GIGA {
		@Override
		public String toString() {
			return "G";
		}
	};

	public static FFUnit fromString(final String u) {
		switch (u.toLowerCase()) {
		case "k":
			return KILO;
		case "m":
			return MEGA;
		case "g":
			return GIGA;
		default:
			throw new IndexOutOfBoundsException("Unknow " + u);
		}
	}

	@Override
	public String toString() {
		return name().toLowerCase();
	}
}
