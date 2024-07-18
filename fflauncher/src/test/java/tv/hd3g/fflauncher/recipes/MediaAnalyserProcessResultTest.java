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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;

import tv.hd3g.commons.testtools.Fake;
import tv.hd3g.commons.testtools.MockToolsExtendsJunit;
import tv.hd3g.fflauncher.filtering.lavfimtd.LavfiMetadataFilterParser;

@ExtendWith(MockToolsExtendsJunit.class)
class MediaAnalyserProcessResultTest {

	MediaAnalyserProcessResult mapr;

	@Mock
	LavfiMetadataFilterParser lavfiMetadatas;
	@Mock
	Collection<MediaAnalyserSessionFilterContext> filters;
	@Mock
	Optional<Integer> r128Target;
	@Mock
	MediaAnalyserSessionFilterContext context;
	@Fake
	String ffmpegCommandLine;

	@BeforeEach
	void init() {
		mapr = new MediaAnalyserProcessResult(lavfiMetadatas, filters, r128Target, ffmpegCommandLine);
	}

	@AfterEach
	void end() {
		verify(lavfiMetadatas, atLeast(0)).getReportCount();
		verify(lavfiMetadatas, atLeast(0)).getEventCount();
	}

	@ParameterizedTest
	@ValueSource(ints = { 1, 2, 3 })
	void testIsEmpty(final int value) {
		if (value % 2 == 1) {
			when(lavfiMetadatas.getReportCount()).thenReturn(1);
		}
		if (value >> 1 == 1) {
			when(lavfiMetadatas.getEventCount()).thenReturn(1);
		}
		assertFalse(mapr.isEmpty());
	}

	@Test
	void testIsEmpty_noResult() {
		assertTrue(mapr.isEmpty());
	}

	@Test
	void testIsEmpty_noLavfiMetadatas() {
		mapr = new MediaAnalyserProcessResult(null, filters, r128Target, ffmpegCommandLine);
		assertTrue(mapr.isEmpty());
	}

	@Test
	void testImportFromOffline() {
		mapr = MediaAnalyserProcessResult.importFromOffline(
				Stream.of("frame:0 pts:0 pts_time:0.0"),
				List.of(),
				ffmpegCommandLine);
		assertTrue(mapr.isEmpty());
	}

}
