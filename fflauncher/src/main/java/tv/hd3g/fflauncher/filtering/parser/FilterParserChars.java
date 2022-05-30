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

import java.util.Objects;

final class FilterParserChars {

	private final char[] entries;
	private final boolean escaped;

	FilterParserChars(final char entry) {
		entries = new char[] { entry };
		escaped = false;
	}

	FilterParserChars(final char entry, final boolean escaped) {
		entries = new char[] { entry };
		this.escaped = escaped;
	}

	FilterParserChars(final CharSequence chars) {
		Objects.requireNonNull(chars, "\"chars\" can't to be null");
		if (chars.length() == 0) {
			throw new IndexOutOfBoundsException("Can't manage empty chars");
		}

		final var list = new char[chars.length()];
		for (var pos = 0; pos < list.length; pos++) {
			list[pos] = chars.charAt(pos);
		}
		entries = list;
		escaped = false;
	}

	void write(final StringBuilder sb) {
		if (escaped) {
			sb.append("\\");
		}
		sb.append(entries);
	}

	@Override
	public String toString() {
		if (escaped) {
			return "\\" + String.valueOf(entries);
		} else {
			return String.valueOf(entries);
		}
	}

	boolean isComma() {
		return escaped == false && entries[0] == ',';
	}

	boolean isColon() {
		return escaped == false && entries[0] == ':';
	}

	boolean isSemicolon() {
		return escaped == false && entries[0] == ';';
	}

	boolean isBracketOpen() {
		return escaped == false && entries[0] == '[';
	}

	boolean isBracketClose() {
		return escaped == false && entries[0] == ']';
	}

	boolean isEquals() {
		return escaped == false && entries[0] == '=';
	}

	boolean isFromEscaped() {
		return escaped;
	}
}
