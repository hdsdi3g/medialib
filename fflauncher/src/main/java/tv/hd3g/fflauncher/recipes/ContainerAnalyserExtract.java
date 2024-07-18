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

import static java.util.Objects.requireNonNull;

import java.util.LinkedList;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import tv.hd3g.fflauncher.processingtool.FFSourceDefinition;
import tv.hd3g.fflauncher.progress.FFprobeXMLProgressWatcher;
import tv.hd3g.fflauncher.progress.FFprobeXMLProgressWatcher.ProgressConsumer;
import tv.hd3g.processlauncher.ProcesslauncherLifecycle;
import tv.hd3g.processlauncher.processingtool.CallbackWatcher;

@Slf4j
public class ContainerAnalyserExtract extends ContainerAnalyserBase<ContainerAnalyserExtractResult, CallbackWatcher> {

	private final LinkedList<String> stdOutLines;
	private ProgressConsumer progressConsumer;

	public ContainerAnalyserExtract(final String execName) {
		super(execName, new CallbackWatcher());
		stdOutLines = new LinkedList<>();
		executorWatcher.setStdOutErrConsumer(lineEntry -> {
			final var line = lineEntry.line();
			log.trace("Line: {}", line);
			if (lineEntry.stdErr() == false) {
				stdOutLines.add(line);
				if (progressConsumer != null) {
					progressConsumer.accept(line);
				}
			}
		});
	}

	@Override
	public void setProgressWatcher(final FFprobeXMLProgressWatcher progressWatcher) {
		super.setProgressWatcher(progressWatcher);
		progressConsumer = requireNonNull(progressWatcher, "\"progressWatcher\" can't to be null").createProgress(this);
	}

	@Override
	protected ContainerAnalyserExtractResult compute(final FFSourceDefinition sourceOrigin,
													 final ProcesslauncherLifecycle lifeCycle) {
		Optional.ofNullable(progressConsumer)
				.ifPresent(ProgressConsumer::waitForEnd);
		return new ContainerAnalyserExtractResult(stdOutLines.stream().toList(), lifeCycle.getFullCommandLine());
	}

}
