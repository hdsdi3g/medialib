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

public record LineEntry(long date,
						String line,
						boolean stdErr,
						ProcesslauncherLifecycle source) {

	public long getTimeAgo() {
		return date - source.getStartDate();
	}

	public boolean isEmpty() {
		return line.trim().isEmpty();
	}

	public boolean canUseThis(final CapturedStreams choosedStream) {
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

	public static LineEntry makeStdOut(final String line, final ProcesslauncherLifecycle source) {
		return new LineEntry(System.currentTimeMillis(), line, false, source);
	}

	public static LineEntry makeStdErr(final String line, final ProcesslauncherLifecycle source) {
		return new LineEntry(System.currentTimeMillis(), line, true, source);
	}

}
