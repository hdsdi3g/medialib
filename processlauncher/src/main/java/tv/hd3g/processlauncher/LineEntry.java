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

public class LineEntry {

	private final long date;
	private final String line;
	private final boolean stdErr;
	private final ProcesslauncherLifecycle source;

	LineEntry(final long date, final String line, final boolean stdErr, final ProcesslauncherLifecycle source) {
		this.line = line;
		this.stdErr = stdErr;
		this.source = source;
		this.date = date;
	}

	public long getTimeAgo() {
		return date - source.getStartDate();
	}

	public long getDate() {
		return date;
	}

	public String getLine() {
		return line;
	}

	public ProcesslauncherLifecycle getSource() {
		return source;
	}

	public boolean isStdErr() {
		return stdErr;
	}

	boolean canUseThis(final CapturedStreams choosedStream) {
		return stdErr && choosedStream.canCaptureStderr() || stdErr == false && choosedStream.canCaptureStdout();
	}

	/**
	 * For logging purpose
	 */
	@Override
	public String toString() {
		final var execName = source.getLauncher().getExecutableName();
		if (stdErr) {
			return execName + " ❌ " + line;
		} else {
			return execName + " ✅ " + line;
		}
	}

}
