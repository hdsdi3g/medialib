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

import java.util.Map;

import lombok.Getter;
import lombok.ToString;
import tv.hd3g.fflauncher.recipes.MediaAnalyser;

@Getter
@ToString
public class Ebur128StrErrFilterEvent {

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
	 * t => 2.10748
	 * TARGET => -23 LUFS
	 * M => -18.5
	 * S => -120.7
	 * I => -19.5 LUFS
	 * LRA => 0.0 LU
	 * SPK => -5.5 -5.6 dBFS
	 * FTPK => -5.5 -5.6 dBFS
	 * TPK => -5.5 -5.6 dBFS
	 */
	public Ebur128StrErrFilterEvent(final Map<String, String> content) {
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
		if (rawValue == null) {
			return NEGATIVE_INFINITY;
		}
		return Float.valueOf(MediaAnalyser.splitter(rawValue, ' ', 2).get(0));
	}

	private Stereo<Float> extractValues(final String rawValue) {
		if (rawValue == null) {
			return new Stereo<>(NEGATIVE_INFINITY, NEGATIVE_INFINITY);
		}
		final var items = MediaAnalyser.splitter(rawValue, ' ');
		return new Stereo<>(Float.valueOf(items.get(0)), Float.valueOf(items.get(1)));
	}

}
