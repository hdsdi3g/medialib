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
 * Copyright (C) hdsdi3g for hd3g.tv 2019
 *
 */
package tv.hd3g.processlauncher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Processlauncher {

	private final boolean execCodeMustBeZero;
	private final List<ExecutionCallbacker> executionCallbackers;
	private final Optional<ExecutionTimeLimiter> executionTimeLimiter;
	private final Optional<CaptureStandardOutput> captureStandardOutput;
	private final Optional<ExternalProcessStartup> externalProcessStartup;
	private final ProcessBuilder processBuilder;
	private final String fullCommandLine;
	private final ProcesslauncherBuilder processlauncherBuilder;
	private final String executableName;

	public Processlauncher(final ProcesslauncherBuilder processlauncherBuilder) {
		this.processlauncherBuilder = Objects.requireNonNull(processlauncherBuilder,
		        "\"processlauncherBuilder\" can't to be null");

		execCodeMustBeZero = processlauncherBuilder.isExecCodeMustBeZero();
		executionCallbackers = Collections.unmodifiableList(new ArrayList<>(processlauncherBuilder
		        .getExecutionCallbackers()));
		executionTimeLimiter = processlauncherBuilder.getExecutionTimeLimiter();
		captureStandardOutput = processlauncherBuilder.getCaptureStandardOutput();
		externalProcessStartup = processlauncherBuilder.getExternalProcessStartup();
		processBuilder = processlauncherBuilder.makeProcessBuilder();
		fullCommandLine = processlauncherBuilder.getFullCommandLine();
		executableName = processlauncherBuilder.getExecutableName();
	}

	public ProcesslauncherLifecycle start() throws IOException {
		return new ProcesslauncherLifecycle(this);
	}

	/**
	 * @return unmodifiableList
	 */
	public List<ExecutionCallbacker> getExecutionCallbackers() {
		return executionCallbackers;
	}

	public Optional<ExecutionTimeLimiter> getExecutionTimeLimiter() {
		return executionTimeLimiter;
	}

	public Optional<CaptureStandardOutput> getCaptureStandardOutput() {
		return captureStandardOutput;
	}

	public Optional<ExternalProcessStartup> getExternalProcessStartup() {
		return externalProcessStartup;
	}

	public boolean isExecCodeMustBeZero() {
		return execCodeMustBeZero;
	}

	public ProcessBuilder getProcessBuilder() {
		return processBuilder;
	}

	/**
	 * @return getFullCommandLine()
	 */
	@Override
	public String toString() {
		return fullCommandLine;
	}

	public String getFullCommandLine() {
		return fullCommandLine;
	}

	public ProcesslauncherBuilder getProcesslauncherBuilder() {
		return processlauncherBuilder;
	}

	public String getExecutableName() {
		return executableName;
	}
}
