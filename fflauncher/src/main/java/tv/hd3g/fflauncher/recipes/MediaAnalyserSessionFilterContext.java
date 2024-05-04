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
package tv.hd3g.fflauncher.recipes;

import java.util.Collection;

import tv.hd3g.fflauncher.filtering.Filter;
import tv.hd3g.fflauncher.filtering.FilterChains;
import tv.hd3g.fflauncher.filtering.FilterSupplier;

public record MediaAnalyserSessionFilterContext(String type, String name, String setup, String className) {

	static MediaAnalyserSessionFilterContext getFromFilter(final FilterSupplier filterSupplier,
														   final String filterType) {
		final var filter = filterSupplier.toFilter();
		return new MediaAnalyserSessionFilterContext(
				filterType,
				filter.getFilterName(),
				filter.toString(),
				filterSupplier.getClass().getName());
	}

	static FilterChains getFilterChains(final Collection<MediaAnalyserSessionFilterContext> filters) {
		final var fChains = new FilterChains();
		final var chain = fChains.createChain();

		filters.stream()
				.map(MediaAnalyserSessionFilterContext::setup)
				.map(Filter::new)
				.forEach(chain::add);
		return fChains;
	}

}
