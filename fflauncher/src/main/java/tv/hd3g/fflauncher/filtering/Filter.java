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

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import tv.hd3g.fflauncher.filtering.parser.FilterParser;
import tv.hd3g.fflauncher.filtering.parser.FilterParserDefinition;

/**
 * Full mutable, not thread safe.
 */
@EqualsAndHashCode
@Getter
public class Filter implements FilterParserDefinition, FilterAddArgumentTrait, FilterAddOptionalArgumentTrait {

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

	@Override
	public void setSourceBlocks(final List<String> sourceBlocks) {
		this.sourceBlocks = requireNonNull(sourceBlocks);
	}

	@Override
	public void setDestBlocks(final List<String> destBlocks) {
		this.destBlocks = requireNonNull(destBlocks);
	}

	@Override
	public void setFilterName(final String filterName) {
		this.filterName = requireNonNull(filterName);
	}

	@Override
	public void setArguments(final List<FilterArgument> arguments) {
		this.arguments = requireNonNull(arguments);
	}

}
