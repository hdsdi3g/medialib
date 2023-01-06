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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import tv.hd3g.fflauncher.filtering.VideoFilterCropdetect.Mode;
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
	@Mock
	Mode mode;

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
		verifyNoMoreInteractions(vfFunction, afFunction, videoFilterSupplier, audioFilterSupplier, mode);
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
		void init() throws Exception {
			when(vfFunction.apply(any())).thenReturn(true);
			when(afFunction.apply(any())).thenReturn(true);
		}

		@Test
		void testAddFilterPhasemeter() {
			assertEquals(f, f.addFilterPhasemeter(filter -> fSupplier.set(filter)));
			checkAudioFilter();
		}

		@Test
		void testAddFilterAstats() {
			assertEquals(f, f.addFilterAstats(filter -> fSupplier.set(filter)));
			checkAudioFilter();
		}

		@Test
		void testAddFilterSilencedetect() {
			assertEquals(f, f.addFilterSilencedetect(filter -> fSupplier.set(filter)));
			checkAudioFilter();
		}

		@Test
		void testAddFilterVolumedetect() {
			assertEquals(f, f.addFilterVolumedetect(filter -> fSupplier.set(filter)));
			checkAudioFilter();
		}

		@Test
		void testAddFilterEbur128() {
			assertEquals(f, f.addFilterEbur128(filter -> fSupplier.set(filter)));
			checkAudioFilter();
		}

		@Test
		void testAddFilterAMetadata() {
			assertEquals(f, f.addFilterAMetadata(filter -> fSupplier.set(filter)));
			checkAudioFilter();
		}

		@Test
		void testAddFilterMetadata() {
			assertEquals(f, f.addFilterMetadata(filter -> fSupplier.set(filter)));
			checkVideoFilter();
		}

		@Test
		void testAddFilterSiti() {
			assertEquals(f, f.addFilterSiti(filter -> fSupplier.set(filter)));
			checkVideoFilter();
		}

		@Test
		void testAddFilterIdet() {
			assertEquals(f, f.addFilterIdet(filter -> fSupplier.set(filter)));
			checkVideoFilter();
		}

		@Test
		void testAddFilterFreezedetect() {
			assertEquals(f, f.addFilterFreezedetect(filter -> fSupplier.set(filter)));
			checkVideoFilter();
		}

		@Test
		void testAddFilterBlackdetect() {
			assertEquals(f, f.addFilterBlackdetect(filter -> fSupplier.set(filter)));
			checkVideoFilter();
		}

		@Test
		void testAddFilterCropdetect() {
			assertEquals(f, f.addFilterCropdetect(mode, filter -> fSupplier.set(filter)));
			checkVideoFilter();
			final var crop = (VideoFilterCropdetect) videoFilterSupplierCaptor.getValue();
			assertEquals(mode, crop.getMode());
		}

		@Test
		void testAddFilterBlockdetect() {
			assertEquals(f, f.addFilterBlockdetect(filter -> fSupplier.set(filter)));
			checkVideoFilter();
		}

		@Test
		void testAddFilterBlurdetect() {
			assertEquals(f, f.addFilterBlurdetect(filter -> fSupplier.set(filter)));
			checkVideoFilter();
		}

		@Test
		void testAddFilterMEstimate() {
			assertEquals(f, f.addFilterMEstimate(filter -> fSupplier.set(filter)));
			checkVideoFilter();
		}

		@Test
		void testAddOptionalFilter_audio() {
			assertEquals(f, f.addOptionalFilter(audioFilterSupplier, filter -> fSupplier.set(filter)));
			assertEquals(fSupplier.get(), audioFilterSupplier);
			verify(afFunction, times(1)).apply(audioFilterSupplier);
		}

		@Test
		void testAddOptionalFilter_video() {
			assertEquals(f, f.addOptionalFilter(videoFilterSupplier, filter -> fSupplier.set(filter)));
			assertEquals(fSupplier.get(), videoFilterSupplier);
			verify(vfFunction, times(1)).apply(videoFilterSupplier);
		}

	}

	@Nested
	class FilterNotAvailable {

		@BeforeEach
		void init() throws Exception {
			when(vfFunction.apply(any())).thenReturn(false);
			when(afFunction.apply(any())).thenReturn(false);
		}

		@Test
		void testAddFilterPhasemeter() {
			assertEquals(f, f.addFilterPhasemeter(filter -> fSupplier.set(filter)));
			checkNoAudioFilter();
		}

		@Test
		void testAddFilterAstats() {
			assertEquals(f, f.addFilterAstats(filter -> fSupplier.set(filter)));
			checkNoAudioFilter();
		}

		@Test
		void testAddFilterSilencedetect() {
			assertEquals(f, f.addFilterSilencedetect(filter -> fSupplier.set(filter)));
			checkNoAudioFilter();
		}

		@Test
		void testAddFilterVolumedetect() {
			assertEquals(f, f.addFilterVolumedetect(filter -> fSupplier.set(filter)));
			checkNoAudioFilter();
		}

		@Test
		void testAddFilterEbur128() {
			assertEquals(f, f.addFilterEbur128(filter -> fSupplier.set(filter)));
			checkNoAudioFilter();
		}

		@Test
		void testAddFilterAMetadata() {
			assertEquals(f, f.addFilterAMetadata(filter -> fSupplier.set(filter)));
			checkNoAudioFilter();
		}

		@Test
		void testAddFilterMetadata() {
			assertEquals(f, f.addFilterMetadata(filter -> fSupplier.set(filter)));
			checkNoVideoFilter();
		}

		@Test
		void testAddFilterSiti() {
			assertEquals(f, f.addFilterSiti(filter -> fSupplier.set(filter)));
			checkNoVideoFilter();
		}

		@Test
		void testAddFilterIdet() {
			assertEquals(f, f.addFilterIdet(filter -> fSupplier.set(filter)));
			checkNoVideoFilter();
		}

		@Test
		void testAddFilterFreezedetect() {
			assertEquals(f, f.addFilterFreezedetect(filter -> fSupplier.set(filter)));
			checkNoVideoFilter();
		}

		@Test
		void testAddFilterBlackdetect() {
			assertEquals(f, f.addFilterBlackdetect(filter -> fSupplier.set(filter)));
			checkNoVideoFilter();
		}

		@Test
		void testAddFilterCropdetect() {
			assertEquals(f, f.addFilterCropdetect(mode, filter -> fSupplier.set(filter)));
			checkNoVideoFilter();
			final var crop = (VideoFilterCropdetect) videoFilterSupplierCaptor.getValue();
			assertEquals(mode, crop.getMode());
		}

		@Test
		void testAddFilterBlockdetect() {
			assertEquals(f, f.addFilterBlockdetect(filter -> fSupplier.set(filter)));
			checkNoVideoFilter();
		}

		@Test
		void testAddFilterBlurdetect() {
			assertEquals(f, f.addFilterBlurdetect(filter -> fSupplier.set(filter)));
			checkNoVideoFilter();
		}

		@Test
		void testAddFilterMEstimate() {
			assertEquals(f, f.addFilterMEstimate(filter -> fSupplier.set(filter)));
			checkNoVideoFilter();
		}

		@Test
		void testAddOptionalFilter_audio() {
			final var fSupplier = new AtomicReference<FilterSupplier>();
			assertEquals(f, f.addOptionalFilter(audioFilterSupplier, filter -> fSupplier.set(filter)));
			assertNull(fSupplier.get());
			verify(afFunction, times(1)).apply(audioFilterSupplier);
		}

		@Test
		void testAddOptionalFilter_video() {
			final var fSupplier = new AtomicReference<FilterSupplier>();
			assertEquals(f, f.addOptionalFilter(videoFilterSupplier, filter -> fSupplier.set(filter)));
			assertNull(fSupplier.get());
			verify(vfFunction, times(1)).apply(videoFilterSupplier);
		}

	}

}
