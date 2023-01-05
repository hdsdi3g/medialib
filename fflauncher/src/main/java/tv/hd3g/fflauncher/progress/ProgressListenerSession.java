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
 * Copyright (C) hdsdi3g for hd3g.tv 2022
 *
 */
package tv.hd3g.fflauncher.progress;

import static java.lang.Thread.State.TERMINATED;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Not reusable.
 */
public class ProgressListenerSession {
	public static final String LOCALHOST_IPV4 = "127.0.0.1";

	private static Logger log = LogManager.getLogger();

	private final Thread currentThread;
	private final ProgressCallback progressCallback;
	private final Duration statsPeriod;
	private final AtomicLatchReference<ServerSocket> serverSocketReference;
	private final AtomicLatchReference<Socket> clientSocketReference;

	ProgressListenerSession(final ThreadFactory threadFactory,
							final ProgressCallback progressCallback,
							final Duration statsPeriod) {
		Objects.requireNonNull(threadFactory);
		this.progressCallback = Objects.requireNonNull(progressCallback, "\"progressCallback\" can't to be null");
		this.statsPeriod = Objects.requireNonNull(statsPeriod, "\"statsPeriod\" can't to be null");
		currentThread = threadFactory.newThread(this::listen);
		serverSocketReference = new AtomicLatchReference<>();
		clientSocketReference = new AtomicLatchReference<>();
	}

	/**
	 * End listening after the unique client disconnect from it.
	 */
	public int start() {
		if (currentThread.isAlive() == false) {
			if (currentThread.getState() == TERMINATED) {
				throw new IllegalStateException("Server Thread is now terminated");
			}
			currentThread.start();
		}

		return serverSocketReference.get(1, TimeUnit.SECONDS).getLocalPort();
	}

	private void listen() {
		var localport = 0;
		try (var server = createServerSocket()) {
			final var localSocketAddress = server.getLocalSocketAddress();
			log.debug("Socket listen now to {}...", localSocketAddress);
			serverSocketReference.set(server);
			localport = server.getLocalPort();
			progressCallback.onStartProgressListener(localport);
			try (var clientSocket = server.accept()) {
				log.debug("Client (ffmpeg?) is now connected on {}, wait to receive progress datas...",
						localSocketAddress);
				clientSocketReference.set(clientSocket);
				progressCallback.onFFmpegConnection(localport);

				final var is = clientSocket.getInputStream();
				final var buffer = new byte[1024];
				int size;
				long beforeProgressDate;
				long afterProgressDate;
				while ((size = is.read(buffer, 0, buffer.length)) > 0) {
					final var rawBlockLines = new String(buffer, 0, size)
							.lines()
							.map(String::trim)
							.toList();
					log.trace("ffmpeg progress ({}) send block: {}", localSocketAddress, rawBlockLines);
					beforeProgressDate = System.currentTimeMillis();
					progressCallback.onProgress(localport, new ProgressBlock(rawBlockLines));
					afterProgressDate = System.currentTimeMillis();

					if (afterProgressDate - beforeProgressDate > statsPeriod.toMillis()) {
						log.warn(
								"The time to process the progressCallback ({} sec) take too more time than the ffmpeg's stats_period time ({} sec)",
								(afterProgressDate - beforeProgressDate) / 1000,
								statsPeriod.toSeconds());
					}

					if (rawBlockLines.isEmpty() == false
						&& rawBlockLines.get(rawBlockLines.size() - 1).equals("progress=end")) {
						log.debug("Client (ffmpeg?) has now ends it's works on {}", localSocketAddress);
						break;
					}
				}
			}
		} catch (final SocketException e) {
			if ("Connection reset".equalsIgnoreCase(e.getMessage())) {
				progressCallback.onConnectionReset(localport, e);
			} else {
				log.error("Socket error", e);
			}
		} catch (final IOException e) {
			log.error("Can't listen socket", e);
		}
		if (localport > 0) {
			progressCallback.onEndProgress(localport);
		}
	}

	protected ServerSocket createServerSocket() throws IOException {
		return new ServerSocket(0, 1, InetAddress.getByName(LOCALHOST_IPV4));
	}

	public void manualClose() {
		if (currentThread.isAlive() == false) {
			return;
		}
		log.debug("Manually close socket session...");
		try {
			serverSocketReference.get(1, TimeUnit.SECONDS, ServerSocket::close);
			clientSocketReference.get(1, TimeUnit.SECONDS, Socket::close);
		} catch (final IOException e) {
			log.error("Can't close socket session", e);
		}
	}

}
