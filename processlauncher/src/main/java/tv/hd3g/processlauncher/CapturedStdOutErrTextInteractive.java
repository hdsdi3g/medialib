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

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.function.Function;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CapturedStdOutErrTextInteractive extends CapturedStdOutErrText {

	private final Function<LineEntry, String> interactive;
	private final Charset destCharset;

	/**
	 * @param interactive function return null if nothing to send.
	 * @param destCharset used for injected String to byte[] in stream
	 */
	public CapturedStdOutErrTextInteractive(final Function<LineEntry, String> interactive,
											final Charset destCharset) {
		this.interactive = Objects.requireNonNull(interactive, "\"interactive\" can't to be null");
		this.destCharset = Objects.requireNonNull(destCharset, "\"destCharset\" can't to be null");
	}

	/**
	 * @param interactive function return null if nothing to send.
	 */
	public CapturedStdOutErrTextInteractive(final Function<LineEntry, String> interactive) {
		this(interactive, Charset.defaultCharset());
	}

	@Override
	public void onText(final LineEntry lineEntry) {
		final var result = interactive.apply(lineEntry);
		final var source = lineEntry.source();

		if (result != null && source.isRunning()) {
			try {
				source.getStdInInjection().println(result, destCharset);
			} catch (final IOException e) {
				log.error("Can't send some text to process", e);
			}
		}
	}

}
