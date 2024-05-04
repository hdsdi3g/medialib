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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import tv.hd3g.fflauncher.ConversionTool;
import tv.hd3g.fflauncher.FFbase;
import tv.hd3g.fflauncher.about.FFAbout;
import tv.hd3g.fflauncher.about.FFAboutFilter;
import tv.hd3g.fflauncher.enums.FilterConnectorType;
import tv.hd3g.fflauncher.filtering.parser.FilterParser;
import tv.hd3g.processlauncher.cmdline.Parameters;

/**
 * Manage filter entries like "split [main][tmp]; [tmp] crop=iw:ih/2:0:0, vflip [flip]; [main][flip] overlay=0:H/2"
 */
public class FilterChains {

	private final List<List<Filter>> chain;

	public FilterChains() {
		chain = new ArrayList<>();
	}

	public FilterChains(final String filterChain) {
		chain = FilterParser.fullParsing(filterChain, Filter::new);
	}

	public void addFilterInLastChain(final Filter filter, final boolean createNewChain) {
		if (createNewChain || chain.isEmpty()) {
			createChain().add(filter);
		} else {
			chain.get(chain.size() - 1).add(filter);
		}
	}

	/**
	 * @return created filter
	 */
	public Filter addFilterInLastChain(final String rawFilter, final boolean createNewChain) {
		final var filter = new Filter(rawFilter);
		addFilterInLastChain(filter, createNewChain);
		return filter;
	}

	/**
	 * @return selected filter chain with previousFilter and new added filter
	 */
	public List<Filter> insertFilterInChain(final Filter filter, final Filter previousFilter) {
		final var currentChain = chain.stream()
				.filter(c -> c.contains(previousFilter))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Can't found filter \""
																+ previousFilter
																+ "\" declared is actual chains"));
		final var actualPos = currentChain.indexOf(previousFilter);
		if (actualPos + 1 == currentChain.size()) {
			currentChain.add(filter);
		} else {
			currentChain.add(actualPos + 1, filter);
		}
		return currentChain;
	}

	public List<Filter> getLastChain() {
		return chain.get(chain.size() - 1);
	}

	public List<Filter> insertFilterInChain(final String rawFilter, final Filter previousFilter) {
		return insertFilterInChain(new Filter(rawFilter), previousFilter);
	}

	public int getChainsCount() {
		return chain.size();
	}

	/**
	 * @param chainId 0 is first
	 * @return mutable
	 */
	public List<Filter> getChain(final int chainId) {
		return chain.get(chainId);
	}

	public Stream<Filter> getAllFiltersInChains() {
		return chain.stream().flatMap(List::stream);
	}

	/**
	 * @return mutable
	 */
	public List<Filter> createChain() {
		final var newChain = new ArrayList<Filter>();
		chain.add(newChain);
		return newChain;
	}

	/**
	 * @param chainId 0 is first
	 */
	public void removeChain(final int chainId) {
		chain.remove(chainId);
	}

	/**
	 * @param parameterName like -filter_complex, -vf, -af, ... Use toString.
	 */
	public void pushFilterChainTo(final String parameterName, final FFbase ffbase) {
		ffbase.getInternalParameters().addParameters(parameterName, toString());
	}

	/**
	 * @param varName declared on ffbase. Use toString.
	 */
	public void setFilterChainToVar(final String varName, final FFbase ffbase) {
		if (getChainsCount() > 0) {
			ffbase.getParametersVariables().put(varName, Parameters.of(toString()));
		}
	}

	/**
	 * @param parameterName like -vf, -af, ...
	 * @return an unmodifiableList
	 */
	public static List<FilterChains> parse(final String parameterName, final Parameters parameters) {
		return Optional.ofNullable(parameters.getValues(parameterName))
				.orElse(List.of())
				.stream()
				.map(FilterChains::new)
				.toList();
	}

	/**
	 * @param parameterName like -vf, -af, ...
	 */
	public static List<FilterChains> parse(final String parameterName, final ConversionTool conversionTool) {
		return parse(parameterName, conversionTool.getInternalParameters());
	}

	/**
	 * @param parameterName like -vf, -af, ...
	 */
	public static FilterChains merge(final List<FilterChains> chainsList) {
		final var item = new FilterChains();
		chainsList.stream().map(fc -> fc.chain).forEach(item.chain::addAll);
		return item;
	}

	/**
	 * @param parameterName like -vf, -af, ...
	 */
	public static List<FilterChains> parseFromReadyToRunParameters(final String parameterName,
																   final ConversionTool conversionTool) {
		return parse(parameterName, conversionTool.getReadyToRunParameters());
	}

	@Override
	public String toString() {
		return chain.stream()
				.map(filters -> filters.stream()
						.map(Filter::toString)
						.collect(Collectors.joining(",")))
				.collect(Collectors.joining(";"));
	}

	/**
	 * @return all non managed filters for this instance. Empty == all ok.
	 */
	public List<Filter> checkFiltersAvailability(final FFAbout about) {
		return checkFiltersAvailability(about, null);
	}

	/**
	 * @return all non managed filters for this instance. Empty == all ok.
	 */
	public List<Filter> checkFiltersAvailability(final FFAbout about,
												 final FilterConnectorType expectedType) {
		final var filters = about.getFilters();

		final var availableFilters = filters.stream()
				.filter(f -> {
					if (expectedType == null) {
						return true;
					}
					return expectedType.equals(f.getSourceConnector());
				})
				.map(FFAboutFilter::getTag)
				.distinct()
				.collect(Collectors.toUnmodifiableSet());

		return chain.stream()
				.flatMap(List::stream)
				.filter(filter -> availableFilters.contains(filter.getFilterName()) == false)
				.toList();
	}

}
