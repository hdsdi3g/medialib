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
 * Copyright (C) hdsdi3g for hd3g.tv 2020
 *
 */
package tv.hd3g.fflauncher.filtering;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FilterArgument {

	private final String key;
	private String value;

	public FilterArgument(final String key, final String value) {
		this.key = key;
		this.value = value;
	}

	public FilterArgument(final String key, final Number value) {
		this.key = key;
		this.value = String.valueOf(value);
	}

	public FilterArgument(final String key, final Enum<?> value) {
		this.key = key;
		this.value = value.toString();
	}

	/**
	 * map with toString
	 */
	public FilterArgument(final String key, final Collection<?> values, final String join) {
		this(key, values.stream(), join);
	}

	/**
	 * map with toString
	 */
	public FilterArgument(final String key, final Stream<?> values, final String join) {
		this.key = key;
		value = values
		        .map(Object::toString)
		        .collect(Collectors.joining(join));
	}

	public FilterArgument(final String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}

	/**
	 * @return can be null
	 */
	public String getValue() {
		return value;
	}

	public void setValue(final String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		if (value != null) {
			return key + "=" + value;
		} else {
			return key;
		}
	}

	/**
	 * Only use key
	 */
	@Override
	public int hashCode() {
		return Objects.hash(key);
	}

	/**
	 * Only use key
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final var other = (FilterArgument) obj;
		return Objects.equals(key, other.key);
	}

}
