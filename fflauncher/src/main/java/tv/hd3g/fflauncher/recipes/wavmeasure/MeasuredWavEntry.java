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
 * Copyright (C) hdsdi3g for hd3g.tv 2024
 *
 */

package tv.hd3g.fflauncher.recipes.wavmeasure;

import static java.util.Locale.US;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Ratio level by default (1 = 100%, 0.01 = 1%, 0 = -inf)
 */
public record MeasuredWavEntry(float position,
							   double peakPositive,
							   double peakNegative,
							   double rmsPositive,
							   double rmsNegative) {

	static final double HALF_16_BITS = 0x7FFF;
	static final double FLOOR_LEVEL_16_BITS = 20d * Math.log10(1 / HALF_16_BITS);

	private static double ratioToDb(final double value) {
		final var result = 20d * Math.log10(value);
		if (Double.isNaN(result)
			|| Double.isInfinite(result)
			|| result < FLOOR_LEVEL_16_BITS) {
			return FLOOR_LEVEL_16_BITS;
		}
		return result;
	}

	@JsonIgnore
	public double getPeakPositiveDb() {
		return ratioToDb(peakPositive);
	}

	@JsonIgnore
	public double getPeakNegativeDb() {
		return ratioToDb(peakNegative);
	}

	@JsonIgnore
	public double getRmsPositiveDb() {
		return ratioToDb(rmsPositive);
	}

	@JsonIgnore
	public double getRmsNegativeDb() {
		return ratioToDb(rmsNegative);
	}

	private static String format(final Number n) {
		return String.format(US, "%.2f", n);
	}

	@Override
	public final String toString() {
		final var sb = new StringBuilder();
		sb.append(format(position));
		sb.append("s peak=");
		sb.append(format(ratioToDb(peakPositive)));
		sb.append("/");
		sb.append(format(ratioToDb(peakNegative)));
		sb.append(" rms=");
		sb.append(format(ratioToDb(rmsPositive)));
		sb.append("/");
		sb.append(format(ratioToDb(rmsNegative)));
		return sb.toString();
	}
}