/*
 * This file is part of processlauncher.
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
package tv.hd3g.processlauncher.processingtool;

import static java.util.Objects.requireNonNull;
import static tv.hd3g.processlauncher.CapturedStreams.BOTH_STDOUT_STDERR;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.event.Level;

import lombok.Getter;
import tv.hd3g.processlauncher.CapturedStdOutErrTextInteractive;
import tv.hd3g.processlauncher.CapturedStdOutErrTextRetention;
import tv.hd3g.processlauncher.InvalidExecution;
import tv.hd3g.processlauncher.LineEntry;
import tv.hd3g.processlauncher.ProcesslauncherBuilder;
import tv.hd3g.processlauncher.ProcesslauncherLifecycle;

public class KeepStdoutAndErrToLogWatcher implements ExecutorWatcher {

	private final Logger log;
	private final Function<LineEntry, Level> levelMapper;
	@Getter
	private final CapturedStdOutErrTextRetention textRetention;
	protected Predicate<String> filterOutErrorLines;

	/**
	 * @param log Where to put all stdout/err events
	 * @param levelMapper How to log stdOut/err events, return the Level to log or null for discard line.
	 */
	public KeepStdoutAndErrToLogWatcher(final Logger log, final Function<LineEntry, Level> levelMapper) {
		filterOutErrorLines = p -> true;
		this.log = log;
		this.levelMapper = levelMapper;
		textRetention = new CapturedStdOutErrTextRetention();
	}

	public KeepStdoutAndErrToLogWatcher() {
		this(null, null);
	}

	@Override
	public void setFilterOutErrorLines(final Predicate<String> filterOutErrorLines) {
		this.filterOutErrorLines = requireNonNull(filterOutErrorLines, "\"filterOutErrorLines\" can't to be null");
	}

	@Override
	public void setupWatcherRun(final ProcesslauncherBuilder builder) {
		if (log == null) {
			builder.getSetCaptureStandardOutputAsOutputText(BOTH_STDOUT_STDERR).addObserver(textRetention);
		} else {
			final var capture = builder.getSetCaptureStandardOutputAsOutputText();
			capture.addObserver(new CapturedStdOutErrTextInteractive(line -> {
				final var level = levelMapper.apply(line);
				if (level != null && log.isEnabledForLevel(level)) {
					log.atLevel(level).log(line.toString());
				}
				return null;
			}));
			capture.addObserver(textRetention);
		}
	}

	@Override
	public void afterStartProcess(final ProcesslauncherLifecycle lifeCycle) {
		try {
			lifeCycle.checkExecution();
			textRetention.waitForClosedStreams();
		} catch (final InvalidExecution e) {
			throw e.injectStdErr(textRetention.getStderrLines(false)
					.filter(filterOutErrorLines)
					.map(String::trim).collect(Collectors.joining("|")));
		}
	}

}
