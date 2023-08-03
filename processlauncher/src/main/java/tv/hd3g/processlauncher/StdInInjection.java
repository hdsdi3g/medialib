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
 * Copyright (C) hdsdi3g for hd3g.tv 2018
 *
 */
package tv.hd3g.processlauncher;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StdInInjection extends OutputStream {

	public static final String LINESEPARATOR = System.getProperty("line.separator");

	private final OutputStream stdIn;

	public StdInInjection(final OutputStream stdIn) {
		this.stdIn = stdIn;
	}

	@Override
	public void flush() throws IOException {
		stdIn.flush();
	}

	@Override
	public void close() throws IOException {
		stdIn.close();
	}

	@Override
	public void write(final int b) throws IOException {
		stdIn.write(b);
	}

	@Override
	public void write(final byte[] b, final int off, final int len) throws IOException {
		stdIn.write(b, off, len);
	}

	/**
	 * Send text + new line + flush
	 */
	public StdInInjection println(final String text) throws IOException {
		println(text, Charset.defaultCharset());
		return this;
	}

	/**
	 * Send text + new line + flush
	 */
	public StdInInjection println(final String text, final Charset destCharset) throws IOException {
		if (log.isTraceEnabled()) {
			log.trace("Println: \"{}\"", text);
		}
		write(text.getBytes(destCharset));
		write(LINESEPARATOR.getBytes(destCharset));
		flush();
		return this;
	}

}
