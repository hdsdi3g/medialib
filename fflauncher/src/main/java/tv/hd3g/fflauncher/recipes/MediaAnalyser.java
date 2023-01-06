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

import static java.lang.Integer.MAX_VALUE;
import static java.util.Collections.unmodifiableList;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import tv.hd3g.fflauncher.FFmpeg;
import tv.hd3g.fflauncher.about.FFAbout;
import tv.hd3g.fflauncher.filtering.AudioFilterSupplier;
import tv.hd3g.fflauncher.filtering.VideoFilterSupplier;
import tv.hd3g.fflauncher.progress.ProgressCallback;
import tv.hd3g.fflauncher.progress.ProgressListener;
import tv.hd3g.processlauncher.cmdline.ExecutableFinder;
import tv.hd3g.processlauncher.cmdline.Parameters;

/**
 * ffmpeg, with filters, deep media analyser
 */
public class MediaAnalyser implements AddFiltersTraits {
	private final String execName;
	private final ExecutableFinder executableFinder;
	private final FFAbout about;
	private final List<AudioFilterSupplier> audioFilters;
	private final List<VideoFilterSupplier> videoFilters;
	private ProgressListener progressListener;
	private ProgressCallback progressCallback;

	public MediaAnalyser(final String execName, final ExecutableFinder executableFinder, final FFAbout about) {
		this.execName = Objects.requireNonNull(execName);
		this.executableFinder = Objects.requireNonNull(executableFinder);
		this.about = Objects.requireNonNull(about, "\"about\" can't to be null");
		audioFilters = Collections.synchronizedList(new ArrayList<>());
		videoFilters = Collections.synchronizedList(new ArrayList<>());
	}

	public MediaAnalyserSession createSession(final File source) {
		return new MediaAnalyserSession(this, null, source);
	}

	public MediaAnalyserSession createSession(final String source) {
		return new MediaAnalyserSession(this, source, null);
	}

	public ExecutableFinder getExecutableFinder() {
		return executableFinder;
	}

	List<AudioFilterSupplier> getAudioFilters() {
		return audioFilters;
	}

	List<VideoFilterSupplier> getVideoFilters() {
		return videoFilters;
	}

	public void setProgress(final ProgressListener progressListener, final ProgressCallback progressCallback) {
		this.progressListener = progressListener;
		this.progressCallback = progressCallback;
		if (progressListener == null ^ progressCallback == null) {
			throw new IllegalArgumentException("You must set or reset both listener and callback");
		}
	}

	/**
	 * @return true if current ffmpeg can manage this filter. Else it don't be added on filter list.
	 */
	@Override
	public boolean addFilter(final VideoFilterSupplier vf) {
		if (about.isFilterIsAvaliable(vf.toFilter().getFilterName())) {
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
		if (about.isFilterIsAvaliable(af.toFilter().getFilterName())) {
			audioFilters.add(af);
			return true;
		}
		return false;
	}

	public FFmpeg createFFmpeg() {
		final var ffmpeg = new FFmpeg(execName, new Parameters());
		if (progressListener != null) {
			ffmpeg.setProgressListener(progressListener, progressCallback);
		}
		return ffmpeg;
	}

	/**
	 * @param item like "AABB"
	 * @param assertStarts like "AA"
	 * @return like "BB"
	 */
	public static String assertAndParse(final String item, final String assertStarts) {
		return assertAndParse(item, assertStarts, l -> l);
	}

	public static <T> T assertAndParse(final String item, final String assertStarts, final Function<String, T> parser) {
		if (item.startsWith(assertStarts) == false) {
			throw new IllegalArgumentException("Not a " + assertStarts + ": " + item);
		}
		return parser.apply(item.substring(assertStarts.length()));
	}

	public static List<String> splitter(final String line, final char with) {
		return splitter(line, with, MAX_VALUE);
	}

	public static List<String> splitter(final String line, final char with, final int max) {
		var currentChars = new StringBuilder();
		final List<String> result = new ArrayList<>();

		char chr;
		for (var pos = 0; pos < line.length(); pos++) {
			chr = line.charAt(pos);
			if (chr != with) {
				currentChars.append(chr);
			} else if (currentChars.isEmpty() == false) {
				result.add(currentChars.toString());
				currentChars = new StringBuilder();
				if (result.size() >= max) {
					return unmodifiableList(result);
				}
			}
		}

		if (currentChars.isEmpty() == false) {
			result.add(currentChars.toString());
		}

		return unmodifiableList(result);
	}

}
