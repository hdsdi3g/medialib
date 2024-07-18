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

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ThreadFactory;

/**
 * Reusable
 */
public class ProgressListener {
	private final ThreadFactory threadFactory;

	public ProgressListener() {
		this(r -> {
			final var t = new Thread(r);
			t.setDaemon(true);
			t.setName("Socket progressWatcher for ffmpeg progress");
			return t;
		});
	}

	public ProgressListener(final ThreadFactory threadFactory) {
		this.threadFactory = Objects.requireNonNull(threadFactory, "\"threadFactory\" can't to be null");
	}

	public ProgressListenerSession createSession(final ProgressCallback progressCallback, final Duration statsPeriod) {
		return new ProgressListenerSession(threadFactory, progressCallback, statsPeriod);
	}

}
