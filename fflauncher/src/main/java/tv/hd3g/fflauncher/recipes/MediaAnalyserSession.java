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

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tv.hd3g.fflauncher.filtering.AudioFilterSupplier;
import tv.hd3g.fflauncher.filtering.FilterChains;
import tv.hd3g.fflauncher.filtering.FilterSupplier;
import tv.hd3g.fflauncher.filtering.VideoFilterSupplier;
import tv.hd3g.fflauncher.resultparser.Ebur128StrErrFilterEvent;
import tv.hd3g.fflauncher.resultparser.MetadataFilterFrameParser;
import tv.hd3g.fflauncher.resultparser.RawStdErrEventParser;
import tv.hd3g.fflauncher.resultparser.RawStdErrFilterEvent;
import tv.hd3g.ffprobejaxb.FFprobeJAXB;
import tv.hd3g.processlauncher.InvalidExecution;

public class MediaAnalyserSession {
	private static Logger log = LogManager.getLogger();

	private final MediaAnalyser mediaAnalyser;
	private final List<AudioFilterSupplier> audioFilters;
	private final List<VideoFilterSupplier> videoFilters;
	private final String source;
	private final File sourceFile;

	private FFprobeJAXB ffprobeResult;
	private BiConsumer<MediaAnalyserSession, Ebur128StrErrFilterEvent> ebur128EventConsumer;
	private BiConsumer<MediaAnalyserSession, RawStdErrFilterEvent> rawStdErrEventConsumer;

	MediaAnalyserSession(final MediaAnalyser mediaAnalyser, final String source, final File sourceFile) {
		this.mediaAnalyser = mediaAnalyser;
		if (source == null && sourceFile == null) {
			throw new IllegalArgumentException("No source for ffmpeg");
		}
		this.source = source;
		this.sourceFile = sourceFile;

		audioFilters = Collections.unmodifiableList(mediaAnalyser.getAudioFilters());
		videoFilters = Collections.unmodifiableList(mediaAnalyser.getVideoFilters());

		ebur128EventConsumer = (m, event) -> log.trace("On ebur128: {} on {}", event, m);
		rawStdErrEventConsumer = (m, event) -> log.trace("On rawStd: {} on {}", event, m);
	}

	public MediaAnalyserSession setFFprobeResult(final FFprobeJAXB ffprobeResult) {
		this.ffprobeResult = ffprobeResult;
		return this;
	}

	public void setEbur128EventConsumer(final BiConsumer<MediaAnalyserSession, Ebur128StrErrFilterEvent> ebur128EventConsumer) {
		this.ebur128EventConsumer = Objects.requireNonNull(ebur128EventConsumer,
				"\"ebur128EventConsumer\" can't to be null");
	}

	public void setRawStdErrEventConsumer(final BiConsumer<MediaAnalyserSession, RawStdErrFilterEvent> rawStdErrEventConsumer) {
		this.rawStdErrEventConsumer = Objects.requireNonNull(rawStdErrEventConsumer,
				"\"rawStdErrEventConsumer\" can't to be null");

	}

	public MediaAnalyserResult process() {
		if (audioFilters.isEmpty() && videoFilters.isEmpty()) {
			throw new IllegalArgumentException("No filters are sets");
		}

		if (ffprobeResult != null) {
			if (ffprobeResult.getAudiosStreams().count() == 0 && audioFilters.isEmpty() == false) {
				throw new IllegalStateException("Can't apply audio filters if source don't have an audio stream!");
			} else if (ffprobeResult.getFirstVideoStream().isEmpty() && videoFilters.isEmpty() == false) {
				throw new IllegalStateException("Can't apply video filters if source don't have an video stream!");
			}
		}

		final var ffmpeg = mediaAnalyser.createFFmpeg();
		ffmpeg.setHidebanner();
		ffmpeg.setNostats();

		if (source != null) {
			ffmpeg.addSimpleInputSource(source);
		} else {
			ffmpeg.addSimpleInputSource(sourceFile);
		}

		if (audioFilters.isEmpty() == false) {
			final var chain = new FilterChains();
			chain.createChain()
					.addAll(audioFilters.stream()
							.map(FilterSupplier::toFilter)
							.toList());
			chain.pushFilterChainTo("-af", ffmpeg);
		} else {
			ffmpeg.setNoAudio();
		}

		if (videoFilters.isEmpty() == false) {
			final var chain = new FilterChains();
			chain.createChain()
					.addAll(videoFilters.stream()
							.map(FilterSupplier::toFilter)
							.toList());
			chain.pushFilterChainTo("-vf", ffmpeg);
		} else {
			ffmpeg.setNoVideo();
		}

		ffmpeg.addSimpleOutputDestination("-", "null");

		ffmpeg.fixIOParametredVars(APPEND_PARAM_AT_END, APPEND_PARAM_AT_END);

		final var metadataFilterFrameParser = new MetadataFilterFrameParser();
		final var rawStdErrEventParser = new RawStdErrEventParser(event -> {
			if (event.getFilterName().equals("ebur128")) {
				ebur128EventConsumer.accept(this, new Ebur128StrErrFilterEvent(event.getLineValue()));
			} else {
				rawStdErrEventConsumer.accept(this, event);
			}
		});

		final var stdErrLinesBucket = new CircularFifoQueue<String>(10);
		final var processLifecycle = ffmpeg.execute(mediaAnalyser.getExecutableFinder(),
				lineEntry -> {
					final var line = lineEntry.getLine();
					log.trace("Line: {}", line);
					if (lineEntry.isStdErr() == false) {
						metadataFilterFrameParser.onLine(line);
					} else {
						rawStdErrEventParser.onLine(line);
						stdErrLinesBucket.add(line.trim());
					}
				});

		log.debug("Start {}", processLifecycle.getLauncher().getFullCommandLine());

		processLifecycle.waitForEnd();
		final var execOk = processLifecycle.isCorrectlyDone();
		if (execOk == false) {
			final var stdErr = stdErrLinesBucket.stream().collect(Collectors.joining("|"));
			throw new InvalidExecution(processLifecycle, stdErr);
		}

		return new MediaAnalyserResult(this, metadataFilterFrameParser.close(), rawStdErrEventParser.close());
	}

	public String getSource() {
		return source;
	}

	public File getSourceFile() {
		return sourceFile;
	}

	@Override
	public String toString() {
		if (source != null) {
			return source;
		} else {
			return sourceFile.getPath();
		}
	}
}
