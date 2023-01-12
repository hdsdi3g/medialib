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
 * Copyright (C) hdsdi3g for hd3g.tv 2023
 *
 */
package tv.hd3g.fflauncher.recipes;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import tv.hd3g.fflauncher.FFbase;

class BaseAnalyserSession {
	private Duration maxExecTime;
	private ScheduledExecutorService maxExecTimeScheduler;

	/**
	 * @return true if maxExecTime is more than one sec.
	 */
	public boolean setMaxExecutionTime(final Duration maxExecTime,
									   final ScheduledExecutorService maxExecTimeScheduler) {
		if (maxExecTime.getSeconds() <= 0) {
			return false;
		}
		this.maxExecTime = maxExecTime;
		this.maxExecTimeScheduler = Objects.requireNonNull(maxExecTimeScheduler,
				"\"maxExecTimeScheduler\" can't to be null");
		return true;
	}

	protected void applyMaxExecTime(final FFbase ffbase) {
		if (maxExecTimeScheduler == null) {
			return;
		}
		ffbase.setMaxExecTimeScheduler(maxExecTimeScheduler);
		ffbase.setMaxExecutionTimeForShortCommands(maxExecTime.toSeconds(), TimeUnit.SECONDS);
	}

}
