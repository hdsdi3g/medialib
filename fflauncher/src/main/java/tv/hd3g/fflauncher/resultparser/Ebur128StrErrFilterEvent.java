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

import static java.lang.Float.NEGATIVE_INFINITY;
import static java.util.Objects.requireNonNull;

import java.util.HashMap;

import lombok.Getter;
import lombok.ToString;
import tv.hd3g.fflauncher.filtering.lavfimtd.NumberParserTraits;
import tv.hd3g.fflauncher.recipes.MediaAnalyser;

@Getter
@ToString
public class Ebur128StrErrFilterEvent implements NumberParserTraits {

	private final float t;
	private final float target;
	private final float m;
	private final float s;
	private final float i;
	private final float lra;
	private final Stereo<Float> spk;
	private final Stereo<Float> ftpk;
	private final Stereo<Float> tpk;

	/**
	 * @param lineValue "t: 0.707479 TARGET:-23 LUFS M: -24.4 S:-120.7 ...."
	 */
	public Ebur128StrErrFilterEvent(final String lineValue) {
		final var content = new HashMap<String, String>();

		final var dataItems = MediaAnalyser.splitter(lineValue, ':');
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

		t = extractValue(content.get("t"));
		target = extractValue(content.get("TARGET"));
		m = extractValue(content.get("M"));
		s = extractValue(content.get("S"));
		i = extractValue(content.get("I"));
		lra = extractValue(content.get("LRA"));
		spk = extractValues(content.get("SPK"));
		ftpk = extractValues(content.get("FTPK"));
		tpk = extractValues(content.get("TPK"));
	}

	private float extractValue(final String rawValue) {
		if (rawValue == null || rawValue.equalsIgnoreCase("nan")) {
			return NEGATIVE_INFINITY;
		}
		return parseFloat(MediaAnalyser.splitter(rawValue, ' ', 2).get(0));
	}

	private Stereo<Float> extractValues(final String rawValue) {
		if (rawValue == null) {
			return new Stereo<>(NEGATIVE_INFINITY, NEGATIVE_INFINITY);
		}
		final var items = MediaAnalyser.splitter(rawValue, ' ');

		float l;
		if (items.get(0).equalsIgnoreCase("nan")) {
			l = NEGATIVE_INFINITY;
		} else {
			l = parseFloat(items.get(0));
		}

		float r;
		if (items.get(1).equalsIgnoreCase("nan")) {
			r = NEGATIVE_INFINITY;
		} else {
			r = parseFloat(items.get(1));
		}

		return new Stereo<>(l, r);
	}

}
