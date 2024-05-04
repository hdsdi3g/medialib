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
import static tv.hd3g.fflauncher.enums.FFLogLevel.WARNING;
import static tv.hd3g.fflauncher.recipes.MediaAnalyserSessionFilterContext.getFilterChains;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import lombok.extern.slf4j.Slf4j;
import tv.hd3g.fflauncher.FFmpeg;
import tv.hd3g.fflauncher.filtering.AudioFilterSupplier;
import tv.hd3g.fflauncher.filtering.Filter;
import tv.hd3g.fflauncher.filtering.FilterArgument;
import tv.hd3g.fflauncher.filtering.FilterChains;
import tv.hd3g.fflauncher.filtering.FilterSupplier;
import tv.hd3g.fflauncher.filtering.VideoFilterSupplier;
import tv.hd3g.fflauncher.filtering.lavfimtd.LavfiMetadataFilterParser;
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
	}

	public MediaAnalyserSession setFFprobeResult(final FFprobeJAXB ffprobeResult) {
		this.ffprobeResult = ffprobeResult;
		return this;
	}

	public Optional<FFprobeJAXB> getFFprobeResult() {
		return Optional.ofNullable(ffprobeResult);
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
			if (ffprobeResult.getAudioStreams().count() == 0 && audioFilters.isEmpty() == false) {
				throw new IllegalStateException("Can't apply audio filters if source don't have an audio stream!");
			} else if (ffprobeResult.getFirstVideoStream().isEmpty() && videoFilters.isEmpty() == false) {
				throw new IllegalStateException("Can't apply video filters if source don't have an video stream!");
			}
		}

		final var ffmpeg = mediaAnalyser.createFFmpeg();
		ffmpeg.setHidebanner();
		ffmpeg.setNostats();
		ffmpeg.setLogLevel(WARNING, false, true);

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

		final var stdErrLinesBucket = new CircularFifoQueue<String>(10);
		final var processLifecycle = ffmpeg.execute(mediaAnalyser.getExecutableFinder(),
				lineEntry -> {
					final var line = lineEntry.getLine();
					log.trace("Line: {}", line);
					if (lineEntry.isStdErr() == false) {
						lavfiMetadataFilterParser.addLavfiRawLine(line);
					} else {
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

		final var r128Target = FilterChains.parse("-af", ffmpeg.getInternalParameters())
				.stream()
				.map(MediaAnalyserSession::extractEbur128TargetFromAFilterChains)
				.flatMap(Optional::stream)
				.findFirst();

		return new MediaAnalyserResult(
				lavfiMetadataFilterParser.close(),
				getFilterContextList(),
				r128Target);
	}

	static Optional<Integer> extractEbur128TargetFromAFilterChains(final FilterChains fchains) {
		return fchains.getAllFiltersInChains()
				.filter(f -> f.getFilterName().equals("ebur128"))
				.map(Filter::getArguments)
				.flatMap(List::stream)
				.filter(f -> f.getKey().equals("target"))
				.map(FilterArgument::getValue)
				.map(Integer::parseInt)
				.findFirst();
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
														final Collection<MediaAnalyserSessionFilterContext> filters) {
		final var lavfiMetadataFilterParser = new LavfiMetadataFilterParser();
		stdOutLines.forEach(lavfiMetadataFilterParser::addLavfiRawLine);
		final var ebur128Target = extractEbur128TargetFromAFilterChains(getFilterChains(filters));

		return new MediaAnalyserResult(lavfiMetadataFilterParser.close(), filters, ebur128Target);
	}

	public String extract(final Consumer<String> sysOut, final Consumer<String> sysErr) {
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

		final var ffmpegCommandLine = processLifecycle.getLauncher().getFullCommandLine();
		log.debug("Start {}", ffmpegCommandLine);

		processLifecycle.waitForEnd();
		final var execOk = processLifecycle.isCorrectlyDone();
		if (execOk == false) {
			final var stdErr = stdErrLinesBucket.stream().collect(Collectors.joining("|"));
			throw new InvalidExecution(processLifecycle, stdErr);
		}
		return ffmpegCommandLine;
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
