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
package tv.hd3g.fflauncher.recipes;

import static net.datafaker.Faker.instance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static tv.hd3g.fflauncher.recipes.MediaAnalyser.splitter;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;

import net.datafaker.Faker;
import tv.hd3g.fflauncher.about.FFAbout;
import tv.hd3g.fflauncher.filtering.AudioFilterAMetadata;
import tv.hd3g.fflauncher.filtering.AudioFilterSupplier;
import tv.hd3g.fflauncher.filtering.Filter;
import tv.hd3g.fflauncher.filtering.VideoFilterMetadata;
import tv.hd3g.fflauncher.filtering.VideoFilterSupplier;
import tv.hd3g.fflauncher.progress.ProgressCallback;
import tv.hd3g.fflauncher.progress.ProgressListener;
import tv.hd3g.processlauncher.cmdline.ExecutableFinder;
import tv.hd3g.processlauncher.cmdline.Parameters;

class MediaAnalyserTest {
	static Faker faker = instance();

	MediaAnalyser ma;

	String execName;
	String filterName;
	String filterNameMtd;

	@Mock
	ExecutableFinder executableFinder;
	@Mock
	FFAbout about;
	@Mock
	File fSource;
	String sSource;
	@Mock
	VideoFilterSupplier vf;
	@Mock
	AudioFilterSupplier af;
	@Mock
	VideoFilterMetadata vfMt;
	@Mock
	AudioFilterAMetadata afMt;
	@Mock
	Filter filter;
	@Mock
	Filter filterMtd;
	@Mock
	ProgressListener progressListener;
	@Mock
	ProgressCallback progressCallback;

	@BeforeEach
	void init() throws Exception {
		openMocks(this).close();
		execName = faker.numerify("exec###");
		sSource = faker.numerify("source###");
		ma = new MediaAnalyser(execName, executableFinder, about);
		filterName = faker.numerify("filterName###");
		filterNameMtd = faker.numerify("filterMtd###");

		when(about.isFilterIsAvaliable(filterName)).thenReturn(true);
		when(about.isFilterIsAvaliable(filterNameMtd)).thenReturn(true);

		when(af.toFilter()).thenReturn(filter);
		when(vf.toFilter()).thenReturn(filter);
		when(filter.getFilterName()).thenReturn(filterName);

		when(vfMt.toFilter()).thenReturn(filterMtd);
		when(afMt.toFilter()).thenReturn(filterMtd);
		when(filterMtd.getFilterName()).thenReturn(filterNameMtd);
	}

	@AfterEach
	void ends() {
		verifyNoMoreInteractions(
				executableFinder,
				about,
				fSource,
				vf,
				af,
				vfMt,
				afMt,
				filter,
				filterMtd,
				progressListener,
				progressCallback);
	}

	@Test
	void testCreateSessionFile() {
		final var session = ma.createSession(fSource);
		assertNotNull(session);
		assertNull(session.getSource());
		assertEquals(fSource, session.getSourceFile());
	}

	@Test
	void testCreateSessionString() {
		final var session = ma.createSession(sSource);
		assertNotNull(session);
		assertEquals(sSource, session.getSource());
		assertNull(session.getSourceFile());
	}

	@Test
	void testGetExecutableFinder() {
		assertEquals(executableFinder, ma.getExecutableFinder());
	}

	@Test
	void testSetProgress() {
		assertThrows(IllegalArgumentException.class, () -> ma.setProgress(null, progressCallback));
		assertThrows(IllegalArgumentException.class, () -> ma.setProgress(progressListener, null));
		ma.setProgress(null, null);
		ma.setProgress(progressListener, progressCallback);
	}

	@Test
	void testCreateFFmpeg() {
		final var ffmpeg = ma.createFFmpeg();
		assertNotNull(ffmpeg);
		assertEquals(execName, ffmpeg.getExecutableName());
		assertEquals(new Parameters(), ffmpeg.getInternalParameters());
	}

	@Test
	void testCreateFFmpeg_WithProgress() {
		ma.setProgress(progressListener, progressCallback);
		final var ffmpeg = ma.createFFmpeg();
		assertNotNull(ffmpeg);
		assertEquals(execName, ffmpeg.getExecutableName());
		assertEquals(new Parameters(), ffmpeg.getInternalParameters());
	}

	@Test
	void testGetAudioFilters() {
		final var f = ma.getAudioFilters();
		assertNotNull(f);
		assertTrue(f.isEmpty());
	}

	@Test
	void testGetVideoFilters() {
		final var f = ma.getVideoFilters();
		assertNotNull(f);
		assertTrue(f.isEmpty());
	}

	@Test
	void testAddFilterVideoFilterSupplier() {
		assertTrue(ma.addFilter(vf));
		assertEquals(List.of(vf), ma.getVideoFilters());

		verify(about, times(1)).isFilterIsAvaliable(filterName);
		verify(vf, times(1)).toFilter();
		verify(filter, times(1)).getFilterName();
	}

	@Test
	void testAddFilterVideoFilterSupplier_notAvaliable() {
		when(about.isFilterIsAvaliable(filterName)).thenReturn(false);

		assertFalse(ma.addFilter(vf));
		assertTrue(ma.getVideoFilters().isEmpty());

		verify(about, times(1)).isFilterIsAvaliable(filterName);
		verify(vf, times(1)).toFilter();
		verify(filter, times(1)).getFilterName();
	}

	@Test
	void testAddFilterAudioFilterSupplier() {
		assertTrue(ma.addFilter(af));
		assertEquals(List.of(af), ma.getAudioFilters());

		verify(about, times(1)).isFilterIsAvaliable(filterName);
		verify(af, times(1)).toFilter();
		verify(filter, times(1)).getFilterName();
	}

	@Test
	void testAddFilterAudioFilterSupplier_notAvaliable() {
		when(about.isFilterIsAvaliable(filterName)).thenReturn(false);

		assertFalse(ma.addFilter(af));
		assertTrue(ma.getAudioFilters().isEmpty());

		verify(about, times(1)).isFilterIsAvaliable(filterName);
		verify(af, times(1)).toFilter();
		verify(filter, times(1)).getFilterName();
	}

	@Test
	void testAddFilterVideoFilterSupplier_metadata() {
		assertTrue(ma.addFilter(vfMt));
		assertEquals(List.of(vfMt), ma.getVideoFilters());

		verify(about, times(1)).isFilterIsAvaliable(filterNameMtd);
		verify(vfMt, times(1)).toFilter();
		verify(filterMtd, times(1)).getFilterName();
	}

	@Test
	void testAddFilterAudioFilterSupplier_metadata() {
		assertTrue(ma.addFilter(afMt));
		assertEquals(List.of(afMt), ma.getAudioFilters());

		verify(about, times(1)).isFilterIsAvaliable(filterNameMtd);
		verify(afMt, times(1)).toFilter();
		verify(filterMtd, times(1)).getFilterName();
	}

	@Test
	void testAssertAndParseStringString() {
		final var v0 = faker.numerify("aKey####");
		final var v1 = faker.numerify("aValue####");
		assertEquals(v1, MediaAnalyser.assertAndParse(v0 + v1, v0));
	}

	@Test
	void testAssertAndParseStringStringFunctionOfString_Integer() {
		final var v0 = faker.numerify("aKey####");
		final var v1 = faker.random().nextInt();
		assertEquals(v1, (int) MediaAnalyser.assertAndParse(v0 + v1, v0, Integer::valueOf));
	}

	@Test
	void testAssertAndParseStringStringFunctionOfString_Float() {
		final var v0 = faker.numerify("aKey####");
		final var v1 = faker.random().nextFloat();
		assertEquals(v1, (float) MediaAnalyser.assertAndParse(v0 + v1, v0, Float::valueOf));
	}

	@Test
	void testAssertAndParse_nope() {
		final var v0 = faker.numerify("aKey####");
		final var v1 = faker.numerify("aValue####");
		final var key = faker.numerify("aKey####") + v1;
		assertThrows(IllegalArgumentException.class,
				() -> MediaAnalyser.assertAndParse(key, v0));
	}

	@Nested
	class SplitterTest {
		String v0;
		String v1;
		String v2;
		String v3;

		@BeforeEach
		void init() {
			v0 = faker.numerify("left####");
			v1 = faker.numerify("middle####");
			v2 = faker.numerify("back####");
			v3 = faker.numerify("right####");
		}

		@Test
		void testStringChar() {
			final var result = splitter(v0 + "." + v1 + "." + v2 + "." + v3, '.');
			assertNotNull(result);
			assertEquals(4, result.size());
			assertEquals(v0, result.get(0));
			assertEquals(v1, result.get(1));
			assertEquals(v2, result.get(2));
			assertEquals(v3, result.get(3));
		}

		@Test
		void testStringCharInt_0width() {
			final var result = splitter(v0 + "." + v1 + "." + v2 + "." + v3, '.', 0);
			assertNotNull(result);
			assertEquals(1, result.size());
			assertEquals(v0, result.get(0));
		}

		@Test
		void testStringCharInt_1width() {
			final var result = splitter(v0 + "." + v1 + "." + v2 + "." + v3, '.', 1);
			assertNotNull(result);
			assertEquals(1, result.size());
			assertEquals(v0, result.get(0));
		}

		@Test
		void testStringCharInt_2width() {
			final var result = splitter(v0 + "." + v1 + "." + v2 + "." + v3, '.', 2);
			assertNotNull(result);
			assertEquals(2, result.size());
			assertEquals(v0, result.get(0));
			assertEquals(v1, result.get(1));
		}

		@Test
		void testStringCharInt_3width() {
			final var result = splitter(v0 + "." + v1 + "." + v2 + "." + v3, '.', 3);
			assertNotNull(result);
			assertEquals(3, result.size());
			assertEquals(v0, result.get(0));
			assertEquals(v1, result.get(1));
			assertEquals(v2, result.get(2));
		}

		@ParameterizedTest
		@ValueSource(strings = { "?", "!", "=", ",", "+", "-" })
		void testStringChar_separators(final String input) {
			final var separator = input.charAt(0);

			final var result = splitter(v0 + separator + v1 + separator + v2 + separator + v3, separator);
			assertNotNull(result);
			assertEquals(4, result.size());
			assertEquals(v0, result.get(0));
			assertEquals(v1, result.get(1));
			assertEquals(v2, result.get(2));
			assertEquals(v3, result.get(3));
		}

	}
}
