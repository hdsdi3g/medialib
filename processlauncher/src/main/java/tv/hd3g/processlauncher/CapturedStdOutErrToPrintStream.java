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

import java.io.PrintStream;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

public class CapturedStdOutErrToPrintStream extends CapturedStdOutErrText {

	private final PrintStream printStreamStdOut;
	private final PrintStream printStreamStdErr;
	private Optional<Predicate<LineEntry>> filter;

	public CapturedStdOutErrToPrintStream(final PrintStream printStreamStdOut, final PrintStream printStreamStdErr) {
		this.printStreamStdOut = Objects.requireNonNull(printStreamStdOut, "\"printStreamStdOut\" can't to be null");
		this.printStreamStdErr = Objects.requireNonNull(printStreamStdErr, "\"printStreamStdErr\" can't to be null");
		filter = Optional.empty();
	}

	public Optional<Predicate<LineEntry>> getFilter() {
		return filter;
	}

	public CapturedStdOutErrToPrintStream setFilter(final Predicate<LineEntry> filter) {
		this.filter = Optional.ofNullable(filter);
		return this;
	}

	static final String STDOUT_SEPARATOR = "\t> ";
	static final String STDERR_SEPARATOR = "\t! ";

	@Override
	void onText(final LineEntry lineEntry) {
		if (filter.map(f -> f.test(lineEntry)).orElse(true) == false) {
			return;
		}

		final PrintStream out;
		if (lineEntry.isStdErr()) {
			out = printStreamStdErr;
		} else {
			out = printStreamStdOut;
		}

		final var source = lineEntry.getSource();
		out.print(source.getExecNameWithoutExt());
		out.print(source.getPID().map(pid -> "#" + pid).orElse(""));

		if (lineEntry.isStdErr()) {
			out.print(STDERR_SEPARATOR);
		} else {
			out.print(STDOUT_SEPARATOR);
		}

		out.println(lineEntry.getLine());
		out.flush();
	}

}
