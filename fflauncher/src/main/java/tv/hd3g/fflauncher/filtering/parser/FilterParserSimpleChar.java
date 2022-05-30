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
package tv.hd3g.fflauncher.filtering.parser;

class FilterParserSimpleChar {
	private final char entry;

	FilterParserSimpleChar(final char entry) {
		this.entry = entry;
	}

	void write(final StringBuilder sb) {
		sb.append(entry);
	}

	boolean isSpace() {
		return entry == ' ' || entry == '\n';
	}

	boolean isEscape() {
		return entry == '\\';
	}

	boolean isQuoted() {
		return entry == '\'';
	}

	FilterParserChars toFilterChars() {
		return new FilterParserChars(entry);
	}

	FilterParserChars toEscapedFilterChars() {
		return new FilterParserChars(entry, true);
	}
}
