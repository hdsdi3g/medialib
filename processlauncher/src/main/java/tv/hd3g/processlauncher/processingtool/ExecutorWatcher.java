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

import java.util.function.Predicate;

import tv.hd3g.processlauncher.ProcesslauncherBuilder;
import tv.hd3g.processlauncher.ProcesslauncherLifecycle;

/**
 * @see CallbackWatcher
 * @see DirectStdoutGetStderrWatcher
 * @see KeepStdoutAndErrToLogWatcher
 */
public interface ExecutorWatcher {

	/**
	 * Must be non-blocking
	 */
	default void setupWatcherRun(final ProcesslauncherBuilder builder) {
	}

	/**
	 * Should be blocking
	 */
	default void afterStartProcess(final ProcesslauncherLifecycle lifeCycle) {
		lifeCycle.waitForEnd();
	}

	default void setFilterOutErrorLines(final Predicate<String> filterOutErrorLines) {
	}

}
