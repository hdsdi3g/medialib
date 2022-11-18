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

import static java.util.Objects.requireNonNull;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;
import lombok.ToString;
import tv.hd3g.fflauncher.recipes.MediaAnalyser;

@Getter
@ToString
public class RawStdErrFilterEvent {

	private final String filterName;
	private final int filterChainPos;
	/**
	 * t => "2.20748"
	 * TARGET => "-23 LUFS"
	 * M => "-120.7"
	 * FTPK => "-35.2 -35.3 dBFS"
	 */
	private final Map<String, String> content;

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

		content = new LinkedHashMap<>();
		final var dataPos = rawLine.indexOf("]");
		final var dataItems = MediaAnalyser.splitter(rawLine.substring(dataPos + 1).trim(), ':');

		/**
		 * t
		 * 1.80748 TARGET
		 * -23 LUFS M
		 * -25.5 S
		 * -120.7 I
		 * -19.2 LUFS LRA
		 * 0.0 LU SPK
		 * -5.5 -5.6 dBFS FTPK
		 * -19.1 -21.6 dBFS TPK
		 * -5.5 -5.6 dBFS
		 */
		String key = null;
		String value = null;
		String entry;
		int lastSpacePos;
		for (var pos = 0; pos < dataItems.size(); pos++) {
			entry = dataItems.get(pos);
			if (pos == 0) {
				/** First */
				key = entry;
			} else if (pos + 1 == dataItems.size()) {
				/** Last */
				content.put(requireNonNull(key), entry.trim());
			} else {
				lastSpacePos = entry.lastIndexOf(" ");
				value = entry.substring(0, lastSpacePos);
				content.put(requireNonNull(key), value.trim());
				key = entry.substring(lastSpacePos + 1);
			}

		}
	}

}
