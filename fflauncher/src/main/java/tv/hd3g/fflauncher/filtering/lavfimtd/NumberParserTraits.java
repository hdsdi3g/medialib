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
 * Copyright (C) hdsdi3g for hd3g.tv 2023
 *
 */
package tv.hd3g.fflauncher.filtering.lavfimtd;

import static java.lang.Float.NaN;
import static java.lang.Float.isFinite;
import static java.lang.Float.isNaN;
import static java.lang.Math.round;

import java.util.Optional;

public interface NumberParserTraits {

	default boolean stringNullOrBlank(final String value) {
		return value == null || value.isBlank();
	}

	default float parseFloat(final String value) {
		if (stringNullOrBlank(value)) {
			return Float.NaN;
		} else if (value.equalsIgnoreCase("-inf")) {
			return Float.NEGATIVE_INFINITY;
		} else if (value.equalsIgnoreCase("inf")) {
			return Float.POSITIVE_INFINITY;
		}
		try {
			return Float.valueOf(value);
		} catch (final Exception e) {
			return Float.NaN;
		}
	}

	default Double parseDouble(final String value) {
		if (stringNullOrBlank(value)) {
			return Double.NaN;
		} else if (value.equalsIgnoreCase("-inf")) {
			return Double.NEGATIVE_INFINITY;
		} else if (value.equalsIgnoreCase("inf")) {
			return Double.POSITIVE_INFINITY;
		}
		try {
			return Double.valueOf(value);
		} catch (final Exception e) {
			return Double.NaN;
		}
	}

	/**
	 * From a float string chain
	 */
	default Optional<Integer> parseInt(final String value) {
		if (stringNullOrBlank(value)) {
			return Optional.empty();
		} else if (value.contains(".")) {
			return Optional.ofNullable(round(parseFloat(value)));
		}
		try {
			return Optional.ofNullable(Integer.parseInt(value));
		} catch (final NumberFormatException e) {
			return Optional.empty();
		}
	}

	/**
	 * From a double string chain
	 */
	default Optional<Long> parseLong(final String value) {
		if (stringNullOrBlank(value)) {
			return Optional.empty();
		} else if (value.contains(".")) {
			return Optional.ofNullable(round(parseDouble(value)));
		}
		try {
			return Optional.ofNullable(Long.parseLong(value));
		} catch (final NumberFormatException e) {
			return Optional.empty();
		}
	}

	default int parseIntOrNeg1(final String value) {
		return parseInt(value).orElse(-1);
	}

	default long parseLongOrNeg1(final String value) {
		return parseLong(value).orElse(-1l);
	}

	default float parseFloatOrNeg1(final String value) {
		final var val = parseFloat(value);
		if (Float.isFinite(val) == false) {
			return -1f;
		}
		return val;
	}

	default float linearToDb(final float linear) {
		if (isFinite(linear) && isNaN(linear) == false) {
			return (float) (20d * Math.log10(linear));
		}
		return NaN;
	}

}
