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

import static java.lang.Math.floor;
import static java.lang.Thread.currentThread;
import static java.lang.Thread.State.NEW;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import tv.hd3g.fflauncher.ffprobecontainer.FFprobeResultSAX;
import tv.hd3g.fflauncher.recipes.ContainerAnalyserBase;

public record FFprobeXMLProgressWatcher(Duration programDuration,
										ThreadFactory threadFactory,
										Consumer<ContainerAnalyserBase<?, ?>> onStartCallback,
										Consumer<FFprobeXMLProgressEvent> progressCallback,
										Consumer<ContainerAnalyserBase<?, ?>> onEndCallback) {

	public static record FFprobeXMLProgressEvent(double progress, float speed, Object session) {
	}

	public <T extends ContainerAnalyserBase<?, ?>> ProgressConsumer createProgress(final T session) {
		if (programDuration.isZero() || programDuration.isNegative()) {
			return t -> {
			};
		}

		return new Progress(session);
	}

	public interface ProgressConsumer extends Consumer<String> {

		default void waitForEnd() {
		}

	}

	private class Progress extends DefaultHandler implements ProgressConsumer, Runnable, ErrorHandler {
		private final ContainerAnalyserBase<?, ?> session;
		private final Thread worker;
		private final LinkedBlockingQueue<Integer> linesBytes;
		private final InputStream source;

		private boolean ended;
		private double lastPtsTime;
		private long startdate;
		private long lastProgressDate;
		private double durationTime;

		public Progress(final ContainerAnalyserBase<?, ?> session) {
			this.session = session;
			worker = threadFactory.newThread(this);
			linesBytes = new LinkedBlockingQueue<>();
			ended = false;
			lastPtsTime = -1;
			startdate = System.currentTimeMillis();
			lastProgressDate = -1;
			durationTime = floor(programDuration.toMillis() / 1000f);
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
				onStartCallback.accept(session);
			} else if (qName.equals("packet") || qName.equals("frame")) {
				final var sPtsTime = attributes.getValue("pts_time");
				if (sPtsTime == null) {
					return;
				}
				final var ptsTime = Float.parseFloat(sPtsTime);
				if (ptsTime > lastPtsTime) {
					lastPtsTime = ptsTime;
					final var progress = lastPtsTime / durationTime;
					if (progress > 1d || durationTime - lastPtsTime < 1d) {
						return;
					}

					if (lastProgressDate <= startdate) {
						progressCallback.accept(new FFprobeXMLProgressEvent(progress, 1f, session));
						lastProgressDate = System.currentTimeMillis();
						return;
					} else if (System.currentTimeMillis() - lastProgressDate < 500) {
						return;
					}

					final var speed = (System.currentTimeMillis() - startdate) / lastPtsTime * 1000f;
					lastProgressDate = System.currentTimeMillis();
					progressCallback.accept(new FFprobeXMLProgressEvent(progress, (float) speed, session));
				}
			}
		}

		@Override
		public void endDocument() throws SAXException {
			if (ended) {
				return;
			}
			ended = true;
			onEndCallback.accept(session);
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

}
