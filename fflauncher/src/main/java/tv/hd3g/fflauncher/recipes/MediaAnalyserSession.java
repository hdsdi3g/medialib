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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import lombok.extern.slf4j.Slf4j;
import tv.hd3g.fflauncher.FFmpeg;
import tv.hd3g.fflauncher.filtering.AudioFilterSupplier;
import tv.hd3g.fflauncher.filtering.FilterChains;
import tv.hd3g.fflauncher.filtering.FilterSupplier;
import tv.hd3g.fflauncher.filtering.VideoFilterSupplier;
import tv.hd3g.fflauncher.filtering.lavfimtd.LavfiMetadataFilterParser;
import tv.hd3g.fflauncher.resultparser.Ebur128StrErrFilterEvent;
import tv.hd3g.fflauncher.resultparser.RawStdErrEventParser;
import tv.hd3g.fflauncher.resultparser.RawStdErrFilterEvent;
import tv.hd3g.ffprobejaxb.FFprobeJAXB;
import tv.hd3g.processlauncher.InvalidExecution;

@Slf4j
public class MediaAnalyserSession extends BaseAnalyserSession {

	private final MediaAnalyser mediaAnalyser;
	private final List<AudioFilterSupplier> audioFilters;
	private final List<VideoFilterSupplier> videoFilters;
	private final String source;
	private final File sourceFile;

	private FFprobeJAXB ffprobeResult;
	private BiConsumer<MediaAnalyserSession, Ebur128StrErrFilterEvent> ebur128EventConsumer;
	private BiConsumer<MediaAnalyserSession, RawStdErrFilterEvent> rawStdErrEventConsumer;
	private String pgmFFDuration;
	private String pgmFFStartTime;

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

	public Optional<FFprobeJAXB> getFFprobeResult() {
		return Optional.ofNullable(ffprobeResult);
	}

	public void setEbur128EventConsumer(final BiConsumer<MediaAnalyserSession, Ebur128StrErrFilterEvent> ebur128EventConsumer) {
		this.ebur128EventConsumer = Objects.requireNonNull(ebur128EventConsumer,
				"\"ebur128EventConsumer\" can't to be null");
	}

	public void setRawStdErrEventConsumer(final BiConsumer<MediaAnalyserSession, RawStdErrFilterEvent> rawStdErrEventConsumer) {
		this.rawStdErrEventConsumer = Objects.requireNonNull(rawStdErrEventConsumer,
				"\"rawStdErrEventConsumer\" can't to be null");

	}

	public void setPgmFFDuration(final String pgmFFDuration) {
		this.pgmFFDuration = pgmFFDuration;
	}

	public void setPgmFFStartTime(final String pgmFFStartTime) {
		this.pgmFFStartTime = pgmFFStartTime;
	}

	private FFmpeg prepareFFmpeg() {
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

		if (pgmFFDuration != null && pgmFFDuration.isEmpty() == false) {
			ffmpeg.addDuration(pgmFFDuration);
		}
		if (pgmFFStartTime != null && pgmFFStartTime.isEmpty() == false) {
			ffmpeg.addStartPosition(pgmFFStartTime);
		}

		ffmpeg.fixIOParametredVars(APPEND_PARAM_AT_END, APPEND_PARAM_AT_END);
		applyMaxExecTime(ffmpeg);
		return ffmpeg;
	}

	/**
	 * @param oLavfiLinesToMerge Sometimes ffmpeg ametadata and metadata must output lines to somewhere.
	 *        One can be stdout, but not the both.
	 *        So, if a metadata output to a file, this file can be read *after* the process with the Supplier.
	 */
	public MediaAnalyserResult process(final Optional<Supplier<Stream<String>>> oLavfiLinesToMerge) {
		final var ffmpeg = prepareFFmpeg();

		final var lavfiMetadataFilterParser = new LavfiMetadataFilterParser();
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
						lavfiMetadataFilterParser.addLavfiRawLine(line);
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

		oLavfiLinesToMerge.stream()
				.flatMap(Supplier::get)
				.forEach(lavfiMetadataFilterParser::addLavfiRawLine);

		return new MediaAnalyserResult(
				lavfiMetadataFilterParser.close(),
				rawStdErrEventParser.close(),
				getFilterContextList());
	}

	public List<MediaAnalyserSessionFilterContext> getFilterContextList() {
		return Stream.concat(
				audioFilters.stream()
						.filter(f -> f.getFilterName().equals("ametadata") == false)
						.map(f -> MediaAnalyserSessionFilterContext.getFromFilter(f, "audio")),
				videoFilters.stream()
						.filter(f -> f.getFilterName().equals("metadata") == false)
						.map(f -> MediaAnalyserSessionFilterContext.getFromFilter(f, "video")))
				.distinct()
				.sorted((l, r) -> l.type().concat(l.name()).compareTo(r.type().concat(r.name())))
				.toList();
	}

	public static MediaAnalyserResult importFromOffline(final Stream<String> stdOutLines,
														final Stream<String> stdErrLines,
														final Consumer<Ebur128StrErrFilterEvent> ebur128EventConsumer,
														final Consumer<RawStdErrFilterEvent> rawStdErrEventConsumer,
														final Collection<MediaAnalyserSessionFilterContext> filters) {
		final var lavfiMetadataFilterParser = new LavfiMetadataFilterParser();
		stdOutLines.forEach(lavfiMetadataFilterParser::addLavfiRawLine);

		final var rawStdErrEventParser = new RawStdErrEventParser(event -> {
			if (event.getFilterName().equals("ebur128")) {
				ebur128EventConsumer.accept(new Ebur128StrErrFilterEvent(event.getLineValue()));
			} else {
				rawStdErrEventConsumer.accept(event);
			}
		});
		stdErrLines.forEach(rawStdErrEventParser::onLine);

		return new MediaAnalyserResult(lavfiMetadataFilterParser.close(), rawStdErrEventParser.close(), filters);
	}

	public void extract(final Consumer<String> sysOut, final Consumer<String> sysErr) {
		final var ffmpeg = prepareFFmpeg();

		final var stdErrLinesBucket = new CircularFifoQueue<String>(10);
		final var processLifecycle = ffmpeg.execute(mediaAnalyser.getExecutableFinder(),
				lineEntry -> {
					final var line = lineEntry.getLine();
					log.trace("Line: {}", line);
					if (lineEntry.isStdErr() == false) {
						sysOut.accept(line);
					} else {
						sysErr.accept(line);
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

	/**
	 * @return unmodifiable list w/o ametadata filter
	 */
	public List<AudioFilterSupplier> getAudioFilters() {
		return audioFilters.stream()
				.filter(f -> f.toFilter().getFilterName().equals("ametadata") == false)
				.toList();
	}

	/**
	 * @return unmodifiable list w/o metadata filter
	 */
	public List<VideoFilterSupplier> getVideoFilters() {
		return videoFilters.stream()
				.filter(f -> f.toFilter().getFilterName().equals("metadata") == false)
				.toList();
	}

}
