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

import java.util.LinkedList;

import lombok.extern.slf4j.Slf4j;
import tv.hd3g.fflauncher.about.FFAbout;
import tv.hd3g.processlauncher.ProcesslauncherLifecycle;
import tv.hd3g.processlauncher.processingtool.CallbackWatcher;

@Slf4j
public class MediaAnalyserExtract extends
								  MediaAnalyserBase<Void, MediaAnalyserExtractResult>
								  implements AddFiltersTraits {

	private final LinkedList<String> stdOutLines;
	private final LinkedList<String> stdErrLines;

	public MediaAnalyserExtract(final String execName,
								final FFAbout about) {
		super(execName, about, new CallbackWatcher());
		stdOutLines = new LinkedList<>();
		stdErrLines = new LinkedList<>();
		executorWatcher.setStdOutErrConsumer(
				lineEntry -> {
					final var line = lineEntry.line();
					log.trace("Line: {}", line);
					if (lineEntry.stdErr()) {
						stdErrLines.add(line);
					} else {
						stdOutLines.add(line);
					}
				});
	}

	@Override
	protected MediaAnalyserExtractResult compute(final Void sourceOrigin, final ProcesslauncherLifecycle lifeCycle) {
		return new MediaAnalyserExtractResult(
				stdOutLines.stream().toList(),
				stdErrLines.stream().toList(),
				lifeCycle.getFullCommandLine(),
				getFilterContextList());
	}

}
