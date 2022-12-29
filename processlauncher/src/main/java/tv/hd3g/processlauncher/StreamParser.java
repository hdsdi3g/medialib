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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StreamParser extends Thread {
	private static final Logger log = LogManager.getLogger();
	static final AtomicLong CREATED_THREAD_COUNTER = new AtomicLong(-1);

	private final InputStream processStream;
	private final boolean isStdErr;
	private final ProcesslauncherLifecycle source;
	private final List<CapturedStdOutErrText> observers;

	StreamParser(final InputStream processStream,
				 final boolean isStdErr,
				 final ProcesslauncherLifecycle source,
				 final List<CapturedStdOutErrText> observers) {
		this.processStream = processStream;
		this.isStdErr = isStdErr;
		this.source = source;
		this.observers = Collections.unmodifiableList(observers);

		setDaemon(true);
		setPriority(MAX_PRIORITY);

		final var execName = source.getLauncher().getExecutableName();
		if (isStdErr) {
			setName("Executable syserr watcher for " + execName + " TId#"
					+ CREATED_THREAD_COUNTER.incrementAndGet());
		} else {
			setName("Executable sysout watcher for " + execName + " TId#"
					+ CREATED_THREAD_COUNTER.incrementAndGet());
		}
	}

	@Override
	public void run() {
		try {
			final var reader = new BufferedReader(new InputStreamReader(processStream));
			subRun(reader);
		} catch (final IOException ioe) {
			log.error("Trouble opening process streams: {}", this, ioe);
		}
	}

	private void subRun(final BufferedReader reader) throws IOException {
		try {
			var line = "";
			while ((line = reader.readLine()) != null) {
				final var lineEntry = new LineEntry(System.currentTimeMillis(), line, isStdErr,
						source);
				observers.forEach(observer -> {
					try {
						observer.onText(lineEntry);
					} catch (final RuntimeException e) {
						log.error("Can't callback process text event ", e);
					}
				});
			}
		} catch (final IOException ioe) {
			if (ioe.getMessage().equalsIgnoreCase("Bad file descriptor")) {
				if (log.isTraceEnabled()) {
					log.trace("Bad file descriptor, {}", this);
				}
			} else if (ioe.getMessage().equalsIgnoreCase("Stream closed")) {
				if (log.isTraceEnabled()) {
					log.trace("Stream closed, {}", this);
				}
			} else {
				throw ioe;
			}
		} catch (final Exception e) {
			log.error("Trouble during process {}", this, e);
		} finally {
			reader.close();
		}
	}

	public ProcesslauncherLifecycle getSource() {
		return source;
	}

	public boolean isStdErr() {
		return isStdErr;
	}
}
