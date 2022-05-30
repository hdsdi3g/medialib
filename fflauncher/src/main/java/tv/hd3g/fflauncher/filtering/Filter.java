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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import tv.hd3g.fflauncher.filtering.parser.FilterParser;
import tv.hd3g.fflauncher.filtering.parser.FilterParserDefinition;

/**
 * Full mutable, not thread safe.
 */
public class Filter implements FilterParserDefinition {

	private List<String> sourceBlocks;
	private List<String> destBlocks;
	private String filterName;
	private List<FilterArgument> arguments;

	public Filter(final String rawFilter) {
		final var items = FilterParser.fullParsing(rawFilter, () -> this);
		if (items.size() > 1 || items.get(0).size() > 1) {
			throw new IllegalArgumentException("This is filter chain, not a simple filter: " + rawFilter);
		}
	}

	Filter() {
	}

	/**
	 * @param arguments will be cloned in internal ArrayList (to keep the mutable contract)
	 */
	public Filter(final String filterName, final Collection<FilterArgument> arguments) {
		this.filterName = filterName;
		this.arguments = new ArrayList<>(arguments);
		sourceBlocks = new ArrayList<>();
		destBlocks = new ArrayList<>();
	}

	public Filter(final String filterName, final FilterArgument... arguments) {
		this.filterName = filterName;

		for (var pos = 0; pos < arguments.length; pos++) {
			Objects.requireNonNull(arguments[pos], "arguments can't contain null elements: #" + pos);
		}

		this.arguments = new ArrayList<>(List.of(arguments));
		sourceBlocks = new ArrayList<>();
		destBlocks = new ArrayList<>();
	}

	@Override
	public String toString() {
		final var ioBlocksJoiner = Collectors.joining("][");
		var sources = sourceBlocks.stream().collect(ioBlocksJoiner);
		if (sources.isEmpty() == false) {
			sources = "[" + sources + "]";
		}

		var dest = destBlocks.stream().collect(ioBlocksJoiner);
		if (dest.isEmpty() == false) {
			dest = "[" + dest + "]";
		}

		var argumentItems = arguments.stream()
		        .map(FilterArgument::toString)
		        .collect(Collectors.joining(":"));
		if (argumentItems.isEmpty() == false) {
			argumentItems = "=" + argumentItems;
		}

		return sources + filterName + argumentItems + dest;
	}

	public List<String> getDestBlocks() {
		return destBlocks;
	}

	public List<String> getSourceBlocks() {
		return sourceBlocks;
	}

	/**
	 * @return same as FFfilter.getTag()
	 */
	public String getFilterName() {
		return filterName;
	}

	public List<FilterArgument> getArguments() {
		return arguments;
	}

	@Override
	public int hashCode() {
		return Objects.hash(destBlocks, arguments, filterName, sourceBlocks);
	}

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
		final var other = (Filter) obj;
		return Objects.equals(destBlocks, other.destBlocks) && Objects.equals(arguments,
		        other.arguments) && Objects.equals(filterName, other.filterName) && Objects.equals(
		                sourceBlocks, other.sourceBlocks);
	}

	@Override
	public void setSourceBlocks(final List<String> sourceBlocks) {
		this.sourceBlocks = sourceBlocks;
	}

	@Override
	public void setDestBlocks(final List<String> destBlocks) {
		this.destBlocks = destBlocks;
	}

	@Override
	public void setFilterName(final String filterName) {
		this.filterName = filterName;
	}

	@Override
	public void setArguments(final List<FilterArgument> arguments) {
		this.arguments = arguments;
	}

	public void addArgument(final String key, final String value) {
		getArguments().add(new FilterArgument(key, value));
	}

	public void addArgument(final String key, final Number value) {
		getArguments().add(new FilterArgument(key, value));
	}

	public void addArgument(final String key, final Enum<?> value) {
		getArguments().add(new FilterArgument(key, value));
	}

	/**
	 * map with toString
	 */
	public void addArgument(final String key, final Collection<?> values, final String join) {
		getArguments().add(new FilterArgument(key, values, join));
	}

	/**
	 * map with toString
	 */
	public void addArgument(final String key, final Stream<?> values, final String join) {
		getArguments().add(new FilterArgument(key, values, join));
	}

	public void addArgument(final String key) {
		getArguments().add(new FilterArgument(key));
	}

}
