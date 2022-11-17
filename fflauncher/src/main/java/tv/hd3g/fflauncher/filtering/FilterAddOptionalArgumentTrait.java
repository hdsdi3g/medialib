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
package tv.hd3g.fflauncher.filtering;

import static java.math.RoundingMode.CEILING;
import static java.util.Locale.ENGLISH;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.util.Optional;

public interface FilterAddOptionalArgumentTrait extends FilterAddArgumentTrait {

	default void addOptionalArgument(final String key, final String value) {
		if (key != null && value != null) {
			addArgument(key, value);
		}
	}

	default void addOptionalArgument(final String key, final Number value) {
		if (key != null && value != null) {
			addArgument(key, value);
		}
	}

	default void addOptionalArgument(final String key, final Enum<?> value) {
		if (key != null && value != null) {
			addArgument(key, value);
		}
	}

	default void addOptionalArgument(final String key) {
		if (key != null) {
			addArgument(key);
		}
	}

	default void addOptionalArgument(final String key, final boolean add) {
		if (key != null && add) {
			addArgument(key);
		}
	}

	default void addOptionalDurationSecArgument(final String key, final Duration duration) {
		if (key != null && duration != null) {
			addArgument(key, duration.toSeconds());
		}
	}

	default String roundWithPrecision(final double number) {
		final var dfMs = new DecimalFormat("#.###");
		dfMs.setRoundingMode(CEILING);
		dfMs.setDecimalFormatSymbols(new DecimalFormatSymbols(ENGLISH));
		return dfMs.format(number);
	}

	default void addOptionalDurationSecMsArgument(final String key, final Duration duration) {
		if (key != null && duration != null) {
			addArgument(key, roundWithPrecision(duration.toMillis() / 1000d));
		}
	}

	/**
	 * With String.valueOf(value.get())
	 */
	default <T> void addArgument(final String key, final Optional<T> value) {
		if (key != null && value.isPresent()) {
			addArgument(key, String.valueOf(value.get()));
		}
	}

	default void addOptionalArgument(final String key, final boolean add, final String value) {
		if (key != null && add && value != null) {
			addArgument(key, value);
		}
	}

	default void addOptionalNonNegativeArgument(final String key, final int value) {
		if (key != null && value > -1) {
			addArgument(key, value);
		}
	}

	default void addOptionalNonNegativeArgument(final String key, final long value) {
		if (key != null && value > -1) {
			addArgument(key, value);
		}
	}

	default void addOptionalNonNegativeArgument(final String key, final float value) {
		if (key != null && Math.signum(value) >= 0f) {
			addArgument(key, roundWithPrecision(value));
		}
	}

	default void addOptionalNonNegativeArgument(final String key, final double value) {
		if (key != null && Math.signum(value) >= 0f) {
			addArgument(key, roundWithPrecision(value));
		}
	}

}
