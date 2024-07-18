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

import static tv.hd3g.fflauncher.FFprobe.FFPrintFormat.XML;

import java.time.Duration;

import lombok.Setter;
import tv.hd3g.fflauncher.FFprobe;
import tv.hd3g.fflauncher.processingtool.FFprobeToolBuilder;
import tv.hd3g.fflauncher.progress.FFprobeXMLProgressWatcher;
import tv.hd3g.processlauncher.processingtool.ExecutorWatcher;

public abstract class ContainerAnalyserBase<T, W extends ExecutorWatcher> extends FFprobeToolBuilder<T, W> {

	@Setter
	protected FFprobeXMLProgressWatcher progressWatcher;

	protected ContainerAnalyserBase(final String execName, final W watcher) {
		super(new FFprobe(execName), watcher);
		ffprobe.setHidebanner();
		ffprobe.setShowFrames();
		ffprobe.setShowPackets();
		ffprobe.setPrintFormat(XML);
		progressWatcher = emptyWatcher;
	}

	static final FFprobeXMLProgressWatcher emptyWatcher = new FFprobeXMLProgressWatcher(
			Duration.ZERO,
			Thread::new,
			i -> {
			},
			x -> {
			},
			i -> {
			});

}
