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

import java.util.Optional;

public interface FilterAddOptionalArgumentTrait extends FilterAddArgumentTrait {

	// TODO test

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

	default void addOptionalNonNegativeArgument(final String key, final byte value) {
		if (key != null && value > -1) {
			addArgument(key, value);
		}
	}

	default void addOptionalNonNegativeArgument(final String key, final short value) {
		if (key != null && value > -1) {
			addArgument(key, value);
		}
	}

	default void addOptionalNonNegativeArgument(final String key, final float value) {
		if (key != null && Math.signum(value) >= 0f) {
			addArgument(key, value);
		}
	}

	default void addOptionalNonNegativeArgument(final String key, final double value) {
		if (key != null && Math.signum(value) >= 0f) {
			addArgument(key, value);
		}
	}

}
