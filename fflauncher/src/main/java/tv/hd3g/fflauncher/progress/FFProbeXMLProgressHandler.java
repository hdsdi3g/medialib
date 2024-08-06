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
 * Copyright (C) hdsdi3g for hd3g.tv 2024
 *
 */
package tv.hd3g.fflauncher.progress;

import static java.lang.Float.isFinite;
import static java.lang.Math.floor;
import static java.lang.Thread.currentThread;
import static java.lang.Thread.State.NEW;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import tv.hd3g.fflauncher.ffprobecontainer.FFprobeResultSAX;
import tv.hd3g.fflauncher.progress.FFprobeXMLProgressWatcher.FFprobeXMLProgressEvent;
import tv.hd3g.fflauncher.recipes.ContainerAnalyserBase;

public class FFProbeXMLProgressHandler extends DefaultHandler
									   implements FFprobeXMLProgressConsumer, Runnable, ErrorHandler {
	private final FFprobeXMLProgressWatcher watcher;
	private final ContainerAnalyserBase<?, ?> session;
	private final Thread worker;
	private final LinkedBlockingQueue<Integer> linesBytes;
	private final InputStream source;

	private boolean ended;
	private double lastTime;
	private long startdate;
	private long lastProgressDate;
	private double durationTime;

	public FFProbeXMLProgressHandler(final FFprobeXMLProgressWatcher watcher,
									 final ContainerAnalyserBase<?, ?> session,
									 final Optional<ThreadFactory> threadFactory) {
		this.watcher = Objects.requireNonNull(watcher, "\"watcher\" can't to be null");
		this.session = Objects.requireNonNull(session, "\"session\" can't to be null");

		if (threadFactory.isEmpty()) {
			worker = new Thread(this);
			worker.setDaemon(true);
			worker.setName("Watcher for ffprobe container analyser");
		} else {
			worker = threadFactory.get().newThread(this);
		}

		linesBytes = new LinkedBlockingQueue<>();
		ended = false;
		lastTime = -1;
		startdate = System.currentTimeMillis();
		lastProgressDate = -1;
		durationTime = floor(watcher.programDuration().toMillis() / 1000f);
		source = new InputStream() {

			@Override
			public int read() throws IOException {
				if (ended) {
					return -1;
				}
				try {
					return Optional.ofNullable(linesBytes.poll(1, SECONDS)).orElse(-1);
				} catch (final InterruptedException e) {
					currentThread().interrupt();
					throw new IllegalStateException(e);
				}
			}
		};
	}

	@Override
	public synchronized void accept(final String t) {
		if (ended) {
			return;
		}
		t.chars().forEach(i -> {
			try {
				linesBytes.put(i);
			} catch (final InterruptedException e) {
				currentThread().interrupt();
				throw new IllegalStateException(e);
			}
		});

		if (worker.getState() == NEW) {
			worker.start();
		}
	}

	@Override
	public void run() {
		try {
			FFprobeResultSAX.factory.newSAXParser().parse(source, this);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void startElement(final String uri,
							 final String localName,
							 final String qName,
							 final Attributes attributes) throws SAXException {
		if (qName.equals("ffprobe")) {
			startdate = System.currentTimeMillis();
			watcher.onStartCallback().accept(session);
		} else if (qName.equals("packet") || qName.equals("frame")) {
			final var sPtsTime = Optional.ofNullable(attributes.getValue("pts_time")).orElse("-1");
			final var sDtsTime = Optional.ofNullable(attributes.getValue("dts_time")).orElse("-1");
			final var tempPtsTime = Float.parseFloat(sPtsTime);
			final var tempDtsTime = Float.parseFloat(sDtsTime);
			var time = tempPtsTime;
			if (isFinite(time) == false || time < 0) {
				time = tempDtsTime;
			}
			if (isFinite(time) == false || time < 0) {
				return;
			}

			if (time > lastTime) {
				lastTime = time;
				final var progress = lastTime / durationTime;
				if (progress > 1d || durationTime - lastTime < 1d) {
					return;
				}

				if (lastProgressDate <= startdate) {
					watcher.progressCallback().accept(new FFprobeXMLProgressEvent(progress, 1f, session));
					lastProgressDate = System.currentTimeMillis();
					return;
				} else if (System.currentTimeMillis() - lastProgressDate < 500) {
					return;
				}

				lastProgressDate = System.currentTimeMillis();
				final var speed = lastTime * 1000d / (lastProgressDate - startdate);
				watcher.progressCallback().accept(new FFprobeXMLProgressEvent(progress, (float) speed, session));
			}
		}
	}

	@Override
	public void endDocument() throws SAXException {
		if (ended) {
			return;
		}
		ended = true;
		watcher.onEndCallback().accept(session);
	}

	@Override
	public void endElement(final String uri, final String localName, final String qName) throws SAXException {
		if (qName.equals("ffprobe") || qName.equals("packets_and_frames")) {
			endDocument();
		}
	}

	@Override
	public void waitForEnd() {
		while (worker.isAlive()) {
			Thread.onSpinWait();
		}
	}

}
