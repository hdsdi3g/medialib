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
package tv.hd3g.fflauncher.about;

import static java.lang.Long.parseLong;
import static java.time.Duration.ofSeconds;
import static tv.hd3g.processlauncher.cmdline.Parameters.bulk;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;
import tv.hd3g.fflauncher.FFbase;
import tv.hd3g.processlauncher.CapturedStdOutErrTextRetention;
import tv.hd3g.processlauncher.InvalidExecution;
import tv.hd3g.processlauncher.ProcesslauncherLifecycle;
import tv.hd3g.processlauncher.cmdline.ExecutableFinder;
import tv.hd3g.processlauncher.processingtool.KeepStdoutAndErrToLogWatcher;
import tv.hd3g.processlauncher.processingtool.ProcessingToolBuilder;

/**
 * Threadsafe
 * Sync (blocking) during executions
 */
@Slf4j
public class FFAbout {

	private static final String CUVID = "cuvid";

	private final String execName;
	private final ExecutableFinder executableFinder;
	private final ScheduledExecutorService maxExecTimeScheduler;

	public FFAbout(final String execName,
				   final ExecutableFinder executableFinder,
				   final ScheduledExecutorService maxExecTimeScheduler) {
		this.execName = Objects.requireNonNull(execName, "\"execName\" can't to be null");
		this.executableFinder = Objects.requireNonNull(executableFinder, "\"executableFinder\" can't to be null");
		this.maxExecTimeScheduler = Objects.requireNonNull(maxExecTimeScheduler,
				"\"maxExecTimeScheduler\" can't to be null");
	}

	private CapturedStdOutErrTextRetention internalRun(final String bulkParameters) {
		try {
			final var builder = new ExecBuilder(new FFbase(execName, bulk(bulkParameters)));
			builder.setExecutableFinder(executableFinder);
			builder.setMaxExecutionTime(ofSeconds(
					parseLong(System.getProperty("fflauncher.about.maxExecTime", "5"))),
					maxExecTimeScheduler);
			return builder.process(bulkParameters).getResult();
		} catch (final InvalidExecution e) {
			if (log.isDebugEnabled()) {
				log.debug("Can't execute {}, it return: {}", execName, e.getStdErr());
			}
			throw e;
		}
	}

	private class ExecBuilder extends
							  ProcessingToolBuilder<String, FFbase, CapturedStdOutErrTextRetention, KeepStdoutAndErrToLogWatcher> {

		final FFbase ffbase;

		protected ExecBuilder(final FFbase ffbase) {
			super(ffbase.getExecutableName(), new KeepStdoutAndErrToLogWatcher());
			this.ffbase = ffbase;
		}

		@Override
		protected FFbase getParametersProvider(final String sourceOrigin) {
			return ffbase;
		}

		@Override
		protected CapturedStdOutErrTextRetention compute(final String sourceOrigin,
														 final ProcesslauncherLifecycle lifeCycle) {
			return executorWatcher.getTextRetention();
		}

	}

	private FFAboutVersion version;
	private List<FFAboutCodec> codecs;
	private List<FFAboutFormat> formats;
	private List<FFAboutDevice> devices;
	private Set<String> bitStreamFilters;
	private FFAboutProtocols protocols;
	private List<FFAboutFilter> filters;
	private List<FFAboutPixelFormat> pixelsFormats;
	private Set<String> hardwareAccelerationMethods;

	public synchronized FFAboutVersion getVersion() {
		if (version == null) {
			version = new FFAboutVersion(internalRun("-loglevel quiet -version").getStdouterrLines(false).map(
					String::trim)
					.toList());
		}
		return version;
	}

	/**
	 * -codecs show available codecs
	 */
	public synchronized List<FFAboutCodec> getCodecs() {
		if (codecs == null) {
			codecs = FFAboutCodec.parse(internalRun("-codecs").getStdoutLines(false)
					.map(String::trim)
					.toList());
		}
		return codecs;
	}

	/**
	 * -formats show available formats
	 */
	public synchronized List<FFAboutFormat> getFormats() {
		if (formats == null) {
			formats = FFAboutFormat.parseFormats(internalRun("-formats").getStdoutLines(false)
					.map(String::trim)
					.toList());
		}
		return formats;
	}

	/**
	 * -devices show available devices
	 */
	public synchronized List<FFAboutDevice> getDevices() {
		if (devices == null) {
			devices = FFAboutDevice.parseDevices(internalRun("-devices").getStdoutLines(false)
					.map(String::trim)
					.toList());
		}
		return devices;
	}

	static Set<String> parseBSFS(final Stream<String> lines) {
		return lines.map(String::trim).filter(line -> (line.toLowerCase().startsWith("Bitstream filters:"
				.toLowerCase()) == false))
				.collect(Collectors.toSet());
	}

	/**
	 * -bsfs show available bit stream filters
	 */
	public synchronized Set<String> getBitStreamFilters() {
		if (bitStreamFilters == null) {
			bitStreamFilters = parseBSFS(internalRun("-bsfs").getStdoutLines(false).map(String::trim));
		}
		return bitStreamFilters;
	}

	/**
	 * -protocols show available protocols
	 */
	public synchronized FFAboutProtocols getProtocols() {
		if (protocols == null) {
			protocols = new FFAboutProtocols(internalRun("-protocols").getStdouterrLines(false).map(String::trim)
					.toList());
		}

		return protocols;
	}

	/**
	 * -filters show available filters
	 */
	public synchronized List<FFAboutFilter> getFilters() {
		if (filters == null) {
			filters = FFAboutFilter.parseFilters(internalRun("-filters").getStdoutLines(false).map(String::trim)
					.toList());
		}
		return filters;
	}

	/**
	 * -pix_fmts show available pixel formats
	 */
	public synchronized List<FFAboutPixelFormat> getPixelFormats() {
		if (pixelsFormats == null) {
			pixelsFormats = FFAboutPixelFormat.parsePixelsFormats(internalRun("-pix_fmts").getStdoutLines(false).map(
					String::trim).toList());
		}
		return pixelsFormats;
	}

	static Set<String> parseHWAccelerationMethods(final Stream<String> lines) {
		return lines.map(String::trim).filter(line -> (line.toLowerCase().startsWith("Hardware acceleration methods:"
				.toLowerCase()) == false)).collect(Collectors.toSet());
	}

	/**
	 * -hwaccels show available HW acceleration methods
	 */
	public synchronized Set<String> getAvailableHWAccelerationMethods() {
		if (hardwareAccelerationMethods == null) {
			hardwareAccelerationMethods = parseHWAccelerationMethods(internalRun("-hwaccels").getStdoutLines(false)
					.map(String::trim));
		}
		return hardwareAccelerationMethods;
	}

	/**
	 * Get by ffmpeg -sample_fmts
	 */
	public static final Map<String, Integer> sample_formats;

	static {
		final var sf = new HashMap<String, Integer>();
		sf.put("u8", 8);
		sf.put("s16", 16);
		sf.put("s32", 32);
		sf.put("flt", 32);
		sf.put("dbl", 64);
		sf.put("u8p", 8);
		sf.put("s16p", 16);
		sf.put("s32p", 32);
		sf.put("fltp", 32);
		sf.put("dblp", 64);
		sf.put("s64", 64);
		sf.put("s64p", 64);
		sample_formats = Collections.unmodifiableMap(sf);
	}

	public boolean isCoderIsAvaliable(final String codec_name) {
		return getCodecs().stream()
				.anyMatch(codec -> (codec.name.equalsIgnoreCase(codec_name) && codec.encodingSupported == true));
	}

	public boolean isDecoderIsAvaliable(final String codec_name) {
		return getCodecs().stream()
				.anyMatch(codec -> (codec.name.equalsIgnoreCase(codec_name) && codec.decodingSupported == true));
	}

	public boolean isFromFormatIsAvaliable(final String demuxer_name) {
		return getFormats().stream()
				.anyMatch(format -> (format.name.equalsIgnoreCase(demuxer_name) && format.demuxing == true));
	}

	public boolean isToFormatIsAvaliable(final String muxer_name) {
		return getFormats().stream()
				.anyMatch(format -> (format.name.equalsIgnoreCase(muxer_name) && format.muxing == true));
	}

	public boolean isFilterIsAvaliable(final String filter_name) {
		return getFilters().stream()
				.anyMatch(filter -> filter.getTag().equalsIgnoreCase(filter_name));
	}

	/**
	 * @param engine_name like libx264rgb or libxvid
	 *        ALL CODECS ARE NOT AVAILABLE FOR ALL GRAPHICS CARDS, EVEN IF FFMPEG SUPPORT IT HERE.
	 */
	public boolean isCoderEngineIsAvaliable(final String engine_name) {
		return getCodecs().stream()
				.anyMatch(codec -> (codec.encodingSupported == true
									&& codec.encoders.contains(engine_name)));
	}

	/**
	 * @param engine_name like h264_cuvid or libopenjpeg
	 *        ALL CODECS ARE NOT AVAILABLE FOR ALL GRAPHICS CARDS, EVEN IF FFMPEG SUPPORT IT HERE.
	 */
	public boolean isDecoderEngineIsAvaliable(final String engine_name) {
		return getCodecs().stream().anyMatch(codec -> (codec.decodingSupported == true && codec.decoders.contains(
				engine_name)));
	}

	/**
	 * ALL FUNCTIONS ARE NOT AVAILABLE FOR ALL GRAPHICS CARDS, EVEN IF FFMPEG SUPPORT IT HERE.
	 * @return true if configured and up for cuda, cuvid and nvenc
	 */
	public boolean isNVToolkitIsAvaliable() {
		if (getAvailableHWAccelerationMethods().contains("cuda") == false) {
			log.debug("(NVIDIA) Cuda is not available in hardware acceleration methods");
			return false;
		} else if (getAvailableHWAccelerationMethods().contains(CUVID) == false) {
			log.debug("(NVIDIA) Cuvid is not available in hardware acceleration methods");
			return false;
		}
		final var allNvRelatedCodecs = getCodecs().stream()
				.filter(c -> c.decoders.isEmpty() == false || c.encoders.isEmpty() == false)
				.flatMap(c -> Stream.concat(c.decoders.stream(), c.encoders.stream()))
				.distinct()
				.filter(c -> c.contains("nvenc") || c.contains(CUVID))
				.toList();

		if (allNvRelatedCodecs.stream().noneMatch(c -> c.contains("nvenc"))) {
			log.debug("(NVIDIA) nvenc is not available in codec list");
			return false;
		} else if (allNvRelatedCodecs.stream().noneMatch(c -> c.contains(CUVID))) {
			log.debug("(NVIDIA) cuvid is not available in codec list");
			return false;
		}

		return true;
	}

	/**
	 * ALL FUNCTIONS ARE NOT AVAILABLE FOR ALL GRAPHICS CARDS, EVEN IF FFMPEG SUPPORT IT HERE.
	 * @return true if configured with NVIDIA Performance Primitives via libnpp
	 */
	public boolean isHardwareNVScalerFilterIsAvaliable() {
		return getVersion().configuration.contains("libnpp");
	}

}
