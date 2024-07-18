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
 * Copyright (C) hdsdi3g for hd3g.tv 2018
 *
 */
package tv.hd3g.fflauncher.recipes;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;

import tv.hd3g.fflauncher.FFprobe;
import tv.hd3g.fflauncher.FFprobe.FFPrintFormat;
import tv.hd3g.fflauncher.enums.FFLogLevel;
import tv.hd3g.fflauncher.processingtool.FFSourceDefinition;
import tv.hd3g.fflauncher.processingtool.FFprobeToolBuilder;
import tv.hd3g.ffprobejaxb.FFprobeJAXB;
import tv.hd3g.processlauncher.ProcesslauncherLifecycle;
import tv.hd3g.processlauncher.processingtool.KeepStdoutAndErrToLogWatcher;

public class ProbeMedia extends FFprobeToolBuilder<FFprobeJAXB, KeepStdoutAndErrToLogWatcher> {

	public ProbeMedia(final String execName,
					  final ScheduledExecutorService maxExecTimeScheduler) {
		super(new FFprobe(execName), new KeepStdoutAndErrToLogWatcher());
		setMaxExecutionTime(Duration.ofSeconds(5), maxExecTimeScheduler);

		ffprobe.setPrintFormat(FFPrintFormat.XML).setShowStreams().setShowFormat().setShowChapters().isHidebanner();
		ffprobe.setLogLevel(FFLogLevel.ERROR, false, false);
		ffprobe.setFilterForLinesEventsToDisplay(
				l -> (l.stdErr()
					  && ffprobe.filterOutErrorLines().test(l.line())));
	}

	public ProbeMedia(final ScheduledExecutorService maxExecTimeScheduler) {
		this("ffprobe", maxExecTimeScheduler);
	}

	@Override
	protected FFprobeJAXB compute(final FFSourceDefinition sourceOrigin, final ProcesslauncherLifecycle lifeCycle) {
		return FFprobeJAXB.load(executorWatcher.getTextRetention().getStdout(false, System.lineSeparator()));
	}

}
