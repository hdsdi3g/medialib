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
 * Copyright (C) hdsdi3g for hd3g.tv 2022
 *
 */
package tv.hd3g.processlauncher;

import static java.lang.Thread.MAX_PRIORITY;
import static tv.hd3g.processlauncher.StreamParser.CREATED_THREAD_COUNTER;

import java.io.InputStream;
import java.util.List;

public class DirectStandardOutputStdErrRetention implements CaptureStandardOutput {

	private final CapturedStdOutErrText stdErrObserver;
	private final InputStreamConsumer stdOutConsumer;

	public DirectStandardOutputStdErrRetention(final CapturedStdOutErrText stdErrObserver,
											   final InputStreamConsumer stdOutConsumer) {
		this.stdErrObserver = stdErrObserver;
		this.stdOutConsumer = stdOutConsumer;
	}

	@Override
	public Thread stdOutStreamConsumer(final InputStream processInputStream, final ProcesslauncherLifecycle source) {
		final var t = new Thread(() -> {
			try {
				stdOutConsumer.onProcessStart(processInputStream, source);
			} finally {
				stdOutConsumer.onClose(source);
			}
		});

		final var execName = source.getLauncher().getExecutableName();
		t.setName("Executable sysout watcher for " + execName + " TId#"
				  + CREATED_THREAD_COUNTER.incrementAndGet());
		t.setDaemon(true);
		t.setPriority(MAX_PRIORITY);
		t.start();
		return t;
	}

	@Override
	public Thread stdErrStreamConsumer(final InputStream processInputStream, final ProcesslauncherLifecycle source) {
		final var t = new StreamParser(processInputStream, true, source, List.of(stdErrObserver));
		t.start();
		stdErrObserver.setWatchThreadStderr(t);
		return t;
	}
}
