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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.mockito.internal.verification.VerificationModeFactory.atMostOnce;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import tv.hd3g.fflauncher.filtering.lavfimtd.LavfiMetadataFilterParser;

class MediaAnalyserResultTest {

	MediaAnalyserResult r;

	@Mock
	LavfiMetadataFilterParser lavfiMetadatas;
	@Mock
	Set<MediaAnalyserSessionFilterContext> filters;

	@BeforeEach
	void init() throws Exception {
		openMocks(this).close();
		r = new MediaAnalyserResult(lavfiMetadatas, filters, Optional.of(0));
	}

	@AfterEach
	void ends() {
		verify(lavfiMetadatas, atMostOnce()).getReportCount();
		verify(lavfiMetadatas, atMostOnce()).getEventCount();

		verifyNoMoreInteractions(filters, lavfiMetadatas);
	}

	@Test
	void testIsEmpty_realEmpty() {
		r = new MediaAnalyserResult(null, null, null);
		assertTrue(r.isEmpty());
	}

	@Test
	void testIsEmpty_HasReport() {
		when(lavfiMetadatas.getReportCount()).thenReturn(1);
		assertFalse(r.isEmpty());
	}

	@Test
	void testIsEmpty_HasEvent() {
		when(lavfiMetadatas.getEventCount()).thenReturn(1);
		assertFalse(r.isEmpty());
	}

}
