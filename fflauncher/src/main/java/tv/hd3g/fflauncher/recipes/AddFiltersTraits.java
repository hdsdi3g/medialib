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

import java.util.Set;
import java.util.function.Consumer;

import tv.hd3g.fflauncher.filtering.AbstractFilterMetadata;
import tv.hd3g.fflauncher.filtering.AudioFilterAMetadata;
import tv.hd3g.fflauncher.filtering.AudioFilterAPhasemeter;
import tv.hd3g.fflauncher.filtering.AudioFilterAstats;
import tv.hd3g.fflauncher.filtering.AudioFilterEbur128;
import tv.hd3g.fflauncher.filtering.AudioFilterEbur128.Peak;
import tv.hd3g.fflauncher.filtering.AudioFilterSilencedetect;
import tv.hd3g.fflauncher.filtering.AudioFilterSupplier;
import tv.hd3g.fflauncher.filtering.AudioFilterVolumedetect;
import tv.hd3g.fflauncher.filtering.VideoFilterBlackdetect;
import tv.hd3g.fflauncher.filtering.VideoFilterBlockdetect;
import tv.hd3g.fflauncher.filtering.VideoFilterBlurdetect;
import tv.hd3g.fflauncher.filtering.VideoFilterCropdetect;
import tv.hd3g.fflauncher.filtering.VideoFilterFreezedetect;
import tv.hd3g.fflauncher.filtering.VideoFilterIdet;
import tv.hd3g.fflauncher.filtering.VideoFilterMEstimate;
import tv.hd3g.fflauncher.filtering.VideoFilterMetadata;
import tv.hd3g.fflauncher.filtering.VideoFilterSiti;
import tv.hd3g.fflauncher.filtering.VideoFilterSupplier;

public interface AddFiltersTraits {

	boolean addFilter(final VideoFilterSupplier vf);

	boolean addFilter(final AudioFilterSupplier af);

	default AddFiltersTraits addFilterPhasemeter(final Consumer<AudioFilterAPhasemeter> filterifPresent) {
		return addOptionalFilter(new AudioFilterAPhasemeter(), filterifPresent);
	}

	default AddFiltersTraits addFilterAstats(final Consumer<AudioFilterAstats> filterifPresent) {
		final var f = new AudioFilterAstats();
		f.setSelectedMetadatas();
		return addOptionalFilter(f, filterifPresent);
	}

	default AddFiltersTraits addFilterSilencedetect(final Consumer<AudioFilterSilencedetect> filterifPresent) {
		final var f = new AudioFilterSilencedetect();
		f.setMono(true);
		return addOptionalFilter(f, filterifPresent);
	}

	default AddFiltersTraits addFilterVolumedetect(final Consumer<AudioFilterVolumedetect> filterifPresent) {
		return addOptionalFilter(new AudioFilterVolumedetect(), filterifPresent);
	}

	default AddFiltersTraits addFilterEbur128(final Consumer<AudioFilterEbur128> filterifPresent) {
		final var f = new AudioFilterEbur128();
		f.setPeakMode(Set.of(Peak.SAMPLE, Peak.TRUE));
		return addOptionalFilter(f, filterifPresent);
	}

	default AddFiltersTraits addFilterSiti(final Consumer<VideoFilterSiti> filterifPresent) {
		return addOptionalFilter(new VideoFilterSiti(), filterifPresent);
	}

	default AddFiltersTraits addFilterIdet(final Consumer<VideoFilterIdet> filterifPresent) {
		return addOptionalFilter(new VideoFilterIdet(), filterifPresent);
	}

	default AddFiltersTraits addFilterFreezedetect(final Consumer<VideoFilterFreezedetect> filterifPresent) {
		return addOptionalFilter(new VideoFilterFreezedetect(), filterifPresent);
	}

	default AddFiltersTraits addFilterBlackdetect(final Consumer<VideoFilterBlackdetect> filterifPresent) {
		return addOptionalFilter(new VideoFilterBlackdetect(), filterifPresent);
	}

	default AddFiltersTraits addFilterCropdetect(final Consumer<VideoFilterCropdetect> filterifPresent) {
		return addOptionalFilter(new VideoFilterCropdetect(), filterifPresent);
	}

	default AddFiltersTraits addFilterBlockdetect(final Consumer<VideoFilterBlockdetect> filterifPresent) {
		return addOptionalFilter(new VideoFilterBlockdetect(), filterifPresent);
	}

	default AddFiltersTraits addFilterBlurdetect(final Consumer<VideoFilterBlurdetect> filterifPresent) {
		return addOptionalFilter(new VideoFilterBlurdetect(), filterifPresent);
	}

	default AddFiltersTraits addFilterMEstimate(final Consumer<VideoFilterMEstimate> filterifPresent) {
		return addOptionalFilter(new VideoFilterMEstimate(), filterifPresent);
	}

	default AddFiltersTraits addFilterMetadata(final Consumer<VideoFilterMetadata> filterifPresent) {
		final var vMetadata = new VideoFilterMetadata(AbstractFilterMetadata.Mode.PRINT);
		vMetadata.setFile("-");
		return addOptionalFilter(vMetadata, filterifPresent);
	}

	default AddFiltersTraits addFilterAMetadata(final Consumer<AudioFilterAMetadata> filterifPresent) {
		final var aMetadata = new AudioFilterAMetadata(AbstractFilterMetadata.Mode.PRINT);
		aMetadata.setFile("-");
		return addOptionalFilter(aMetadata, filterifPresent);
	}

	default <T extends AudioFilterSupplier> AddFiltersTraits addOptionalFilter(final T filter,
																			   final Consumer<T> filterifPresent) {
		if (addFilter(filter)) {
			filterifPresent.accept(filter);
		}
		return this;
	}

	default <T extends VideoFilterSupplier> AddFiltersTraits addOptionalFilter(final T filter,
																			   final Consumer<T> filterifPresent) {
		if (addFilter(filter)) {
			filterifPresent.accept(filter);
		}
		return this;
	}

}
