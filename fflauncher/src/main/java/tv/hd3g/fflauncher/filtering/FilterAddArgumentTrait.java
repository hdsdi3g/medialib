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

import java.util.Collection;
import java.util.List;

public interface FilterAddArgumentTrait {

	List<FilterArgument> getArguments();

	default void addArgument(final String key, final String value) {
		getArguments().add(new FilterArgument(key, value));
	}

	default void addArgument(final String key, final Number value) {
		getArguments().add(new FilterArgument(key, value));
	}

	default void addArgument(final String key, final Enum<?> value) {
		getArguments().add(new FilterArgument(key, value));
	}

	/**
	 * map with toString
	 */
	default void addArgument(final String key, final Collection<?> values, final String join) {
		if (values.isEmpty()) {
			return;
		}
		getArguments().add(new FilterArgument(key, values, join));
	}

	default void addArgument(final String key) {
		getArguments().add(new FilterArgument(key));
	}

}
