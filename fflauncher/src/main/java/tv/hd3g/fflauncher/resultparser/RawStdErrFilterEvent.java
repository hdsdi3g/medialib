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
package tv.hd3g.fflauncher.resultparser;

import lombok.Getter;
import lombok.ToString;
import tv.hd3g.fflauncher.recipes.MediaAnalyser;

@Getter
@ToString
public class RawStdErrFilterEvent {

	private final String filterName;
	private final int filterChainPos;
	private final String lineValue;

	/**
	 * @param rawLine like [Parsed_ebur128_0 @ 0x55c6a78d7580] t: 0.707479 TARGET:-23 LUFS M: -24.4 S:-120.7 ....
	 */
	RawStdErrFilterEvent(final String rawLine) {
		final var items = MediaAnalyser.splitter(rawLine, ' ');

		/**
		 * [Parsed_ebur128_0
		 */
		final var head = items.get(0);
		final var headItems = MediaAnalyser.splitter(head, '_');
		filterName = headItems.get(1);
		filterChainPos = Integer.valueOf(headItems.get(2));

		if (items.get(1).equals("@") == false) {
			throw new IllegalArgumentException("Missing \"@\" on line \"" + rawLine + "\"");
		} else if (items.get(2).endsWith("]") == false) {
			throw new IllegalArgumentException("Missing \"]\" on line \"" + rawLine + "\"");
		}

		final var dataPos = rawLine.indexOf("]");
		lineValue = rawLine.substring(dataPos + 1).trim();
	}

}
