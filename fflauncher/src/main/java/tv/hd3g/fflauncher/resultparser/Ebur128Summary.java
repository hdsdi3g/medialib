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
import static tv.hd3g.fflauncher.recipes.MediaAnalyser.splitter;

import java.util.List;
import java.util.Optional;

import lombok.Getter;
import lombok.ToString;
import tv.hd3g.fflauncher.filtering.lavfimtd.NumberParserTraits;

@Getter
@ToString
public class Ebur128Summary implements NumberParserTraits {

	private float integrated;
	private float integratedThreshold;
	private float loudnessRange;
	private float loudnessRangeThreshold;
	private float loudnessRangeLow;
	private float loudnessRangeHigh;
	private float samplePeak;
	private float truePeak;

	Ebur128Summary() {
		integrated = NEGATIVE_INFINITY;
		integratedThreshold = NEGATIVE_INFINITY;
		loudnessRange = NEGATIVE_INFINITY;
		loudnessRangeThreshold = NEGATIVE_INFINITY;
		loudnessRangeLow = NEGATIVE_INFINITY;
		loudnessRangeHigh = NEGATIVE_INFINITY;
		samplePeak = NEGATIVE_INFINITY;
		truePeak = NEGATIVE_INFINITY;
	}

	private static final boolean isNegativeInfinity(final float value) {
		return Float.compare(value, NEGATIVE_INFINITY) == 0;
	}

	public boolean isEmpty() {
		return isNegativeInfinity(integrated)
			   && isNegativeInfinity(integratedThreshold)
			   && isNegativeInfinity(loudnessRange)
			   && isNegativeInfinity(loudnessRangeThreshold)
			   && isNegativeInfinity(loudnessRangeLow)
			   && isNegativeInfinity(loudnessRangeHigh)
			   && isNegativeInfinity(samplePeak)
			   && isNegativeInfinity(truePeak);
	}

	/**
	 * @param rawSummaryZone Like [["Threshold", " 0.0 LUFS"], ["Peak", " -inf dBFS"], ["Sample peak"]]...
	 */
	void setRawLines(final List<List<String>> rawSummaryZone) {
		rawSummaryZone.forEach(items -> {
			final var keyName = items.get(0).toUpperCase();
			final var oValue = extractValue(items);
			if (oValue.isPresent() == false) {
				return;
			}
			final var value = oValue.get();

			switch (keyName) {
			case "I" -> {
				integrated = value;
				/**
				 * If I => reset values
				 */
				integratedThreshold = NEGATIVE_INFINITY;
				loudnessRangeThreshold = NEGATIVE_INFINITY;
				samplePeak = NEGATIVE_INFINITY;
				truePeak = NEGATIVE_INFINITY;
			}
			case "LRA" -> loudnessRange = value;
			case "LRA LOW" -> loudnessRangeLow = value;
			case "LRA HIGH" -> loudnessRangeHigh = value;
			case "THRESHOLD" -> {
				if (integratedThreshold > NEGATIVE_INFINITY) {
					loudnessRangeThreshold = value;
				} else {
					integratedThreshold = value;
				}
			}
			case "PEAK" -> {
				if (samplePeak > NEGATIVE_INFINITY) {
					truePeak = value;
				} else {
					samplePeak = value;
				}
			}
			default -> throw new IllegalArgumentException("Unknown " + items.get(0) + ": " + items.get(1));
			}
		});
	}

	/**
	 * @param items like ["Threshold", " 0.0 LUFS"] or ["Peak", " -inf dBFS"].
	 */
	Optional<Float> extractValue(final List<String> items) {
		if (items.size() != 2) {
			return Optional.empty();
		}
		final var rawValue = items.get(1);
		final var numberValue = splitter(rawValue, ' ', 1).get(0);
		if (numberValue.equalsIgnoreCase("-inf")) {
			return Optional.empty();
		}
		return Optional.ofNullable(parseFloat(numberValue));
	}

}
