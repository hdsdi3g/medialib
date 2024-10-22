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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static tv.hd3g.fflauncher.TemporalProcessTraits.ffmpegDurationToDuration;
import static tv.hd3g.fflauncher.recipes.MediaAnalyserSessionFilterContext.getFromFilter;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import tv.hd3g.commons.testtools.Fake;
import tv.hd3g.commons.testtools.MockToolsExtendsJunit;
import tv.hd3g.fflauncher.about.FFAbout;
import tv.hd3g.fflauncher.filtering.AudioFilterSupplier;
import tv.hd3g.fflauncher.filtering.Filter;
import tv.hd3g.fflauncher.filtering.FilterSupplier;
import tv.hd3g.fflauncher.filtering.VideoFilterSupplier;
import tv.hd3g.ffprobejaxb.FFprobeJAXB;
import tv.hd3g.ffprobejaxb.data.FFProbeStream;
import tv.hd3g.processlauncher.ProcesslauncherLifecycle;
import tv.hd3g.processlauncher.processingtool.CallbackWatcher;

@ExtendWith(MockToolsExtendsJunit.class)
class MediaAnalyserBaseTest {

	class MAB extends MediaAnalyserBase<Object, Object> {

		protected MAB(final String execName, final FFAbout about, final CallbackWatcher watcher) {
			super(execName, about, watcher);
		}

		@Override
		protected Object compute(final Object sourceOrigin, final ProcesslauncherLifecycle lifeCycle) {
			throw new UnsupportedOperationException();
		}
	}

	@Mock
	FFAbout about;
	@Mock
	Filter filter;
	@Mock
	VideoFilterSupplier videoFilterSupplier;
	@Mock
	AudioFilterSupplier audioFilterSupplier;
	@Mock
	FFprobeJAXB ffprobeResult;
	@Mock
	FFProbeStream ffProbeStream;
	@Mock
	CallbackWatcher watcher;
	@Mock
	Object sourceOrigin;

	@Fake
	String execName;
	@Fake
	String sourceName;
	@Fake
	String filterName;
	String pgmFFDuration;
	String pgmFFStartTime;

	MAB mab;

	@BeforeEach
	void init() {
		mab = new MAB(execName, about, watcher);
		pgmFFDuration = "1s";
		pgmFFStartTime = "1s";
	}

	@Test
	void testgetParametersProvider_audioOnly() {
		assertThrows(IllegalArgumentException.class, () -> mab.getParametersProvider(sourceOrigin));

		when(about.isFilterIsAvaliable("astats")).thenReturn(true);
		mab.addFilterAstats(f -> {
		});
		assertThrows(IllegalArgumentException.class, () -> mab.getParametersProvider(sourceOrigin));

		mab.setSource(sourceName);
		final var result = mab.getParametersProvider(sourceOrigin);
		assertNotNull(result);
		assertEquals(execName, result.getExecutableName());
		assertEquals(
				"-vn <%IN_AUTOMATIC_0%> -loglevel level+warning -nostats -hide_banner -af astats=metadata=1:measure_perchannel=all:measure_overall=none",
				result.getInternalParameters().toString());

		verify(about, times(1)).isFilterIsAvaliable("astats");
	}

	@Test
	void testgetParametersProvider_videoOnly() {
		when(about.isFilterIsAvaliable(any())).thenReturn(true);
		mab.addFilterSiti(f -> {
		});
		mab.setSource(sourceName);

		final var result = mab.getParametersProvider(sourceOrigin).getInternalParameters().toString();
		assertEquals(
				"-an <%IN_AUTOMATIC_0%> -loglevel level+warning -nostats -hide_banner -vf siti",
				result);

		verify(about, times(1)).isFilterIsAvaliable(any());
	}

	@Test
	void testgetParametersProvider_audioVideoDurationST() {
		when(about.isFilterIsAvaliable(any())).thenReturn(true);
		mab.addFilterAstats(f -> {
		});
		mab.addFilterSiti(f -> {
		});
		mab.setSource(new File(sourceName));
		mab.setPgmFFDuration(ffmpegDurationToDuration(pgmFFDuration));
		mab.setPgmFFStartTime(ffmpegDurationToDuration(pgmFFStartTime));

		final var result = mab.getParametersProvider(sourceOrigin).getInternalParameters().toString();
		assertEquals(
				"<%IN_AUTOMATIC_0%> -loglevel level+warning -nostats -hide_banner -af astats=metadata=1:measure_perchannel=all:measure_overall=none -vf siti -t 00:00:01",
				result);

		verify(about, atLeast(1)).isFilterIsAvaliable(any());
	}

	@Nested
	class WithFFprobeResult {

		@BeforeEach
		void init() {
			when(about.isFilterIsAvaliable(any())).thenReturn(true);
			mab.setFfprobeResult(ffprobeResult);
			mab.setSource(sourceName);
		}

		@Test
		void hasAFilter_withAudio() {
			when(ffprobeResult.getAudioStreams()).thenReturn(Stream.of(ffProbeStream));
			setAFilter();
			assertNotNull(mab.getParametersProvider(sourceOrigin));
		}

		@Test
		void hasVFilter_withVideo() {
			when(ffprobeResult.getFirstVideoStream()).thenReturn(Optional.ofNullable(ffProbeStream));
			setVFilter();
			assertNotNull(mab.getParametersProvider(sourceOrigin));
		}

		@Test
		void hasAFilter_noAudio() {
			when(ffprobeResult.getAudioStreams()).thenReturn(Stream.empty());
			setAFilter();
			assertThrows(IllegalStateException.class, () -> mab.getParametersProvider(sourceOrigin));
		}

		@Test
		void hasVFilter_noVideo() {
			when(ffprobeResult.getFirstVideoStream()).thenReturn(Optional.empty());
			setVFilter();
			assertThrows(IllegalStateException.class, () -> mab.getParametersProvider(sourceOrigin));
		}

		@AfterEach
		void ends() {
			verify(ffprobeResult, atLeast(0)).getAudioStreams();
			verify(ffprobeResult, atLeast(0)).getFirstVideoStream();
			verify(about, atLeast(0)).isFilterIsAvaliable(any());
		}

		private void setAFilter() {
			mab.addFilterAstats(f -> {
			});
		}

		private void setVFilter() {
			mab.addFilterSiti(f -> {
			});
		}
	}

	@Test
	void testGetFFprobeResult() {
		assertFalse(mab.getFFprobeResult().isPresent());
		mab.setFfprobeResult(ffprobeResult);
		assertThat(mab.getFFprobeResult()).contains(ffprobeResult);
	}

	@Test
	void testAddFilterVideoFilterSupplier_added() {
		when(videoFilterSupplier.getFilterName()).thenReturn(filterName);
		when(about.isFilterIsAvaliable(filterName)).thenReturn(true);

		assertTrue(mab.addFilter(videoFilterSupplier));
		assertThat(mab.getVideoFilters()).size().isEqualTo(1);

		verify(videoFilterSupplier, atLeast(1)).getFilterName();
		verify(about, times(1)).isFilterIsAvaliable(filterName);
	}

	@Test
	void testAddFilterAudioFilterSupplier_added() {
		when(audioFilterSupplier.getFilterName()).thenReturn(filterName);
		when(about.isFilterIsAvaliable(filterName)).thenReturn(true);

		assertTrue(mab.addFilter(audioFilterSupplier));
		assertThat(mab.getAudioFilters()).size().isEqualTo(1);

		verify(audioFilterSupplier, atLeast(1)).getFilterName();
		verify(about, times(1)).isFilterIsAvaliable(filterName);
	}

	@Test
	void testAddFilterVideoFilterSupplier_refused() {
		when(videoFilterSupplier.getFilterName()).thenReturn(filterName);

		assertFalse(mab.addFilter(videoFilterSupplier));
		assertThat(mab.getVideoFilters()).isEmpty();

		verify(videoFilterSupplier, atLeast(1)).getFilterName();
		verify(about, times(1)).isFilterIsAvaliable(filterName);
	}

	@Test
	void testAddFilterAudioFilterSupplier_refused() {
		when(audioFilterSupplier.getFilterName()).thenReturn(filterName);

		assertFalse(mab.addFilter(audioFilterSupplier));
		assertThat(mab.getAudioFilters()).isEmpty();

		verify(audioFilterSupplier, atLeast(1)).getFilterName();
		verify(about, times(1)).isFilterIsAvaliable(filterName);
	}

	@Test
	void testGetAudioFilters() {
		when(about.isFilterIsAvaliable("astats")).thenReturn(true);
		when(about.isFilterIsAvaliable("ametadata")).thenReturn(true);

		assertThat(mab.getAudioFilters()).isEmpty();

		mab.addFilterAMetadata(f -> {
		});
		assertThat(mab.getAudioFilters()).isEmpty();

		mab.addFilterAstats(f -> {
		});
		assertThat(mab.getAudioFilters()).size().isEqualTo(1);

		verify(about, times(1)).isFilterIsAvaliable("astats");
		verify(about, times(1)).isFilterIsAvaliable("ametadata");
	}

	@Test
	void testGetVideoFilters() {
		when(about.isFilterIsAvaliable("siti")).thenReturn(true);
		when(about.isFilterIsAvaliable("metadata")).thenReturn(true);

		assertThat(mab.getVideoFilters()).isEmpty();

		mab.addFilterMetadata(f -> {
		});
		assertThat(mab.getVideoFilters()).isEmpty();

		mab.addFilterSiti(f -> {
		});
		assertThat(mab.getVideoFilters()).size().isEqualTo(1);

		verify(about, times(1)).isFilterIsAvaliable("siti");
		verify(about, times(1)).isFilterIsAvaliable("metadata");
	}

	@Test
	void testSetSourceString() {
		mab.setSource(sourceName);
		assertEquals(sourceName, mab.getSource());
	}

	@Test
	void testSetSourceFile() {
		mab.setSource(new File(sourceName));
		assertEquals(new File(sourceName), mab.getSourceFile());
	}

	@Test
	void testToString_noSource() {
		assertThrows(NullPointerException.class, () -> mab.toString());
	}

	@Test
	void testToString_StringSource() {
		mab.setSource(sourceName);
		assertEquals(sourceName, mab.toString());
	}

	@Test
	void testToString_FileSource() {
		mab.setSource(new File(sourceName));
		assertEquals(sourceName, mab.toString());
	}

	@Test
	void testGetFilterContextList() {
		when(about.isFilterIsAvaliable(any())).thenReturn(true);
		assertThat(mab.getFilterContextList()).isEmpty();

		final var aFilter = new AtomicReference<FilterSupplier>();
		mab.addFilterAstats(f -> {
			aFilter.set(f);
		});

		final var vFilter = new AtomicReference<FilterSupplier>();
		mab.addFilterSiti(f -> {
			vFilter.set(f);
		});

		assertThat(mab.getFilterContextList())
				.hasSize(2)
				.contains(
						getFromFilter(aFilter.get(), "audio"),
						getFromFilter(vFilter.get(), "video"));

		verify(about, times(1)).isFilterIsAvaliable("astats");
		verify(about, times(1)).isFilterIsAvaliable("siti");
	}

}
