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

import java.util.function.Predicate;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;
import tv.hd3g.processlauncher.CapturedStdOutErrTextRetention;
import tv.hd3g.processlauncher.DirectStandardOutputStdErrRetention;
import tv.hd3g.processlauncher.InputStreamConsumer;
import tv.hd3g.processlauncher.InvalidExecution;
import tv.hd3g.processlauncher.ProcesslauncherBuilder;
import tv.hd3g.processlauncher.ProcesslauncherLifecycle;

public class DirectStdoutGetStderrWatcher implements ExecutorWatcher {

	@Setter
	private InputStreamConsumer stdOutConsumer;
	@Getter
	private final CapturedStdOutErrTextRetention stdErrtextRetention;
	protected Predicate<String> filterOutErrorLines;

	public DirectStdoutGetStderrWatcher() {
		filterOutErrorLines = p -> true;
		stdErrtextRetention = new CapturedStdOutErrTextRetention();
	}

	@Override
	public void setFilterOutErrorLines(final Predicate<String> filterOutErrorLines) {
		this.filterOutErrorLines = requireNonNull(filterOutErrorLines, "\"filterOutErrorLines\" can't to be null");
	}

	@Override
	public void setupWatcherRun(final ProcesslauncherBuilder builder) {
		final var directStreams = new DirectStandardOutputStdErrRetention(stdErrtextRetention, stdOutConsumer);
		builder.setCaptureStandardOutput(directStreams);
	}

	@Override
	public void afterStartProcess(final ProcesslauncherLifecycle lifeCycle) {
		try {
			lifeCycle.checkExecution();
		} catch (final InvalidExecution e) {
			throw e.injectStdErr(stdErrtextRetention.getStderrLines(false)
					.filter(filterOutErrorLines)
					.map(String::trim).collect(Collectors.joining("|")));
		}
	}
}
