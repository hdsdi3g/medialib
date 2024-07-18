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
package tv.hd3g.fflauncher.recipes.wavmeasure;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.time.Duration;
import java.util.function.Consumer;

import lombok.Getter;
import tv.hd3g.fflauncher.FFmpeg;
import tv.hd3g.fflauncher.SimpleSourceTraits;

public class WavMeasureSetup {

	public static final int SAMPLE_RATE = 48000;

	@Getter
	private final Duration fileDuration;
	@Getter
	private final int outputWide;

	private String source;
	private File sourceFile;
	private Consumer<SimpleSourceTraits> setFFmpegSource;

	private WavMeasureSetup(final Duration fileDuration, final int outputWide) {
		this.fileDuration = requireNonNull(fileDuration, "\"fileDuration\" can't to be null");
		this.outputWide = outputWide;
		if (outputWide < 1) {
			throw new IllegalArgumentException("Invalid outputWide: " + outputWide);
		}
	}

	public WavMeasureSetup(final String source,
						   final Duration fileDuration,
						   final int outputWide) {
		this(fileDuration, outputWide);
		this.source = requireNonNull(source, "\"source\" can't to be null");
	}

	public WavMeasureSetup(final File source,
						   final Duration fileDuration,
						   final int outputWide) {
		this(fileDuration, outputWide);
		sourceFile = requireNonNull(source, "\"source\" can't to be null");
	}

	public WavMeasureSetup(final Consumer<SimpleSourceTraits> setFFmpegSource,
						   final Duration fileDuration,
						   final int outputWide) {
		this(fileDuration, outputWide);
		this.setFFmpegSource = requireNonNull(setFFmpegSource, "\"setFFmpegSource\" can't to be null");
	}

	void applySource(final FFmpeg ffmpeg) {
		if (source != null) {
			ffmpeg.addSimpleInputSource(source);
		} else if (sourceFile != null) {
			ffmpeg.addSimpleInputSource(sourceFile);
		} else {
			setFFmpegSource.accept(ffmpeg);
		}
	}

}
