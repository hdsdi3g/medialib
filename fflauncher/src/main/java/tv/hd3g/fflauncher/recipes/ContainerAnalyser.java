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

import java.io.File;
import java.time.Duration;
import java.util.Objects;

import tv.hd3g.fflauncher.FFprobe;
import tv.hd3g.fflauncher.progress.FFprobeXMLProgressWatcher;
import tv.hd3g.processlauncher.cmdline.ExecutableFinder;
import tv.hd3g.processlauncher.cmdline.Parameters;

/**
 * ffprobe deep container analyser
 */
public class ContainerAnalyser {
	private final String execName;
	private final ExecutableFinder executableFinder;

	private static final FFprobeXMLProgressWatcher emptyWatcher = new FFprobeXMLProgressWatcher(
			Duration.ZERO,
			Thread::new,
			i -> {
			},
			x -> {
			},
			i -> {
			});

	public ContainerAnalyser(final String execName, final ExecutableFinder executableFinder) {
		this.execName = Objects.requireNonNull(execName);
		this.executableFinder = Objects.requireNonNull(executableFinder);
	}

	public ContainerAnalyserSession createSession(final File source,
												  final FFprobeXMLProgressWatcher progressWatcher) {
		return new ContainerAnalyserSession(this, null, source, progressWatcher);
	}

	@Deprecated(forRemoval = true)
	public ContainerAnalyserSession createSession(final File source) {// NOSONAR S1123
		return createSession(source, emptyWatcher);
	}

	public ContainerAnalyserSession createSession(final String source,
												  final FFprobeXMLProgressWatcher progressWatcher) {
		return new ContainerAnalyserSession(this, source, null, progressWatcher);
	}

	@Deprecated(forRemoval = true)
	public ContainerAnalyserSession createSession(final String source) {// NOSONAR S1123
		return createSession(source, emptyWatcher);
	}

	public FFprobe createFFprobe() {
		return new FFprobe(execName, new Parameters());
	}

	ExecutableFinder getExecutableFinder() {
		return executableFinder;
	}
}
