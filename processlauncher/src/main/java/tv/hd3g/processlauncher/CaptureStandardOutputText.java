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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CaptureStandardOutputText implements CaptureStandardOutput {
	private static Logger log = LogManager.getLogger();

	private static final AtomicLong CREATED_THREAD_COUNTER = new AtomicLong(-1);

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
			final var t = new StreamParser(processInputStream, false, source);
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
			final var t = new StreamParser(processInputStream, true, source);
			t.start();
			synchronized (observers) {
				observers.forEach(o -> o.setWatchThreadStderr(t));
			}
			return t;
		}
		return null;
	}

	public class StreamParser extends Thread {

		private final InputStream processStream;
		private final boolean isStdErr;
		private final ProcesslauncherLifecycle source;

		private StreamParser(final InputStream processStream,
		                     final boolean isStdErr,
		                     final ProcesslauncherLifecycle source) {
			this.processStream = processStream;
			this.isStdErr = isStdErr;
			this.source = source;
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

}
