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

import java.util.Optional;
import java.util.function.Supplier;

import lombok.extern.slf4j.Slf4j;
import tv.hd3g.fflauncher.about.FFAbout;
import tv.hd3g.fflauncher.filtering.FilterChains;
import tv.hd3g.fflauncher.filtering.lavfimtd.LavfiMetadataFilterParser;
import tv.hd3g.processlauncher.ProcesslauncherLifecycle;
import tv.hd3g.processlauncher.processingtool.CallbackWatcher;

@Slf4j
public class MediaAnalyserProcess extends
								  MediaAnalyserBase<MediaAnalyserProcessSetup, MediaAnalyserProcessResult>
								  implements AddFiltersTraits {

	private final LavfiMetadataFilterParser lavfiMetadataFilterParser;

	public MediaAnalyserProcess(final String execName,
								final FFAbout about) {
		this(execName, about, new CallbackWatcher(), new LavfiMetadataFilterParser());
	}

	protected MediaAnalyserProcess(final String execName,
								   final FFAbout about,
								   final CallbackWatcher watcher,
								   final LavfiMetadataFilterParser lavfiMetadataFilterParser) {
		super(execName, about, watcher);
		this.lavfiMetadataFilterParser = lavfiMetadataFilterParser;
		executorWatcher.setStdOutErrConsumer(lineEntry -> {
			final var line = lineEntry.line();
			if (lineEntry.stdErr()) {
				log.debug("Line: {}", line);
				return;
			}
			log.trace("Line: {}", line);
			lavfiMetadataFilterParser.addLavfiRawLine(line);
		});
	}

	@Override
	protected MediaAnalyserProcessResult compute(final MediaAnalyserProcessSetup sourceOrigin,
												 final ProcesslauncherLifecycle lifeCycle) {
		sourceOrigin.oLavfiLinesToMerge().stream()
				.flatMap(Supplier::get)
				.forEach(lavfiMetadataFilterParser::addLavfiRawLine);

		final var r128Target = FilterChains.parse("-af", ffmpeg.getInternalParameters())
				.stream()
				.map(MediaAnalyserProcessResult::extractEbur128TargetFromAFilterChains)
				.flatMap(Optional::stream)
				.findFirst();

		return new MediaAnalyserProcessResult(
				lavfiMetadataFilterParser.close(),
				getFilterContextList(),
				r128Target,
				lifeCycle.getFullCommandLine());
	}

}
