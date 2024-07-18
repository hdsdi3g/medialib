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

import static java.util.Collections.synchronizedList;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.collections4.ListUtils.predicatedList;
import static org.apache.commons.collections4.PredicateUtils.notNullPredicate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import tv.hd3g.processlauncher.ProcessLifeCycleException;
import tv.hd3g.processlauncher.ProcesslauncherBuilder;
import tv.hd3g.processlauncher.ProcesslauncherLifecycle;
import tv.hd3g.processlauncher.cmdline.CommandLine;
import tv.hd3g.processlauncher.cmdline.ExecutableFinder;
import tv.hd3g.processlauncher.cmdline.Parameters;

/**
 * O -> Builder -> Result<T> -> T
 * @param <O> Origin source type
 * @param <P> Setup (command line parameters)
 * @param <T> type handled by result (final produced type)
 * @param <W> Process watcher
 */
@Slf4j
public abstract class ProcessingToolBuilder<O, P extends ParametersProvider, T, W extends ExecutorWatcher> {

	protected final String execName;
	@Getter
	protected final List<ProcessingToolCallback> callbacks;
	protected final W executorWatcher;

	@Setter
	protected ExecutableFinder executableFinder;
	protected File workingDirectory;
	protected Duration maxExecTime;
	protected ScheduledExecutorService maxExecTimeScheduler;

	protected ProcessingToolBuilder(final String execName,
									final W executorWatcher) {
		this.execName = requireNonNull(execName, "\"execName\" can't to be null");
		this.executorWatcher = requireNonNull(executorWatcher, "\"executorWatcher\" can't to be null");
		callbacks = synchronizedList(predicatedList(new ArrayList<>(), notNullPredicate()));
	}

	protected abstract P getParametersProvider(O sourceOrigin);

	protected abstract T compute(O sourceOrigin, ProcesslauncherLifecycle lifeCycle);

	public ProcessingToolResult<O, P, T, W> process(final O sourceOrigin) {
		try {
			final var lifeCycle = run(sourceOrigin);

			return new ProcessingToolResult<>(this,
					lifeCycle.getFullCommandLine(),
					compute(sourceOrigin, lifeCycle));
		} catch (final IOException e) {
			throw new ProcessLifeCycleException("Can't start " + execName, e);
		}
	}

	protected ProcesslauncherLifecycle run(final O sourceOrigin) throws IOException {
		final var parametersProvider = getParametersProvider(sourceOrigin);
		final var fullCallbackList = callbacks.stream().toList();

		final var parameters = parametersProvider.getReadyToRunParameters();
		fullCallbackList.forEach(c -> c.prepareParameters(parameters));

		final var cmd = createCommandLine(parameters);

		final var pBuilder = createProcesslauncherBuilder(cmd);
		fullCallbackList.forEach(c -> c.beforeRun(pBuilder));

		if (maxExecTimeScheduler != null) {
			pBuilder.setExecutionTimeLimiter(maxExecTime, maxExecTimeScheduler);
		}

		if (workingDirectory != null) {
			try {
				pBuilder.setWorkingDirectory(workingDirectory);
			} catch (final IOException e) {
				throw new UncheckedIOException("Invalid workingDirectory", e);
			}
		}

		executorWatcher.setupWatcherRun(pBuilder);
		fullCallbackList.forEach(pBuilder::addExecutionCallbacker);

		final var lifeCycle = pBuilder.start();

		log.debug("Start {}", lifeCycle.getFullCommandLine());
		executorWatcher.afterStartProcess(lifeCycle);

		return lifeCycle;
	}

	protected CommandLine createCommandLine(final Parameters parameters) throws IOException {
		return new CommandLine(execName, parameters,
				Optional.ofNullable(executableFinder).orElseGet(ExecutableFinder::new));
	}

	protected ProcesslauncherBuilder createProcesslauncherBuilder(final CommandLine commandLine) {
		return new ProcesslauncherBuilder(commandLine);
	}

	/**
	 * @return true if maxExecTime is more than one sec.
	 */
	public boolean setMaxExecutionTime(final Duration maxExecTime,
									   final ScheduledExecutorService maxExecTimeScheduler) {
		if (maxExecTime.getSeconds() <= 0) {
			return false;
		}
		this.maxExecTime = maxExecTime;
		this.maxExecTimeScheduler = requireNonNull(maxExecTimeScheduler, "\"maxExecTimeScheduler\" can't to be null");
		return true;
	}

	public void setWorkingDirectory(final File workingDirectory) throws IOException {
		requireNonNull(workingDirectory, "\"workingDirectory\" can't to be null");

		if (workingDirectory.exists() == false) {
			throw new FileNotFoundException("\"" + workingDirectory.getPath() + "\" in filesytem");
		} else if (workingDirectory.canRead() == false) {
			throw new IOException("Can't read workingDirectory \"" + workingDirectory.getPath() + "\"");
		} else if (workingDirectory.isDirectory() == false) {
			throw new FileNotFoundException("\"" + workingDirectory.getPath() + "\" is not a directory");
		}
		this.workingDirectory = workingDirectory;
	}

	/**
	 * Only for implementation test purpose
	 */
	public void dryRunCallbacks(final Parameters parameters,
								final ProcesslauncherBuilder pBuilder,
								final ProcesslauncherLifecycle processlauncherLifecycle) {
		callbacks.forEach(callback -> callback.prepareParameters(parameters));
		callbacks.forEach(callback -> callback.beforeRun(pBuilder));
		callbacks.forEach(callback -> callback.postStartupExecution(processlauncherLifecycle));
		callbacks.forEach(callback -> callback.onEndExecution(processlauncherLifecycle));
	}

}
