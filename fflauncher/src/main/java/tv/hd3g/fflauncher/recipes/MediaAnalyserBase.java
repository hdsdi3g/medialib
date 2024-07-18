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

import static tv.hd3g.fflauncher.enums.FFLogLevel.WARNING;
import static tv.hd3g.fflauncher.recipes.MediaAnalyserSessionFilterContext.getFromFilter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.Setter;
import tv.hd3g.fflauncher.FFmpeg;
import tv.hd3g.fflauncher.about.FFAbout;
import tv.hd3g.fflauncher.filtering.AudioFilterSupplier;
import tv.hd3g.fflauncher.filtering.FilterChains;
import tv.hd3g.fflauncher.filtering.FilterSupplier;
import tv.hd3g.fflauncher.filtering.VideoFilterSupplier;
import tv.hd3g.fflauncher.processingtool.FFmpegToolBuilder;
import tv.hd3g.ffprobejaxb.FFprobeJAXB;
import tv.hd3g.processlauncher.processingtool.CallbackWatcher;

public abstract class MediaAnalyserBase<O, T> extends
									   FFmpegToolBuilder<O, T, CallbackWatcher>
									   implements AddFiltersTraits {

	protected final FFAbout about;
	protected final List<AudioFilterSupplier> audioFilters;
	protected final List<VideoFilterSupplier> videoFilters;

	@Getter
	protected String source;
	@Getter
	protected File sourceFile;
	@Setter
	protected FFprobeJAXB ffprobeResult;
	@Setter
	protected String pgmFFDuration;
	@Setter
	protected String pgmFFStartTime;

	protected MediaAnalyserBase(final String execName,
								final FFAbout about,
								final CallbackWatcher watcher) {
		super(new FFmpeg(execName), watcher);
		this.about = Objects.requireNonNull(about, "\"about\" can't to be null");
		audioFilters = Collections.synchronizedList(new ArrayList<>());
		videoFilters = Collections.synchronizedList(new ArrayList<>());

	}

	public void setSource(final String source) {
		this.source = source;
	}

	public void setSource(final File sourceFile) {
		this.sourceFile = sourceFile;
	}

	protected List<MediaAnalyserSessionFilterContext> getFilterContextList() {
		return Stream.concat(
				getAudioFilters().stream()
						.map(f -> getFromFilter(f, "audio")),
				getVideoFilters().stream()
						.map(f -> getFromFilter(f, "video")))
				.distinct()
				.sorted((l, r) -> l.type().concat(l.name()).compareTo(r.type().concat(r.name())))
				.toList();
	}

	@Override
	protected FFmpeg getParametersProvider(final O sourceOrigin) {
		if (audioFilters.isEmpty() && videoFilters.isEmpty()) {
			throw new IllegalArgumentException("No filters are sets");
		}

		if (ffprobeResult != null) {
			if (ffprobeResult.getAudioStreams().count() == 0 && audioFilters.isEmpty() == false) {
				throw new IllegalStateException("Can't apply audio filters if source don't have an audio stream!");
			} else if (ffprobeResult.getFirstVideoStream().isEmpty() && videoFilters.isEmpty() == false) {
				throw new IllegalStateException("Can't apply video filters if source don't have an video stream!");
			}
		}

		ffmpeg.setHidebanner();
		ffmpeg.setNostats();
		ffmpeg.setLogLevel(WARNING, false, true);

		if (source != null) {
			ffmpeg.addSimpleInputSource(source);
		} else if (sourceFile != null) {
			ffmpeg.addSimpleInputSource(sourceFile);
		} else {
			throw new IllegalArgumentException("No source are set");
		}

		if (audioFilters.isEmpty() == false) {
			final var chain = new FilterChains();
			chain.createChain()
					.addAll(audioFilters.stream()
							.map(FilterSupplier::toFilter)
							.toList());
			chain.pushFilterChainTo("-af", ffmpeg);
		} else {
			ffmpeg.setNoAudio();
		}

		if (videoFilters.isEmpty() == false) {
			final var chain = new FilterChains();
			chain.createChain()
					.addAll(videoFilters.stream()
							.map(FilterSupplier::toFilter)
							.toList());
			chain.pushFilterChainTo("-vf", ffmpeg);
		} else {
			ffmpeg.setNoVideo();
		}

		ffmpeg.addSimpleOutputDestination("-", "null");

		if (pgmFFDuration != null && pgmFFDuration.isEmpty() == false) {
			ffmpeg.addDuration(pgmFFDuration);
		}
		if (pgmFFStartTime != null && pgmFFStartTime.isEmpty() == false) {
			ffmpeg.addStartPosition(pgmFFStartTime);
		}

		return ffmpeg;
	}

	/**
	 * @return unmodifiable list w/o ametadata filter
	 */
	public List<AudioFilterSupplier> getAudioFilters() {
		return audioFilters.stream()
				.filter(f -> f.getFilterName().equals("ametadata") == false)
				.toList();
	}

	/**
	 * @return unmodifiable list w/o metadata filter
	 */
	public List<VideoFilterSupplier> getVideoFilters() {
		return videoFilters.stream()
				.filter(f -> f.getFilterName().equals("metadata") == false)
				.toList();
	}

	public Optional<FFprobeJAXB> getFFprobeResult() {
		return Optional.ofNullable(ffprobeResult);
	}

	@Override
	public String toString() {
		if (source != null) {
			return source;
		} else {
			return sourceFile.getPath();
		}
	}

	/**
	 * @return true if current ffmpeg can manage this filter. Else it don't be added on filter list.
	 */
	@Override
	public boolean addFilter(final VideoFilterSupplier vf) {
		if (about.isFilterIsAvaliable(vf.getFilterName())) {
			videoFilters.add(vf);
			return true;
		}
		return false;
	}

	/**
	 * @return true if current ffmpeg can manage this filter. Else it don't be added on filter list.
	 */
	@Override
	public boolean addFilter(final AudioFilterSupplier af) {
		if (about.isFilterIsAvaliable(af.getFilterName())) {
			audioFilters.add(af);
			return true;
		}
		return false;
	}

}
