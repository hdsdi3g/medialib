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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class CaptureStandardOutputText implements CaptureStandardOutput {
	private final CapturedStreams captureOutStreamsBehavior;
	private final List<CapturedStdOutErrText> observers;

	/**
	 * @param executorConsumer each stream parser will be executed in separate thread, ensure the capacity is sufficient for 2 threads by process.
	 */
	CaptureStandardOutputText(final CapturedStreams captureOutStreamsBehavior) {
		this.captureOutStreamsBehavior = captureOutStreamsBehavior;
		observers = new ArrayList<>();
	}

	/**
	 * @param executorConsumer each stream parser will be executed in separate thread, ensure the capacity is sufficient for 2 threads by process.
	 */
	CaptureStandardOutputText() {
		this(CapturedStreams.BOTH_STDOUT_STDERR);
	}

	/**
	 * Never add observer AFTER call stdOut/ErrStreamConsumer()
	 */
	public void addObserver(final CapturedStdOutErrText observer) {
		synchronized (observers) {
			observers.add(observer);
		}
	}

	@Override
	public StreamParser stdOutStreamConsumer(final InputStream processInputStream,
											 final ProcesslauncherLifecycle source) {
		if (captureOutStreamsBehavior.canCaptureStdout()) {
			final var t = new StreamParser(processInputStream, false, source, observers);
			t.start();
			synchronized (observers) {
				observers.forEach(o -> o.setWatchThreadStdout(t));
			}
			return t;
		}
		return null;
	}

	@Override
	public StreamParser stdErrStreamConsumer(final InputStream processInputStream,
											 final ProcesslauncherLifecycle source) {
		if (captureOutStreamsBehavior.canCaptureStderr()) {
			final var t = new StreamParser(processInputStream, true, source, observers);
			t.start();
			synchronized (observers) {
				observers.forEach(o -> o.setWatchThreadStderr(t));
			}
			return t;
		}
		return null;
	}

}
