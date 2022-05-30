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
package tv.hd3g.processlauncher;

import java.util.Optional;

import tv.hd3g.processlauncher.CaptureStandardOutputText.StreamParser;

public abstract class CapturedStdOutErrText {

	private StreamParser watchThreadStdout;
	private StreamParser watchThreadStderr;

	void setWatchThreadStderr(final StreamParser watchThreadStderr) {
		this.watchThreadStderr = watchThreadStderr;
	}

	void setWatchThreadStdout(final StreamParser watchThreadStdout) {
		this.watchThreadStdout = watchThreadStdout;
	}

	public boolean isStreamsWatchIsStillAlive() {
		return Optional.ofNullable(watchThreadStdout).map(Thread::isAlive).orElse(false) ||
		       Optional.ofNullable(watchThreadStderr).map(Thread::isAlive).orElse(false);
	}

	/**
	 * Blocking
	 */
	public void waitForClosedStreams() {
		try {
			if (watchThreadStdout != null) {
				watchThreadStdout.join();
			}
			if (watchThreadStderr != null) {
				watchThreadStderr.join();
			}
		} catch (final InterruptedException e) {// NOSONAR
			throw new IllegalStateException("Can't wait for join Threads", e);
		}
	}

	/**
	 * Blocking
	 */
	public void waitForClosedStreams(final long millis) {
		try {
			if (watchThreadStdout != null) {
				watchThreadStdout.join(millis);
			}
			if (watchThreadStderr != null) {
				watchThreadStderr.join(millis);
			}
		} catch (final InterruptedException e) {// NOSONAR
			throw new IllegalStateException("Can't wait for join Threads", e);
		}
	}

	abstract void onText(LineEntry lineEntry);

}
