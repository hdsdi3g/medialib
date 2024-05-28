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
package tv.hd3g.fflauncher.recipes;

import static tv.hd3g.fflauncher.ConversionTool.APPEND_PARAM_AT_END;
import static tv.hd3g.fflauncher.FFprobe.FFPrintFormat.XML;

import java.io.File;
import java.io.InputStream;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tv.hd3g.fflauncher.FFprobe;
import tv.hd3g.fflauncher.ffprobecontainer.FFprobeResultSAX;
import tv.hd3g.fflauncher.progress.FFprobeXMLProgressWatcher;
import tv.hd3g.processlauncher.InvalidExecution;

@Getter
@Slf4j
public class ContainerAnalyserSession extends BaseAnalyserSession {

	private final ContainerAnalyser containerAnalyser;
	private final String source;
	private final File sourceFile;
	private final FFprobeXMLProgressWatcher ffprobeXMLProgressWatcher;

	ContainerAnalyserSession(final ContainerAnalyser containerAnalyser,
							 final String source,
							 final File sourceFile,
							 final FFprobeXMLProgressWatcher ffprobeXMLProgressWatcher) {
		this.containerAnalyser = containerAnalyser;
		if (source == null && sourceFile == null) {
			throw new IllegalArgumentException("No source for ffmpeg");
		}
		this.source = source;
		this.sourceFile = sourceFile;
		this.ffprobeXMLProgressWatcher = ffprobeXMLProgressWatcher;
	}

	private FFprobe prepareFFprobe() {
		final var ffprobe = containerAnalyser.createFFprobe();
		ffprobe.setHidebanner();
		ffprobe.setShowFrames();
		ffprobe.setShowPackets();
		ffprobe.setPrintFormat(XML);

		if (source != null) {
			ffprobe.addSimpleInputSource(source);
		} else {
			ffprobe.addSimpleInputSource(sourceFile);
		}
		ffprobe.fixIOParametredVars(APPEND_PARAM_AT_END, APPEND_PARAM_AT_END);
		applyMaxExecTime(ffprobe);
		return ffprobe;
	}

	public ContainerAnalyserResult process() {
		final var ffprobe = prepareFFprobe();

		final var parser = new FFprobeResultSAX();
		final var runTool = ffprobe.executeDirectStdout(containerAnalyser.getExecutableFinder(), parser);
		final var ffprobeCommandLine = runTool.getLifecyle().getLauncher().getFullCommandLine();
		log.debug("Start {}", ffprobeCommandLine);
		runTool.waitForEndAndCheckExecution();

		return parser.getResult(this, ffprobeCommandLine);
	}

	public static ContainerAnalyserResult importFromOffline(final InputStream ffprobeStdOut,
															final String ffprobeCommandLine) {
		final var parser = new FFprobeResultSAX();
		parser.onProcessStart(ffprobeStdOut, null);
		parser.onClose(null);
		return parser.getResult(null, ffprobeCommandLine);
	}

	public String extract(final Consumer<String> sysOut) {
		final var ffprobe = prepareFFprobe();

		final var onLineToProgress = ffprobeXMLProgressWatcher.createProgress(this);
		final var stdErrLinesBucket = new CircularFifoQueue<String>(10);
		final var processLifecycle = ffprobe.execute(containerAnalyser.getExecutableFinder(),
				lineEntry -> {
					final var line = lineEntry.getLine();
					log.trace("Line: {}", line);
					if (lineEntry.isStdErr() == false) {
						sysOut.accept(line);
						onLineToProgress.accept(line);
					} else {
						stdErrLinesBucket.add(line.trim());
					}
				});
		final var fullCommandLine = processLifecycle.getLauncher().getFullCommandLine();
		log.debug("Start {}", fullCommandLine);

		processLifecycle.waitForEnd();
		final var execOk = processLifecycle.isCorrectlyDone();
		if (execOk == false) {
			final var stdErr = stdErrLinesBucket.stream().collect(Collectors.joining("|"));
			throw new InvalidExecution(processLifecycle, stdErr);
		}
		return fullCommandLine;
	}

}
