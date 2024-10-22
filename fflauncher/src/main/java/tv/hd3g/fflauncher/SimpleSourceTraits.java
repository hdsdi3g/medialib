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
package tv.hd3g.fflauncher;

import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static tv.hd3g.fflauncher.TemporalProcessTraits.positionToFFmpegPosition;

import java.awt.Point;
import java.io.File;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import tv.hd3g.fflauncher.filtering.Filter;
import tv.hd3g.processlauncher.cmdline.Parameters;

public interface SimpleSourceTraits extends InputSourceProviderTraits, InternalParametersSupplier {

	/**
	 * Define cmd var name like &lt;%IN_AUTOMATIC_n%&gt; with "n" the # of setted sources.
	 * Add -i parameter
	 * Add now in current Parameters the new add var only if not exists (you should call fixIOParametredVars, if you have add manually vars in Parametres)
	 */
	default void addSimpleInputSource(final String sourceName, final String... sourceOptions) {
		requireNonNull(sourceName, "\"sourceName\" can't to be null");

		if (sourceOptions == null) {
			addSimpleInputSource(sourceName, List.of());
		} else {
			addSimpleInputSource(sourceName, Arrays.stream(sourceOptions).toList());
		}
	}

	default void addSimpleInputSource(final String sourceName,
									  final Duration startTime,
									  final String... sourceOptions) {
		requireNonNull(sourceName, "\"sourceName\" can't to be null");
		requireNonNull(startTime, "\"startTime\" can't to be null");

		if (sourceOptions == null) {
			addSimpleInputSource(sourceName, "-ss", positionToFFmpegPosition(startTime));
		} else {
			final var sp = new Parameters();
			sp.addParameters("-ss", positionToFFmpegPosition(startTime));
			sp.addParameters(sourceOptions);
			addSimpleInputSource(sourceName, sp.getParameters());
		}
	}

	/**
	 * Define cmd var name like &lt;%IN_AUTOMATIC_n%&gt; with "n" the # of setted sources.
	 * Add -i parameter
	 * Add now in current Parameters the new add var only if not exists (you should call fixIOParametredVars, if you have add manually vars in Parametres)
	 */
	default void addSimpleInputSource(final File file, final String... sourceOptions) {
		requireNonNull(file, "\"file\" can't to be null");

		if (sourceOptions == null) {
			addSimpleInputSource(file, List.of());
		} else {
			addSimpleInputSource(file, stream(sourceOptions).toList());
		}
	}

	default void addSimpleInputSource(final File file,
									  final Duration startTime,
									  final String... sourceOptions) {
		requireNonNull(file, "\"sourceName\" can't to be null");
		requireNonNull(startTime, "\"startTime\" can't to be null");

		if (sourceOptions == null) {
			addSimpleInputSource(file, "-ss", positionToFFmpegPosition(startTime));
		} else {
			final var sp = new Parameters();
			sp.addParameters("-ss", positionToFFmpegPosition(startTime));
			sp.addParameters(sourceOptions);
			addSimpleInputSource(file, sp.getParameters());
		}
	}

	void addSimpleInputSource(final String sourceName, final List<String> sourceOptions);

	void addSimpleInputSource(final File file, final List<String> sourceOptions);

	static void addSmptehdbarsGeneratorAsInputSource(final SimpleSourceTraits source,
													 final Point resolution,
													 final int durationInSec,
													 final String frameRate) {
		final var f = new Filter("smptehdbars");
		f.addArgument("duration", durationInSec);
		f.addArgument("size", resolution.x + "x" + resolution.y);
		f.addArgument("rate", frameRate);
		source.addSimpleInputSource(f.toString(), "-f", "lavfi");
	}

	static void addSineAudioGeneratorAsInputSource(final SimpleSourceTraits source,
												   final int frequency,
												   final int durationInSec,
												   final int sampleRate) {
		final var f = new Filter("sine");
		f.addArgument("duration", durationInSec);
		f.addArgument("frequency", frequency);
		f.addArgument("sample_rate", sampleRate);
		source.addSimpleInputSource(f.toString(), "-f", "lavfi");
	}
}
