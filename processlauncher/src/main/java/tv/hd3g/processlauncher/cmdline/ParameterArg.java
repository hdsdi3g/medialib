/*
 * This file is part of processlauncher.
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
 * Copyright (C) hdsdi3g for hd3g.tv 2019
 *
 */
package tv.hd3g.processlauncher.cmdline;

class ParameterArg {

	private final boolean isInQuotes;
	private final StringBuilder content;

	ParameterArg(final boolean isInQuotes) {
		this.isInQuotes = isInQuotes;
		content = new StringBuilder();
	}

	ParameterArg add(final char arg) {
		content.append(arg);
		return this;
	}

	@Override
	public String toString() {
		return content.toString();
	}

	public boolean isInQuotes() {
		return isInQuotes;
	}

	public boolean isEmpty() {
		return content.length() == 0;
	}
}
