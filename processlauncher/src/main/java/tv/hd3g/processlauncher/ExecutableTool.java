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
 * Copyright (C) hdsdi3g for hd3g.tv 2019
 *
 */
package tv.hd3g.processlauncher;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import tv.hd3g.processlauncher.cmdline.CommandLine;
import tv.hd3g.processlauncher.cmdline.ExecutableFinder;
import tv.hd3g.processlauncher.cmdline.Parameters;

public interface ExecutableTool {

	Parameters getReadyToRunParameters();

	/**
	 * See by ExecutableFinder
	 */
	String getExecutableName();

	default void beforeRun(final ProcesslauncherBuilder processBuilder) {
	}

	default Consumer<ProcesslauncherBuilder> beforeExecute() {
		return p -> {
		};
	}

	/**
	 * No filter by default.
	 * @return A filter for the error capture post-process, applied on standard error outputed by process.
	 */
	default Predicate<String> filterOutErrorLines() {
		return p -> true;
	}

	/**
	 * @param executableFinder How to run executable
	 * @param log Where to put all stdout/err events
	 * @param levelMapper How to log stdOut/err events, return the Level to log or null for discard line.
	 */
	default ExecutableToolRunning execute(final ExecutableFinder executableFinder,
										  final Logger log,
										  final Function<LineEntry, Level> levelMapper) {
		final var execConsumerBuilder = beforeExecute();
		final var executableName = getExecutableName();
		try {
			final var cmd = new CommandLine(executableName, getReadyToRunParameters(), executableFinder);
			final var builder = new ProcesslauncherBuilder(cmd);

			CapturedStdOutErrTextRetention textRetention;
			if (log == null) {
				textRetention = new CapturedStdOutErrTextRetention();
				builder.getSetCaptureStandardOutputAsOutputText(CapturedStreams.BOTH_STDOUT_STDERR)
						.addObserver(textRetention);
			} else {
				final var capture = builder.getSetCaptureStandardOutputAsOutputText();
				capture.addObserver(new CapturedStdOutErrTextInteractive(line -> {
					final var level = levelMapper.apply(line);
					if (level != null && log.isEnabled(level)) {
						log.log(level, line);
					}
					return null;
				}));
				textRetention = new CapturedStdOutErrTextRetention();
				capture.addObserver(textRetention);
			}

			if (execConsumerBuilder != null) {
				execConsumerBuilder.accept(builder);
			}
			beforeRun(builder);
			return new ExecutableToolRunning(textRetention, builder.start(), this);
		} catch (final IOException e) {
			throw new ProcessLifeCycleException("Can't start " + executableName, e);
		}
	}

	default ExecutableToolRunning execute(final ExecutableFinder executableFinder) {
		return execute(executableFinder, null, null);
	}

	/**
	 * No text retention will be done here!
	 * @param executableFinder How to run executable
	 * @param stdOutErr Where to put all stdout/err events
	 */
	default ProcesslauncherLifecycle execute(final ExecutableFinder executableFinder,
											 final Consumer<LineEntry> stdOutErrConsumer) {
		final var execConsumerBuilder = beforeExecute();
		final var executableName = getExecutableName();
		try {
			final var cmd = new CommandLine(executableName, getReadyToRunParameters(), executableFinder);
			final var builder = new ProcesslauncherBuilder(cmd);
			final var capture = builder.getSetCaptureStandardOutputAsOutputText();

			capture.addObserver(new CapturedStdOutErrTextInteractive(line -> {
				stdOutErrConsumer.accept(line);
				return null;
			}));

			if (execConsumerBuilder != null) {
				execConsumerBuilder.accept(builder);
			}
			beforeRun(builder);
			return builder.start();
		} catch (final IOException e) {
			throw new ProcessLifeCycleException("Can't start " + executableName, e);
		}
	}

	/**
	 * Text retention will be done here only for stderr!
	 */
	default ExecutableToolRunning executeDirectStdout(final ExecutableFinder executableFinder,
													  final InputStreamConsumer stdOutConsumer) {
		final var execConsumerBuilder = beforeExecute();
		final var executableName = getExecutableName();
		try {
			final var cmd = new CommandLine(executableName, getReadyToRunParameters(), executableFinder);
			final var builder = new ProcesslauncherBuilder(cmd);
			final var textRetention = new CapturedStdOutErrTextRetention();
			final var directStreams = new DirectStandardOutputStdErrRetention(textRetention, stdOutConsumer);
			builder.setCaptureStandardOutput(directStreams);

			if (execConsumerBuilder != null) {
				execConsumerBuilder.accept(builder);
			}
			beforeRun(builder);
			return new ExecutableToolRunning(textRetention, builder.start(), this);
		} catch (final IOException e) {
			throw new ProcessLifeCycleException("Can't start " + executableName, e);
		}
	}

}
