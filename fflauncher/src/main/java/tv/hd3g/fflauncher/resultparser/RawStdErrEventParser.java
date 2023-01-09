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
package tv.hd3g.fflauncher.resultparser;

import static java.util.Objects.requireNonNull;
import static tv.hd3g.fflauncher.recipes.MediaAnalyser.splitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RawStdErrEventParser {
	private static Logger log = LogManager.getLogger();

	private final Set<String> summaryZoneHeaders = Set.of(
			"Integrated loudness",
			"I",
			"Threshold",
			"Loudness range",
			"LRA",
			"LRA low",
			"LRA high",
			"Sample peak",
			"Peak",
			"True peak");

	private final Consumer<RawStdErrFilterEvent> onRawStdErrEvent;
	/**
	 * Like [["Threshold", " 0.0 LUFS"], ["Peak", " -inf dBFS"], ["Sample peak"]]...
	 */
	private List<List<String>> rawSummaryZone;
	private boolean inEbur128SummaryZone;
	private Ebur128Summary ebur128Summary;

	public RawStdErrEventParser(final Consumer<RawStdErrFilterEvent> onRawStdErrEvent) {
		this.onRawStdErrEvent = requireNonNull(onRawStdErrEvent, "\"onRawStdErrEvent\" can't to be null");
		inEbur128SummaryZone = false;
		rawSummaryZone = new ArrayList<>();
	}

	public void onLine(final String rLine) {
		final var line = rLine.trim();
		if (line.isEmpty()) {
			return;
		}

		log.trace("RawStdErrEvent line: {}", line);

		if (line.startsWith("[Parsed_") && line.contains(" @ ") && line.contains("]")) {
			/**
			 * [Parsed_XXXXXXX_0 @ 0000000000000] Something
			 */
			if (inEbur128SummaryZone) {
				/**
				 * Only if ebur128 summary zone as other lines after
				 */
				closeSummaryZone();
				inEbur128SummaryZone = false;
			}

			if (line.startsWith("[Parsed_ebur128_") && line.endsWith("] Summary:")) {
				/**
				 * [Parsed_ebur128_0 @ 55c6a78b3c80] Summary:
				 */
					inEbur128SummaryZone = true;
				} else {
					onRawStdErrEvent.accept(new RawStdErrFilterEvent(line));
				}
		} else if (inEbur128SummaryZone) {
			final var entry = splitter(line, ':');
			if (summaryZoneHeaders.contains(entry.get(0))) {
				rawSummaryZone.add(entry);
			} else {
				closeSummaryZone();
				inEbur128SummaryZone = false;
			}
		}
	}

	private void closeSummaryZone() {
		if (inEbur128SummaryZone) {
			ebur128Summary = new Ebur128Summary();
			ebur128Summary.setRawLines(rawSummaryZone);
		}
		rawSummaryZone = new ArrayList<>();
	}

	/**
	 * @return can be null
	 */
	public Ebur128Summary close() {
		if (inEbur128SummaryZone) {
			closeSummaryZone();
		}
		return ebur128Summary;
	}

}
