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
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tv.hd3g.processlauncher.cmdline.ExecutableFinder;

public class ProcesslauncherLifecycle {
	private static final String LOG_FORCE_TO_CLOSE_PROCESS = "Force to close process {}";
	private static final String LOG_CLOSE_MANUALLY_PROCESS = "Close manually process {}";

	private static Logger log = LogManager.getLogger();

	private static final Executor END_EXEC_CALLBACK;
	private static final AtomicLong END_EXEC_CALLBACK_COUNT;

	static {
		END_EXEC_CALLBACK_COUNT = new AtomicLong(0);
		END_EXEC_CALLBACK = Executors.newCachedThreadPool(r -> {
			final var t = new Thread(r);
			t.setPriority(Thread.MIN_PRIORITY);
			t.setDaemon(true);
			t.setName("EndProcessExecCallback#" + END_EXEC_CALLBACK_COUNT.getAndIncrement());
			return t;
		});
	}

	private final Processlauncher launcher;
	private final Process process;
	private final Thread shutdownHook;
	private final String fullCommandLine;
	private final long startDate;

	private volatile boolean processWasKilled;
	private volatile boolean processWasStoppedBecauseTooLongTime;
	private volatile long endDate;
	private StdInInjection stdInInjection;

	ProcesslauncherLifecycle(final Processlauncher launcher) throws IOException {
		this.launcher = launcher;
		processWasKilled = false;
		processWasStoppedBecauseTooLongTime = false;
		fullCommandLine = launcher.getFullCommandLine();

		final var pBuilder = launcher.getProcessBuilder();

		final var externalProcessStartup = launcher.getExternalProcessStartup();
		if (externalProcessStartup.isPresent()) {
			process = externalProcessStartup.get().startProcess(pBuilder);
			Objects.requireNonNull(process, "Can't manage null process");
		} else {
			process = pBuilder.start();
			log.info("Start process # {} {}", process.pid(), fullCommandLine);
		}
		startDate = System.currentTimeMillis();

		shutdownHook = new Thread(() -> {
			log.warn("Try to kill {}", this);
			killProcessTree(process);
		});
		shutdownHook.setDaemon(false);
		shutdownHook.setPriority(Thread.MAX_PRIORITY);
		shutdownHook.setName("ShutdownHook for " + toString());
		Runtime.getRuntime().addShutdownHook(shutdownHook);

		launcher.getExecutionTimeLimiter().ifPresent(etl -> etl.addTimesUp(this, process));

		final var executionCallbackers = launcher.getExecutionCallbackers();
		executionCallbackers.forEach(ec -> ec.postStartupExecution(this));

		launcher.getCaptureStandardOutput().ifPresent(cso -> {
			cso.stdOutStreamConsumer(process.getInputStream(), this);
			cso.stdErrStreamConsumer(process.getErrorStream(), this);
		});

		process.onExit().thenRunAsync(() -> {
			final var pName = getExecNameWithoutExt();
			final var pid = getPID().map(p -> "#" + p).orElse("");
			final var status = getEndStatus().toString().toLowerCase();
			var retnr = "";
			if (isCorrectlyDone() == false) {
				retnr = " return " + getExitCode();
			}

			var dur = "";
			if (getUptime(TimeUnit.SECONDS) == 0) {
				final var msec = getCPUDuration(TimeUnit.MILLISECONDS);
				if (msec == 0) {
					dur = "";
				} else {
					dur = " in " + msec + " msec";
				}
			} else {
				dur = " in " + getUptime(TimeUnit.SECONDS) + " sec";
			}
			log.info("End exec process {}{} {}{}{}", pName, pid, status, retnr, dur);

			endDate = System.currentTimeMillis();
			Runtime.getRuntime().removeShutdownHook(shutdownHook);
			externalProcessStartup.ifPresent(eps -> eps.onEndProcess(this));
			executionCallbackers.forEach(ec -> ec.onEndExecution(this));
		}, END_EXEC_CALLBACK);
	}

	public String getExecNameWithoutExt() {
		final var execName = launcher.getExecutableName();
		if (ExecutableFinder.WINDOWS_EXEC_EXTENSIONS.stream()
		        .anyMatch(ext -> execName.toLowerCase().endsWith(ext.toLowerCase()))) {
			return execName.substring(0, execName.length() - 4);
		} else {
			return execName;
		}
	}

	public long getStartDate() {
		return getProcess().info().startInstant().flatMap(i -> Optional.ofNullable(i.toEpochMilli())).orElse(startDate);
	}

	@Override
	public String toString() {
		if (process.isAlive()) {
			return "Process" + getPID().map(pid -> " #" + pid).orElse("") + " " + fullCommandLine + " ; since "
			       + getUptime(TimeUnit.SECONDS) + " sec";
		} else {
			return "Exec " + getEndStatus() + " " + fullCommandLine;
		}
	}

	private static String processHandleToString(final ProcessHandle processHandle, final boolean verbose) {
		if (verbose) {
			return processHandle.info().command().orElse("<?>") + " #" + processHandle.pid() + " by " + processHandle
			        .info().user().orElse("<?>") + " since " + processHandle.info().totalCpuDuration().orElse(
			                Duration.ZERO).getSeconds() + " sec";
		} else {
			return processHandle.info().commandLine().orElse("<?>") + " #" + processHandle.pid();
		}
	}

	/**
	 * Blocking
	 */
	private void killProcessTree(final Process process) {
		if (process.isAlive() == false) {
			return;
		}

		log.debug("Internal kill {}", this);
		final var cantKill = process.descendants().filter(ProcessHandle::isAlive)
		        .filter(processHandle -> {
			        if (log.isDebugEnabled()) {
				        log.info(LOG_CLOSE_MANUALLY_PROCESS, () -> processHandleToString(processHandle, true));
			        } else if (log.isInfoEnabled()) {
				        log.info(LOG_CLOSE_MANUALLY_PROCESS, () -> processHandleToString(processHandle, false));
			        }
			        return processHandle.destroy() == false;
		        }).filter(processHandle -> {
			        if (log.isDebugEnabled()) {
				        log.info(LOG_FORCE_TO_CLOSE_PROCESS, () -> processHandleToString(processHandle, true));
			        } else if (log.isInfoEnabled()) {
				        log.info(LOG_FORCE_TO_CLOSE_PROCESS, () -> processHandleToString(processHandle, false));
			        }
			        return processHandle.destroyForcibly() == false;
		        }).collect(Collectors.toUnmodifiableList());

		if (process.isAlive()) {
			log.info(LOG_CLOSE_MANUALLY_PROCESS, () -> processHandleToString(process.toHandle(), true));
			if (process.toHandle().destroy() == false) {
				log.info(LOG_FORCE_TO_CLOSE_PROCESS, () -> processHandleToString(process.toHandle(), true));
				if (process.toHandle().destroyForcibly() == false) {
					throw new ProcessLifeCycleException("Can't close process "
					                                    + processHandleToString(process.toHandle(), true));
				}
			}
		}
		if (cantKill.isEmpty() == false) {
			cantKill.forEach(processHandle -> log.error("Can't force close process {}",
			        () -> processHandleToString(processHandle, true)));
			throw new ProcessLifeCycleException("Can't close process " + toString() + " for PID "
			                                    + cantKill.stream()
			                                            .map(ProcessHandle::pid)
			                                            .map(String::valueOf)
			                                            .collect(Collectors.joining(", ")));
		}
	}

	public Processlauncher getLauncher() {
		return launcher;
	}

	public Process getProcess() {
		return process;
	}

	public EndStatus getEndStatus() {
		if (process.isAlive()) {
			return EndStatus.NOT_YET_DONE;
		} else if (processWasKilled) {
			return EndStatus.KILLED;
		} else if (processWasStoppedBecauseTooLongTime) {
			return EndStatus.TOO_LONG_EXECUTION_TIME;
		} else if (launcher.isExecCodeMustBeZero() && process.exitValue() != 0) {
			return EndStatus.DONE_WITH_ERROR;
		}
		return EndStatus.CORRECTLY_DONE;
	}

	public long getEndDate() {
		return endDate;
	}

	public long getUptime(final TimeUnit unit) {
		if (endDate > 0L) {
			return unit.convert(endDate - getStartDate(), TimeUnit.MILLISECONDS);
		}
		return unit.convert(System.currentTimeMillis() - getStartDate(), TimeUnit.MILLISECONDS);
	}

	public long getCPUDuration(final TimeUnit unit) {
		return unit.convert(process.info().totalCpuDuration().orElse(Duration.ZERO).toMillis(), TimeUnit.MILLISECONDS);
	}

	public boolean isKilled() {
		return processWasKilled;
	}

	public boolean isTooLongTime() {
		return processWasStoppedBecauseTooLongTime;
	}

	ProcesslauncherLifecycle runningTakesTooLongTimeStopIt() {
		processWasStoppedBecauseTooLongTime = true;
		killProcessTree(process);
		return this;
	}

	public ProcesslauncherLifecycle kill() {
		if (process.isAlive() == false) {
			return this;
		}
		processWasKilled = true;
		killProcessTree(process);
		return this;
	}

	public ProcesslauncherLifecycle waitForEnd() {
		try {
			process.waitFor();
			while (process.isAlive()) {
				Thread.onSpinWait();
			}
		} catch (final InterruptedException e) {// NOSONAR
			throw new ProcessLifeCycleException("Can't wait the end of " + fullCommandLine, e);
		}
		return this;
	}

	public ProcesslauncherLifecycle waitForEnd(final long timeout, final TimeUnit unit) {
		try {
			process.waitFor(timeout, unit);
		} catch (final InterruptedException e) {// NOSONAR
			throw new ProcessLifeCycleException("Can't wait the end of " + fullCommandLine, e);
		}
		return this;
	}

	/**
	 * waitForEnd and checks isCorrectlyDone
	 */
	public ProcesslauncherLifecycle checkExecution() {
		waitForEnd();
		if (isCorrectlyDone() == false) {
			throw new InvalidExecution(this);
		}
		return this;
	}

	public synchronized StdInInjection getStdInInjection() {
		if (stdInInjection == null) {
			stdInInjection = new StdInInjection(process.getOutputStream());
		}
		return stdInInjection;
	}

	String getFullCommandLine() {
		return fullCommandLine;
	}

	public boolean isCorrectlyDone() {
		return getEndStatus().equals(EndStatus.CORRECTLY_DONE);
	}

	/**
	 * Blocking call until process is really done.
	 * Correct: https://github.com/hdsdi3g/processlauncher/issues/1
	 */
	public Integer getExitCode() {
		while (getProcess().isAlive()) {
			Thread.onSpinWait();
		}
		while (true) {
			try {
				return getProcess().exitValue();
			} catch (final IllegalThreadStateException e) {
				if (e.getMessage().equalsIgnoreCase("process has not exited") == false) {
					throw e;
				}
			}
			Thread.onSpinWait();
		}
	}

	/**
	 * on Windows, return like "HOST_or_DOMAIN"\"username"
	 */
	public Optional<String> getUserExec() {
		return getProcess().info().user();
	}

	public Optional<Long> getPID() {
		try {
			return Optional.ofNullable(getProcess().pid());
		} catch (final UnsupportedOperationException e) {
			return Optional.empty();
		}
	}

	public Boolean isRunning() {
		return getProcess().isAlive();
	}
}
