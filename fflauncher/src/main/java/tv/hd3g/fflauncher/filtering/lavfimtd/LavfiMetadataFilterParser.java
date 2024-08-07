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
 * Copyright (C) hdsdi3g for hd3g.tv 2023
 *
 */
package tv.hd3g.fflauncher.filtering.lavfimtd;

import static java.lang.Integer.MAX_VALUE;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toUnmodifiableMap;
import static tv.hd3g.fflauncher.filtering.AudioFilterAPhasemeter.APHASEMETER;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.SequencedMap;
import java.util.function.Function;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LavfiMetadataFilterParser implements NumberParserTraits {
	public static final String DEFAULT_KEY = "default";
	private static final String SUFFIX_END = "_end";
	private static final String SUFFIX_START = "_start";

	private final List<String> bucket;
	private final List<Map<String, Map<String, String>>> rawEvents;
	private LavfiMtdPosition currentPosition;

	@Getter
	private final List<LavfiMtdValue<Float>> aPhaseMeterReport;
	@Getter
	private final List<LavfiMtdValue<LavfiMtdR128>> r128Report;
	@Getter
	private final List<LavfiMtdValue<LavfiMtdAstats>> aStatsReport;
	@Getter
	private final List<LavfiMtdValue<Float>> blockDetectReport;
	@Getter
	private final List<LavfiMtdValue<Float>> blurDetectReport;
	@Getter
	private final List<LavfiMtdValue<LavfiMtdCropdetect>> cropDetectReport;
	@Getter
	private final List<LavfiMtdValue<LavfiMtdIdet>> idetReport;
	@Getter
	private final List<LavfiMtdValue<LavfiMtdSiti>> sitiReport;
	@Getter
	private final List<LavfiMtdEvent> monoEvents;
	@Getter
	private final List<LavfiMtdEvent> freezeEvents;
	@Getter
	private final List<LavfiMtdEvent> silenceEvents;
	@Getter
	private final List<LavfiMtdEvent> blackEvents;

	public LavfiMetadataFilterParser() {
		bucket = new ArrayList<>();
		rawEvents = new ArrayList<>();
		aPhaseMeterReport = new ArrayList<>();
		r128Report = new ArrayList<>();
		aStatsReport = new ArrayList<>();
		blockDetectReport = new ArrayList<>();
		blurDetectReport = new ArrayList<>();
		cropDetectReport = new ArrayList<>();
		idetReport = new ArrayList<>();
		sitiReport = new ArrayList<>();
		monoEvents = new ArrayList<>();
		freezeEvents = new ArrayList<>();
		silenceEvents = new ArrayList<>();
		blackEvents = new ArrayList<>();
	}

	/**
	 * @param line only inject lavfi metadata stream, like "frame:1022 pts:981168 pts_time:20.441", "lavfi.aphasemeter.phase=1.000000", "lavfi.aphasemeter.mono_start=18.461"
	 */
	public void addLavfiRawLine(final String rawLine) {
		log.trace("Lavfi line: {}", rawLine);
		if (rawLine.startsWith("frame:")) {
			if (currentPosition == null) {
				currentPosition = parseFrameLine(rawLine);
				return;
			}
			completeFrame();
			currentPosition = parseFrameLine(rawLine);
		} else if (currentPosition == null) {
			throw new IllegalArgumentException("Missing frame declaration: " + rawLine);
		} else if (rawLine.startsWith("lavfi.")) {
			bucket.add(rawLine);
		} else {
			log.debug("Can't manage lavfi line content inside frame (ignore it): {}", rawLine);
		}
	}

	public LavfiMetadataFilterParser close() {
		if (bucket.isEmpty() == false && currentPosition != null) {
			completeFrame();
			currentPosition = null;
		}
		extractEvents();
		return this;
	}

	public int getReportCount() {
		return aPhaseMeterReport.size() +
			   aStatsReport.size() +
			   blockDetectReport.size() +
			   blurDetectReport.size() +
			   cropDetectReport.size() +
			   idetReport.size() +
			   sitiReport.size();
	}

	public int getEventCount() {
		return monoEvents.size() +
			   freezeEvents.size() +
			   silenceEvents.size() +
			   blackEvents.size();
	}

	/**
	 * frame:1022 pts:981168 pts_time:20.441
	 */
	LavfiMtdPosition parseFrameLine(final String line) {
		final var items = splitter(line, ' ');
		return new LavfiMtdPosition(
				assertAndParse(items.get(0), "frame:", this::parseIntOrNeg1),
				assertAndParse(items.get(1), "pts:", this::parseLongOrNeg1),
				assertAndParse(items.get(2), "pts_time:", this::parseFloatOrNeg1));
	}

	private void completeFrame() {
		final var rawFrame = bucket.stream()
				.map(line -> assertAndParse(line, "lavfi."))
				.toList()
				.stream()
				.collect(groupingBy(
						line -> splitter(splitter(line, '.', 1).get(0), '=', 1).get(0),
						HashMap::new,
						mapping(line -> splitter(line, '='),
								toMap(
										line -> {
											final var kv = line.get(0);
											final var pos = kv.indexOf(".");
											if (pos == -1) {
												return DEFAULT_KEY;
											}
											return kv.substring(pos + 1);
										},
										line -> line.get(1)))));

		rawFrame.entrySet().forEach(entry -> extractMetadatas(entry.getKey(), entry.getValue()));

		final var frameContainEvent = rawFrame.entrySet().stream()
				.anyMatch(rf -> {
					final var filterName = rf.getKey();
					if (filterName.contains(SUFFIX_START) || filterName.contains(SUFFIX_END)) {
						return true;
					}
					return rf.getValue().keySet().stream()
							.anyMatch(k -> k.contains(SUFFIX_START) || k.contains(SUFFIX_END));
				});
		if (frameContainEvent) {
			rawEvents.add(rawFrame.entrySet().stream()
					.collect(toUnmodifiableMap(Entry::getKey, Entry::getValue)));
		}

		bucket.clear();
	}

	private <T> LavfiMtdValue<T> toMtdValue(final T value) {
		return new LavfiMtdValue<>(currentPosition.frame(), currentPosition.pts(), currentPosition.ptsTime(), value);
	}

	/**
	 * [...]
	 * aphasemeter => mono_duration => 2.94
	 * astats => 1.Peak_level => -0.622282
	 * idet => repeated.bottom => 3.00
	 * blur => default => 5.744382
	 * [...]
	 */
	private void extractMetadatas(final String filterName, final Map<String, String> rawFrames) {
		switch (filterName) {
		case APHASEMETER ->
				/** lavfi.aphasemeter.phase=0.992454 */
				extractMetadataFloat("phase", rawFrames, aPhaseMeterReport);
		case "astats" -> extractAstats(rawFrames)
				.ifPresent(value -> aStatsReport.add(toMtdValue(value)));
		case "r128" -> extractR128(rawFrames)
				.ifPresent(value -> r128Report.add(toMtdValue(value)));
		case "block" ->
				/** lavfi.block=2.204194 */
				extractMetadataFloat(DEFAULT_KEY, rawFrames, blockDetectReport);
		case "blur" ->
				/** lavfi.blur=5.744382 */
				extractMetadataFloat(DEFAULT_KEY, rawFrames, blurDetectReport);
		case "cropdetect" -> extractCropdetect(rawFrames)
				.ifPresent(value -> cropDetectReport.add(toMtdValue(value)));
		case "idet" -> extractIdet(rawFrames)
				.ifPresent(value -> idetReport.add(toMtdValue(value)));
		case "siti" -> extractSiti(rawFrames)
				.ifPresent(value -> sitiReport.add(toMtdValue(value)));
		default -> {
			if (filterName.contains(SUFFIX_START)
				|| filterName.contains(SUFFIX_END)
				|| filterName.contains("_duration")
				|| rawFrames.keySet().stream()
						.anyMatch(f -> f.contains(SUFFIX_START)
									   || f.contains(SUFFIX_END)
									   || f.contains("_duration"))) {
				return;
			}
			log.warn("Can't manage filter value {}: {} [{}]", filterName, rawFrames, currentPosition);
		}
		}
	}

	private void extractEvents() {
		importEventsForFilter(APHASEMETER, "mono", "mono", monoEvents);
		importEventsForFilter("freezedetect", "freeze", "freeze", freezeEvents);
		importEventsForFilter("silence", DEFAULT_KEY, "silence", silenceEvents);
		importEventsForFilter("black", DEFAULT_KEY, "black", blackEvents);
		rawEvents.clear();
	}

	private void extractMetadataFloat(final String keyName,
									  final Map<String, String> rawFrames,
									  final List<LavfiMtdValue<Float>> toAdd) {
		Optional.ofNullable(rawFrames.get(keyName))
				.map(this::parseFloat)
				.ifPresent(value -> toAdd.add(toMtdValue(value)));
	}

	/**
	 * lavfi.astats.1.DC_offset=0.000001
	 * lavfi.astats.1.Peak_level=-0.622282
	 * lavfi.astats.1.Flat_factor=0.000000
	 * lavfi.astats.1.Peak_count=2.000000
	 * lavfi.astats.1.Noise_floor=-78.266739
	 * lavfi.astats.1.Noise_floor_count=708.000000
	 * lavfi.astats.1.Entropy=0.788192
	 * lavfi.astats.2.DC_offset=0.000002
	 * lavfi.astats.2.Peak_level=-0.622282
	 * lavfi.astats.2.Flat_factor=0.000000
	 * lavfi.astats.2.Peak_count=2.000000
	 * lavfi.astats.2.Noise_floor=-78.266739
	 * lavfi.astats.2.Noise_floor_count=1074.000000
	 * lavfi.astats.2.Entropy=0.788152
	 */
	private Optional<LavfiMtdAstats> extractAstats(final Map<String, String> rawFrames) {
		final var channelsContent = new ArrayList<Map<String, String>>();

		rawFrames.entrySet().forEach(entry -> {
			final var keyLine = entry.getKey();
			final var channelId = Integer.valueOf(keyLine.substring(0, keyLine.indexOf("."))) - 1;
			final var key = keyLine.substring(keyLine.indexOf(".") + 1, keyLine.length());

			while (channelId >= channelsContent.size()) {
				channelsContent.add(new HashMap<>());
			}
			channelsContent.get(channelId).put(key.replace(" ", "_"), entry.getValue());
		});

		final var channels = channelsContent.stream()
				.map(content -> new LavfiMtdAstatsChannel(
						getFloat("DC_offset", content),
						getFloat("Peak_level", content),
						getLong("Flat_factor", content),
						getLong("Peak_count", content),
						getFloat("Noise_floor", content),
						getLong("Noise_floor_count", content),
						getFloat("Entropy", content),
						getInt("Bit_depth", content),
						getFloat("Crest_factor", content),
						getFloat("Dynamic_range", content),
						getFloat("Flat_factor", content),
						getFloat("Max_difference", content),
						getFloat("Max_level", content),
						getFloat("Mean_difference", content),
						getFloat("Min_difference", content),
						getFloat("Min_level", content),
						getFloat("RMS_difference", content),
						getFloat("RMS_level", content),
						getFloat("RMS_peak", content),
						getFloat("RMS_trough", content),
						getFloat("Zero_crossings", content),
						getFloat("Zero_crossings_rate", content),
						getLong("Number_of_Infs", content),
						getLong("Number_of_NaNs", content),
						getLong("Number_of_denormals", content),
						getLong("Number_of_samples", content),
						getLong("Abs_Peak_count", content),
						getOthers(content)))
				.toList();

		if (channels.isEmpty()) {
			return Optional.empty();
		}

		return Optional.ofNullable(new LavfiMtdAstats(channels));
	}

	private SequencedMap<String, Float> getOthers(final Map<String, String> content) {
		final var others = new LinkedHashMap<String, Float>();
		content.keySet()
				.stream()
				.sorted()
				.forEach(key -> others.put(key, parseFloat(content.get(key))));
		return Collections.unmodifiableSequencedMap(others);
	}

	private float getFloat(final String key, final Map<String, String> content) {
		return Optional.ofNullable(content.remove(key))
				.map(this::parseFloat)
				.orElse(Float.NaN);
	}

	private long getLong(final String key, final Map<String, String> content) {
		return Optional.ofNullable(content.remove(key))
				.flatMap(this::parseLong)
				.orElse(0l);
	}

	private int getInt(final String key, final Map<String, String> content) {
		return Optional.ofNullable(content.remove(key))
				.flatMap(this::parseInt)
				.orElse(0);
	}

	/**
	 * frame:1801 pts:60041 pts_time:60.041
	 * lavfi.cropdetect.x1=0
	 * lavfi.cropdetect.x2=479
	 * lavfi.cropdetect.y1=0
	 * lavfi.cropdetect.y2=479
	 * lavfi.cropdetect.w=480
	 * lavfi.cropdetect.h=480
	 * lavfi.cropdetect.x=0
	 * lavfi.cropdetect.y=0
	 * frame:7616 pts:317340 pts_time:317.34
	 * lavfi.cropdetect.x1=0
	 * lavfi.cropdetect.x2=1919
	 * lavfi.cropdetect.y1=0
	 * lavfi.cropdetect.y2=1079
	 * lavfi.cropdetect.w=1920
	 * lavfi.cropdetect.h=1072
	 * lavfi.cropdetect.x=0
	 * lavfi.cropdetect.y=4
	 */
	private static Optional<LavfiMtdCropdetect> extractCropdetect(final Map<String, String> rawFrames) {
		try {
			final var x1 = Integer.parseInt(rawFrames.get("x1"));
			final var x2 = Integer.parseInt(rawFrames.get("x2"));
			final var y1 = Integer.parseInt(rawFrames.get("y1"));
			final var y2 = Integer.parseInt(rawFrames.get("y2"));
			final var w = Integer.parseInt(rawFrames.get("w"));
			final var h = Integer.parseInt(rawFrames.get("h"));
			final var x = Integer.parseInt(rawFrames.get("x"));
			final var y = Integer.parseInt(rawFrames.get("y"));
			return Optional.ofNullable(new LavfiMtdCropdetect(x1, x2, y1, y2, w, h, x, y));
		} catch (final NullPointerException e) {
			return Optional.empty();
		}
	}

	/**
	 * lavfi.idet.repeated.current_frame=neither
	 * lavfi.idet.repeated.neither=115.00
	 * lavfi.idet.repeated.top=2.00
	 * lavfi.idet.repeated.bottom=3.00
	 * -
	 * lavfi.idet.single.current_frame=progressive
	 * lavfi.idet.single.tff=0.00
	 * lavfi.idet.single.bff=0.00
	 * lavfi.idet.single.progressive=40.00
	 * lavfi.idet.single.undetermined=80.00
	 * -
	 * lavfi.idet.multiple.current_frame=progressive
	 * lavfi.idet.multiple.tff=0.00
	 * lavfi.idet.multiple.bff=0.00
	 * lavfi.idet.multiple.progressive=120.00
	 * lavfi.idet.multiple.undetermined=0.00
	 */
	private Optional<LavfiMtdIdet> extractIdet(final Map<String, String> rawFrames) {
		try {
			final var single = new LavfiMtdIdetFrame(
					LavfiMtdIdetSingleFrameType.valueOf(
							rawFrames.get("single.current_frame").toUpperCase()),
					parseIntOrNeg1(rawFrames.get("single.tff")),
					parseIntOrNeg1(rawFrames.get("single.bff")),
					parseIntOrNeg1(rawFrames.get("single.progressive")),
					parseIntOrNeg1(rawFrames.get("single.undetermined")));
			final var multiple = new LavfiMtdIdetFrame(
					LavfiMtdIdetSingleFrameType.valueOf(
							rawFrames.get("multiple.current_frame").toUpperCase()),
					parseIntOrNeg1(rawFrames.get("multiple.tff")),
					parseIntOrNeg1(rawFrames.get("multiple.bff")),
					parseIntOrNeg1(rawFrames.get("multiple.progressive")),
					parseIntOrNeg1(rawFrames.get("multiple.undetermined")));
			final var repeated = new LavfiMtdIdetRepeatedFrame(
					LavfiMtdIdetRepeatedFrameType.valueOf(
							rawFrames.get("repeated.current_frame").toUpperCase()),
					parseIntOrNeg1(rawFrames.get("repeated.neither")),
					parseIntOrNeg1(rawFrames.get("repeated.top")),
					parseIntOrNeg1(rawFrames.get("repeated.bottom")));
			return Optional.ofNullable(new LavfiMtdIdet(single, multiple, repeated));
		} catch (final NullPointerException e) {
			return Optional.empty();
		}
	}

	/**
	 * lavfi.r128.M=-36.182
	 * lavfi.r128.S=-36.183
	 * lavfi.r128.I=-24.545
	 * lavfi.r128.LRA=17.320
	 * lavfi.r128.LRA.low=-36.190
	 * lavfi.r128.LRA.high=-18.870
	 * lavfi.r128.sample_peaks_ch0=0.133
	 * lavfi.r128.sample_peaks_ch1=0.117
	 * lavfi.r128.sample_peak=0.133
	 * lavfi.r128.true_peaks_ch0=0.133
	 * lavfi.r128.true_peaks_ch1=0.118
	 * lavfi.r128.true_peak=0.133
	 */
	private Optional<LavfiMtdR128> extractR128(final Map<String, String> rawFrames) {
		return Optional.ofNullable(new LavfiMtdR128(
				parseFloat(rawFrames.get("S")),
				parseFloat(rawFrames.get("M")),
				parseFloat(rawFrames.get("I")),
				parseFloat(rawFrames.get("LRA")),
				parseFloat(rawFrames.get("LRA.low")),
				parseFloat(rawFrames.get("LRA.high")),
				linearToDb(parseFloat(rawFrames.get("sample_peak"))),
				new Stereo<>(
						linearToDb(parseFloat(rawFrames.get("sample_peaks_ch0"))),
						linearToDb(parseFloat(rawFrames.get("sample_peaks_ch1")))),
				linearToDb(parseFloat(rawFrames.get("true_peak"))),
				new Stereo<>(
						linearToDb(parseFloat(rawFrames.get("true_peaks_ch0"))),
						linearToDb(parseFloat(rawFrames.get("true_peaks_ch1"))))));
	}

	/**
	 * lavfi.siti.si=11.67
	 * lavfi.siti.ti=5.60
	 */
	private static Optional<LavfiMtdSiti> extractSiti(final Map<String, String> rawFrames) {
		final var si = rawFrames.get("si");
		final var ti = rawFrames.get("ti");
		if (si == null || ti == null) {
			return Optional.empty();
		}
		return Optional.ofNullable(new LavfiMtdSiti(Float.valueOf(si), Float.valueOf(ti)));
	}

	private void importEventsForFilter(final String lavfiMtdFilterKeyName,
									   final String lavfiMtdKeyName,
									   final String eventsNameToSet,
									   final List<LavfiMtdEvent> listToAddResult) {
		listToAddResult.addAll(rawEvents.stream()
				.filter(entries -> {
					final var itemKeys = entries.keySet();
					if (itemKeys.contains(lavfiMtdFilterKeyName)) {
						final var subItemKeys = entries.get(lavfiMtdFilterKeyName).keySet();
						return subItemKeys.contains(lavfiMtdKeyName + SUFFIX_START)
							   || subItemKeys.contains(lavfiMtdKeyName + SUFFIX_END);
					}
					return itemKeys.contains(lavfiMtdFilterKeyName + SUFFIX_START)
						   || itemKeys.contains(lavfiMtdFilterKeyName + SUFFIX_END);
				})
				.flatMap(entries -> {
					final var itemKeys = entries.keySet();
					if (itemKeys.contains(lavfiMtdFilterKeyName)) {
						return Stream.of(mapStyle0(lavfiMtdFilterKeyName, lavfiMtdKeyName, entries));
					}

					if (itemKeys.contains(lavfiMtdFilterKeyName + SUFFIX_START)) {
						return mapStyle1(lavfiMtdFilterKeyName + SUFFIX_START,
								lavfiMtdKeyName, entries, LavfiMtdEventFrameType.START);
					} else if (itemKeys.contains(lavfiMtdFilterKeyName + SUFFIX_END)) {
						return mapStyle1(lavfiMtdFilterKeyName + SUFFIX_END,
								lavfiMtdKeyName, entries, LavfiMtdEventFrameType.END);
					}

					throw new IllegalStateException("Can't extract to event: " + entries);
				})
				.reduce(new ArrayList<>(0),
						(list, frameDef) -> {
							if (frameDef.type() == LavfiMtdEventFrameType.START) {
								list.add(new LavfiMtdEvent(eventsNameToSet,
										frameDef.scope(),
										Float.valueOf(frameDef.time()),
										Float.NaN));
							} else {
								final var lastPos = reverseSearch(list, eventsNameToSet, frameDef.scope());
								if (lastPos == -1) {
									log.warn("Can't found start event for {}/{}", eventsNameToSet, frameDef);
									return list;
								}
								final var previousStart = list.get(lastPos);
								list.set(lastPos,
										new LavfiMtdEvent(
												eventsNameToSet,
												frameDef.scope(),
												previousStart.start(),
												Float.valueOf(frameDef.time())));
							}
							return list;
						},
						(l, r) -> {
							l.addAll(r);
							return l;
						}));
	}

	/**
	 * lavfi.freezedetect.freeze_start=0.757
	 * lavfi.freezedetect.freeze_duration=3.292
	 * lavfi.freezedetect.freeze_end=4.049
	 */
	private LavfiMtdEventFrameDef mapStyle0(final String lavfiMtdFilterKeyName,
											final String lavfiMtdKeyName,
											final Map<String, Map<String, String>> entries) {
		final var subEntry = entries.get(lavfiMtdFilterKeyName);
		if (subEntry.containsKey(lavfiMtdKeyName + SUFFIX_START)) {
			return new LavfiMtdEventFrameDef(LavfiMtdEventFrameType.START, null, subEntry.get(lavfiMtdKeyName
																							  + SUFFIX_START));
		} else if (subEntry.containsKey(lavfiMtdKeyName + SUFFIX_END)) {
			return new LavfiMtdEventFrameDef(LavfiMtdEventFrameType.END, null, subEntry.get(lavfiMtdKeyName
																							+ SUFFIX_END));
		}
		throw new IllegalStateException("Can't extract to event (mapStyle0): "
										+ lavfiMtdFilterKeyName + ", " + lavfiMtdKeyName + ", " + entries);
	}

	private Stream<LavfiMtdEventFrameDef> mapStyle1(final String keyName,
													final String lavfiMtdKeyName,
													final Map<String, Map<String, String>> entries,
													final LavfiMtdEventFrameType type) {
		final var subEntry = entries.get(keyName);
		if (subEntry.containsKey(lavfiMtdKeyName)) {
			/**
			 * lavfi.black_start=0.007
			 */
			return Stream.of(new LavfiMtdEventFrameDef(type, null, subEntry.get(lavfiMtdKeyName)));
		} else {
			/**
			 * lavfi.silence_start.1=65.1366
			 * lavfi.silence_start.2=65.1366
			 */
			return subEntry.entrySet().stream()
					.map(entry -> new LavfiMtdEventFrameDef(type, entry.getKey(), entry.getValue()));
		}
	}

	private static final int reverseSearch(final List<LavfiMtdEvent> list,
										   final String searchName,
										   final String searchScope) {
		LavfiMtdEvent entry;
		for (var pos = list.size() - 1; pos >= 0; pos--) {
			entry = list.get(pos);
			if (entry.name().equals(searchName)
				&& (entry.scope() != null ? entry.scope().equals(searchScope) : true)) {
				return pos;
			}
		}
		return -1;
	}

	public LavfiMtdSitiSummary computeSitiStats() {
		final var si = sitiReport.stream().map(LavfiMtdValue::value).mapToDouble(LavfiMtdSiti::si).summaryStatistics();
		final var ti = sitiReport.stream().map(LavfiMtdValue::value).mapToDouble(LavfiMtdSiti::ti).summaryStatistics();
		return new LavfiMtdSitiSummary(si, ti);
	}

	/**
	 * @param item like "AABB"
	 * @param assertStarts like "AA"
	 * @return like "BB"
	 */
	public static String assertAndParse(final String item, final String assertStarts) {
		return assertAndParse(item, assertStarts, l -> l);
	}

	public static <T> T assertAndParse(final String item, final String assertStarts, final Function<String, T> parser) {
		if (item.startsWith(assertStarts) == false) {
			throw new IllegalArgumentException("Not a " + assertStarts + ": " + item);
		}
		return parser.apply(item.substring(assertStarts.length()));
	}

	public static List<String> splitter(final String line, final char with) {
		return splitter(line, with, MAX_VALUE);
	}

	public static List<String> splitter(final String line, final char with, final int max) {
		var currentChars = new StringBuilder();
		final List<String> result = new ArrayList<>();

		char chr;
		for (var pos = 0; pos < line.length(); pos++) {
			chr = line.charAt(pos);
			if (chr != with) {
				currentChars.append(chr);
			} else if (currentChars.isEmpty() == false) {
				result.add(currentChars.toString());
				currentChars = new StringBuilder();
				if (result.size() >= max) {
					return unmodifiableList(result);
				}
			}
		}

		if (currentChars.isEmpty() == false) {
			result.add(currentChars.toString());
		}

		return unmodifiableList(result);
	}

}
