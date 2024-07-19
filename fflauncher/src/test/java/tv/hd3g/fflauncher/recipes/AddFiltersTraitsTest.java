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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import net.datafaker.Faker;
import tv.hd3g.fflauncher.filtering.AudioFilterSupplier;
import tv.hd3g.fflauncher.filtering.FilterSupplier;
import tv.hd3g.fflauncher.filtering.VideoFilterCropdetect;
import tv.hd3g.fflauncher.filtering.VideoFilterSupplier;

class AddFiltersTraitsTest {
	static Faker faker = net.datafaker.Faker.instance();

	AddFiltersTraitsImpl f;

	@Mock
	Function<VideoFilterSupplier, Boolean> vfFunction;
	@Mock
	Function<AudioFilterSupplier, Boolean> afFunction;
	@Captor
	ArgumentCaptor<VideoFilterSupplier> videoFilterSupplierCaptor;
	@Captor
	ArgumentCaptor<AudioFilterSupplier> audioFilterSupplierCaptor;
	@Mock
	VideoFilterSupplier videoFilterSupplier;
	@Mock
	AudioFilterSupplier audioFilterSupplier;

	AtomicReference<FilterSupplier> fSupplier;

	class AddFiltersTraitsImpl implements AddFiltersTraits {

		@Override
		public boolean addFilter(final VideoFilterSupplier vf) {
			return vfFunction.apply(vf);
		}

		@Override
		public boolean addFilter(final AudioFilterSupplier af) {
			return afFunction.apply(af);
		}

	}

	@BeforeEach
	void init() throws Exception {
		openMocks(this).close();
		f = new AddFiltersTraitsImpl();
		fSupplier = new AtomicReference<>();
	}

	@AfterEach
	void end() {
		verifyNoMoreInteractions(vfFunction, afFunction, videoFilterSupplier, audioFilterSupplier);
	}

	private void checkVideoFilter() {
		verify(vfFunction, times(1)).apply(videoFilterSupplierCaptor.capture());
		final var captured = videoFilterSupplierCaptor.getValue();
		assertNotNull(captured);
		assertNotNull(fSupplier.get());
		assertEquals(fSupplier.get(), captured);
	}

	private void checkAudioFilter() {
		verify(afFunction, times(1)).apply(audioFilterSupplierCaptor.capture());
		final var captured = audioFilterSupplierCaptor.getValue();
		assertNotNull(captured);
		assertNotNull(fSupplier.get());
		assertEquals(fSupplier.get(), captured);
	}

	private void checkNoVideoFilter() {
		verify(vfFunction, times(1)).apply(videoFilterSupplierCaptor.capture());
		assertNotNull(videoFilterSupplierCaptor.getValue());
		assertNull(fSupplier.get());
	}

	private void checkNoAudioFilter() {
		verify(afFunction, times(1)).apply(audioFilterSupplierCaptor.capture());
		assertNotNull(audioFilterSupplierCaptor.getValue());
		assertNull(fSupplier.get());
	}

	@Nested
	class FilterAvailable {

		@BeforeEach
		void init() {
			when(vfFunction.apply(any())).thenReturn(true);
			when(afFunction.apply(any())).thenReturn(true);
		}

		@Test
		void testAddFilterPhasemeter() {
			assertTrue(f.addFilterPhasemeter(filter -> fSupplier.set(filter)));
			checkAudioFilter();
		}

		@Test
		void testAddFilterAstats() {
			assertTrue(f.addFilterAstats(filter -> fSupplier.set(filter)));
			checkAudioFilter();
		}

		@Test
		void testAddFilterSilencedetect() {
			assertTrue(f.addFilterSilencedetect(filter -> fSupplier.set(filter)));
			checkAudioFilter();
		}

		@Test
		void testAddFilterVolumedetect() {
			assertTrue(f.addFilterVolumedetect(filter -> fSupplier.set(filter)));
			checkAudioFilter();
		}

		@Test
		void testAddFilterEbur128() {
			assertTrue(f.addFilterEbur128(filter -> fSupplier.set(filter)));
			checkAudioFilter();
		}

		@Test
		void testAddFilterAMetadata() {
			assertTrue(f.addFilterAMetadata(filter -> fSupplier.set(filter)));
			checkAudioFilter();
		}

		@Test
		void testAddFilterMetadata() {
			assertTrue(f.addFilterMetadata(filter -> fSupplier.set(filter)));
			checkVideoFilter();
		}

		@Test
		void testAddFilterSiti() {
			assertTrue(f.addFilterSiti(filter -> fSupplier.set(filter)));
			checkVideoFilter();
		}

		@Test
		void testAddFilterIdet() {
			assertTrue(f.addFilterIdet(filter -> fSupplier.set(filter)));
			checkVideoFilter();
		}

		@Test
		void testAddFilterFreezedetect() {
			assertTrue(f.addFilterFreezedetect(filter -> fSupplier.set(filter)));
			checkVideoFilter();
		}

		@Test
		void testAddFilterBlackdetect() {
			assertTrue(f.addFilterBlackdetect(filter -> fSupplier.set(filter)));
			checkVideoFilter();
		}

		@Test
		void testAddFilterCropdetect() {
			assertTrue(f.addFilterCropdetect(filter -> fSupplier.set(filter)));
			checkVideoFilter();
			final var crop = (VideoFilterCropdetect) videoFilterSupplierCaptor.getValue();
			assertFalse(crop.isModeMvedges());
		}

		@Test
		void testAddFilterBlockdetect() {
			assertTrue(f.addFilterBlockdetect(filter -> fSupplier.set(filter)));
			checkVideoFilter();
		}

		@Test
		void testAddFilterBlurdetect() {
			assertTrue(f.addFilterBlurdetect(filter -> fSupplier.set(filter)));
			checkVideoFilter();
		}

		@Test
		void testAddFilterMEstimate() {
			assertTrue(f.addFilterMEstimate(filter -> fSupplier.set(filter)));
			checkVideoFilter();
		}

		@Test
		void testAddOptionalFilter_audio() {
			assertTrue(f.addOptionalFilter(audioFilterSupplier, filter -> fSupplier.set(filter)));
			assertEquals(fSupplier.get(), audioFilterSupplier);
			verify(afFunction, times(1)).apply(audioFilterSupplier);
		}

		@Test
		void testAddOptionalFilter_video() {
			assertTrue(f.addOptionalFilter(videoFilterSupplier, filter -> fSupplier.set(filter)));
			assertEquals(fSupplier.get(), videoFilterSupplier);
			verify(vfFunction, times(1)).apply(videoFilterSupplier);
		}

	}

	@Nested
	class FilterNotAvailable {

		@BeforeEach
		void init() {
			when(vfFunction.apply(any())).thenReturn(false);
			when(afFunction.apply(any())).thenReturn(false);
		}

		@Test
		void testAddFilterPhasemeter() {
			assertFalse(f.addFilterPhasemeter(filter -> fSupplier.set(filter)));
			checkNoAudioFilter();
		}

		@Test
		void testAddFilterAstats() {
			assertFalse(f.addFilterAstats(filter -> fSupplier.set(filter)));
			checkNoAudioFilter();
		}

		@Test
		void testAddFilterSilencedetect() {
			assertFalse(f.addFilterSilencedetect(filter -> fSupplier.set(filter)));
			checkNoAudioFilter();
		}

		@Test
		void testAddFilterVolumedetect() {
			assertFalse(f.addFilterVolumedetect(filter -> fSupplier.set(filter)));
			checkNoAudioFilter();
		}

		@Test
		void testAddFilterEbur128() {
			assertFalse(f.addFilterEbur128(filter -> fSupplier.set(filter)));
			checkNoAudioFilter();
		}

		@Test
		void testAddFilterAMetadata() {
			assertFalse(f.addFilterAMetadata(filter -> fSupplier.set(filter)));
			checkNoAudioFilter();
		}

		@Test
		void testAddFilterMetadata() {
			assertFalse(f.addFilterMetadata(filter -> fSupplier.set(filter)));
			checkNoVideoFilter();
		}

		@Test
		void testAddFilterSiti() {
			assertFalse(f.addFilterSiti(filter -> fSupplier.set(filter)));
			checkNoVideoFilter();
		}

		@Test
		void testAddFilterIdet() {
			assertFalse(f.addFilterIdet(filter -> fSupplier.set(filter)));
			checkNoVideoFilter();
		}

		@Test
		void testAddFilterFreezedetect() {
			assertFalse(f.addFilterFreezedetect(filter -> fSupplier.set(filter)));
			checkNoVideoFilter();
		}

		@Test
		void testAddFilterBlackdetect() {
			assertFalse(f.addFilterBlackdetect(filter -> fSupplier.set(filter)));
			checkNoVideoFilter();
		}

		@Test
		void testAddFilterCropdetect() {
			assertFalse(f.addFilterCropdetect(filter -> fSupplier.set(filter)));
			checkNoVideoFilter();
			final var crop = (VideoFilterCropdetect) videoFilterSupplierCaptor.getValue();
			assertFalse(crop.isModeMvedges());
		}

		@Test
		void testAddFilterBlockdetect() {
			assertFalse(f.addFilterBlockdetect(filter -> fSupplier.set(filter)));
			checkNoVideoFilter();
		}

		@Test
		void testAddFilterBlurdetect() {
			assertFalse(f.addFilterBlurdetect(filter -> fSupplier.set(filter)));
			checkNoVideoFilter();
		}

		@Test
		void testAddFilterMEstimate() {
			assertFalse(f.addFilterMEstimate(filter -> fSupplier.set(filter)));
			checkNoVideoFilter();
		}

		@Test
		void testAddOptionalFilter_audio() {
			fSupplier = new AtomicReference<>();
			assertFalse(f.addOptionalFilter(audioFilterSupplier, filter -> fSupplier.set(filter)));
			assertNull(fSupplier.get());
			verify(afFunction, times(1)).apply(audioFilterSupplier);
		}

		@Test
		void testAddOptionalFilter_video() {
			fSupplier = new AtomicReference<>();
			assertFalse(f.addOptionalFilter(videoFilterSupplier, filter -> fSupplier.set(filter)));
			assertNull(fSupplier.get());
			verify(vfFunction, times(1)).apply(videoFilterSupplier);
		}

	}

}
