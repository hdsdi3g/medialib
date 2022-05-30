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
package tv.hd3g.fflauncher.recipes;

import static tv.hd3g.fflauncher.ConversionTool.APPEND_PARAM_AT_END;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tv.hd3g.fflauncher.FFprobe;
import tv.hd3g.fflauncher.FFprobe.FFPrintFormat;
import tv.hd3g.fflauncher.enums.FFLogLevel;
import tv.hd3g.ffprobejaxb.FFprobeJAXB;
import tv.hd3g.processlauncher.cmdline.ExecutableFinder;
import tv.hd3g.processlauncher.cmdline.Parameters;

public class ProbeMedia {

	private static Logger log = LogManager.getLogger();

	private final String execName;
	private final ExecutableFinder executableFinder;
	private final ScheduledExecutorService maxExecTimeScheduler;

	public ProbeMedia(final ExecutableFinder executableFinder, final ScheduledExecutorService maxExecTimeScheduler) {
		this("ffprobe", executableFinder, maxExecTimeScheduler);
	}

	public ProbeMedia(final String execName, final ExecutableFinder executableFinder,
	                  final ScheduledExecutorService maxExecTimeScheduler) {
		this.execName = Objects.requireNonNull(execName);
		this.executableFinder = Objects.requireNonNull(executableFinder);
		this.maxExecTimeScheduler = Objects.requireNonNull(maxExecTimeScheduler);
	}

	private FFprobe internal() {
		final var parameters = new Parameters();
		final var ffprobe = new FFprobe(execName, parameters);

		ffprobe.setPrintFormat(FFPrintFormat.XML).setShowStreams().setShowFormat().setShowChapters().isHidebanner();
		ffprobe.setMaxExecTimeScheduler(maxExecTimeScheduler);
		ffprobe.setLogLevel(FFLogLevel.ERROR, false, false);
		ffprobe.setFilterForLinesEventsToDisplay(l -> (l.isStdErr() && ffprobe.filterOutErrorLines().test(l
		        .getLine())));

		return ffprobe;
	}

	private FFprobeJAXB execute(final FFprobe ffprobe) {
		final var rtFFprobe = ffprobe.execute(executableFinder);
		final var textRetention = rtFFprobe.checkExecutionGetText();
		final var stdOut = textRetention.getStdout(false, System.lineSeparator());
		return new FFprobeJAXB(stdOut, warn -> log.warn(warn));
	}

	/**
	 * Stateless
	 * Get streams, format and chapters.
	 * Can throw an InvalidExecution in CompletableFuture, with stderr embedded.
	 * @see FFprobe to get cool FfprobeType parsers
	 */
	public FFprobeJAXB doAnalysing(final String source) {
		final var ffprobe = internal();
		ffprobe.addSimpleInputSource(source);
		ffprobe.fixIOParametredVars(APPEND_PARAM_AT_END, APPEND_PARAM_AT_END);
		return execute(ffprobe);
	}

	/**
	 * Stateless
	 * Get streams, format and chapters.
	 * Can throw an InvalidExecution in CompletableFuture, with stderr embedded.
	 * @see FFprobe to get cool FfprobeType parsers
	 */
	public FFprobeJAXB doAnalysing(final File source) {
		final var ffprobe = internal();
		ffprobe.addSimpleInputSource(source);
		ffprobe.fixIOParametredVars(APPEND_PARAM_AT_END, APPEND_PARAM_AT_END);
		return execute(ffprobe);
	}

}
