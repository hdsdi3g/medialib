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
package tv.hd3g.fflauncher.recipes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static tv.hd3g.fflauncher.recipes.MediaAnalyserSessionFilterContext.getFromFilter;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import net.datafaker.Faker;
import tv.hd3g.fflauncher.filtering.Filter;
import tv.hd3g.fflauncher.filtering.FilterSupplier;

class MediaAnalyserSessionFilterContextTest {

	static Faker faker = net.datafaker.Faker.instance();

	@Mock
	FilterSupplier filterSupplier;
	@Mock
	Filter filter;

	String filterType;
	String filterName;
	String filterSetup;

	@BeforeEach
	void init() throws Exception {
		openMocks(this).close();
		filterType = faker.numerify("filterType###");
		filterName = faker.numerify("filterName###");
		filterSetup = faker.numerify("filterSetup###");
	}

	@AfterEach
	void end() {
		verifyNoMoreInteractions(filterSupplier, filter);
	}

	@Test
	void testGetFromFilter() {
		when(filterSupplier.toFilter()).thenReturn(filter);
		when(filter.getFilterName()).thenReturn(filterName);
		when(filter.toString()).thenReturn(filterSetup);

		final var result = getFromFilter(filterSupplier, filterType);
		assertNotNull(result);

		assertEquals(filterType, result.type());
		assertEquals(filterName, result.name());
		assertEquals(filterSetup, result.setup());

		verify(filterSupplier, times(1)).toFilter();
		verify(filter, times(1)).getFilterName();
	}

	@Test
	void testGetFilterChains() {
		final var masfc = new MediaAnalyserSessionFilterContext(
				filterType, filterName, filterSetup, this.getClass().getName());

		final var result = MediaAnalyserSessionFilterContext.getFilterChains(List.of(masfc));

		assertEquals(1, result.getChainsCount());
		final var c = result.getChain(0);
		assertEquals(1, c.size());
		final var f = c.get(0);
		assertEquals(filterSetup, f.toString());
		assertEquals(filterSetup, f.getFilterName());
	}

}
