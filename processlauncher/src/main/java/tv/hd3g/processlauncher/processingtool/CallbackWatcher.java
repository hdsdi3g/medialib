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

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import tv.hd3g.processlauncher.CapturedStdOutErrTextInteractive;
import tv.hd3g.processlauncher.InvalidExecution;
import tv.hd3g.processlauncher.LineEntry;
import tv.hd3g.processlauncher.ProcesslauncherBuilder;
import tv.hd3g.processlauncher.ProcesslauncherLifecycle;

@Slf4j
public class CallbackWatcher implements ExecutorWatcher {

	@Setter
	@Getter
	private Consumer<LineEntry> stdOutErrConsumer;
	protected final CircularFifoQueue<String> stdErrLinesBucket;
	protected Predicate<String> filterOutErrorLines;

	public CallbackWatcher() {
		this(10);
	}

	public CallbackWatcher(final int errorLinesQueueSize) {
		filterOutErrorLines = p -> true;
		stdErrLinesBucket = new CircularFifoQueue<>(errorLinesQueueSize);
		stdOutErrConsumer = line -> log.trace("Line: {}", line.line());
	}

	@Override
	public void setFilterOutErrorLines(final Predicate<String> filterOutErrorLines) {
		this.filterOutErrorLines = requireNonNull(filterOutErrorLines, "\"filterOutErrorLines\" can't to be null");
	}

	@Override
	public void setupWatcherRun(final ProcesslauncherBuilder builder) {
		builder.getSetCaptureStandardOutputAsOutputText()
				.addObserver(new CapturedStdOutErrTextInteractive(line -> {
					stdOutErrConsumer.accept(line);
					if (line.stdErr()
						&& line.isEmpty() == false) {
						stdErrLinesBucket.add(line.line());
					}
					return null;
				}));
	}

	@Override
	public void afterStartProcess(final ProcesslauncherLifecycle lifeCycle) {
		try {
			lifeCycle.checkExecution();
		} catch (final InvalidExecution e) {
			final var stdErr = stdErrLinesBucket.stream()
					.map(String::trim)
					.filter(filterOutErrorLines)
					.collect(Collectors.joining("|"));
			throw e.injectStdErr(stdErr);
		}
	}

}
