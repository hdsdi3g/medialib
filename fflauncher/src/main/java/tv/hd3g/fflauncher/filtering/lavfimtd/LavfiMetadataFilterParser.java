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

import static java.lang.Float.NEGATIVE_INFINITY;
import static java.lang.Float.NaN;
import static java.lang.Float.POSITIVE_INFINITY;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toUnmodifiableMap;
import static tv.hd3g.fflauncher.filtering.AudioFilterAPhasemeter.APHASEMETER;
import static tv.hd3g.fflauncher.recipes.MediaAnalyser.assertAndParse;
import static tv.hd3g.fflauncher.recipes.MediaAnalyser.splitter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LavfiMetadataFilterParser {
	public static final String DEFAULT_KEY = "default";
	private static final String SUFFIX_END = "_end";
	private static final String SUFFIX_START = "_start";

	private final List<String> bucket;
	private final List<Map<String, Map<String, String>>> rawEvents;
	private LavfiMtdPosition currentPosition;

	@Getter
	private final List<LavfiMtdValue<Float>> aPhaseMeterReport;
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
			throw new IllegalArgumentException("Invalid line content inside frame: " + rawLine);
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
	static LavfiMtdPosition parseFrameLine(final String line) {
		final var items = splitter(line, ' ');
		return new LavfiMtdPosition(
				assertAndParse(items.get(0), "frame:", Integer::valueOf),
				assertAndParse(items.get(1), "pts:", Long::valueOf),
				assertAndParse(items.get(2), "pts_time:", Float::valueOf));
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

	/**
	 * From a float string chain
	 */
	private static int parseInt(final String floatString) {
		return Math.round(Float.parseFloat(floatString));
	}

	/**
	 * From a double string chain
	 */
	private static long parseLong(final String doubleString) {
		return Math.round(Double.parseDouble(doubleString));
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
				.map(LavfiMetadataFilterParser::parseFloat)
				.ifPresent(value -> toAdd.add(toMtdValue(value)));
	}

	public static float parseFloat(final String value) {
		if (value == null || value.isBlank() || value.equalsIgnoreCase("nan")) {
			return NaN;
		} else if (value.equalsIgnoreCase("-inf")) {
			return NEGATIVE_INFINITY;
		} else if (value.equalsIgnoreCase("inf")) {
			return POSITIVE_INFINITY;
		}
		return Float.valueOf(value);
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
	private static Optional<LavfiMtdAstats> extractAstats(final Map<String, String> rawFrames) {
		final var channelsContent = new ArrayList<Map<String, String>>();

		rawFrames.entrySet().forEach(entry -> {
			final var keyLine = entry.getKey();
			final var channelId = Integer.valueOf(keyLine.substring(0, keyLine.indexOf("."))) - 1;
			final var key = keyLine.substring(keyLine.indexOf(".") + 1, keyLine.length());

			while (channelId >= channelsContent.size()) {
				channelsContent.add(new HashMap<>());
			}
			channelsContent.get(channelId).put(key, entry.getValue());
		});

		final var channels = channelsContent.stream()
				.map(content -> {
					final var dcOffset = Optional.ofNullable(content.remove("DC_offset"))
							.map(LavfiMetadataFilterParser::parseFloat)
							.orElse(Float.NaN);
					final var peakLevel = Optional.ofNullable(content.remove("Peak_level"))
							.map(LavfiMetadataFilterParser::parseFloat)
							.orElse(Float.NaN);
					final var flatFactor = Optional.ofNullable(content.remove("Flat_factor"))
							.map(LavfiMetadataFilterParser::parseFloat)
							.orElse(Float.NaN);
					final var peakCount = Optional.ofNullable(content.remove("Peak_count"))
							.map(LavfiMetadataFilterParser::parseLong)
							.orElse(0L);
					final var noiseFloor = Optional.ofNullable(content.remove("Noise_floor"))
							.map(LavfiMetadataFilterParser::parseFloat)
							.orElse(Float.NaN);
					final var noiseFloorCount = Optional.ofNullable(content.remove("Noise_floor_count"))
							.map(LavfiMetadataFilterParser::parseLong)
							.orElse(0L);
					final var entropy = Optional.ofNullable(content.remove("Entropy"))
							.map(LavfiMetadataFilterParser::parseFloat)
							.orElse(Float.NaN);
					final var other = content.entrySet().stream()
							.collect(toUnmodifiableMap(Entry::getKey,
									entry -> parseFloat(entry.getValue())));

					return new LavfiMtdAstatsChannel(
							dcOffset,
							peakLevel,
							flatFactor,
							peakCount,
							noiseFloor,
							noiseFloorCount,
							entropy,
							other);
				})
				.toList();

		if (channels.isEmpty()) {
			return Optional.empty();
		}

		return Optional.ofNullable(new LavfiMtdAstats(channels));
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
	private static Optional<LavfiMtdIdet> extractIdet(final Map<String, String> rawFrames) {
		try {
			final var single = new LavfiMtdIdetFrame(
					LavfiMtdIdetSingleFrameType.valueOf(
							rawFrames.get("single.current_frame").toUpperCase()),
					parseInt(rawFrames.get("single.tff")),
					parseInt(rawFrames.get("single.bff")),
					parseInt(rawFrames.get("single.progressive")),
					parseInt(rawFrames.get("single.undetermined")));
			final var multiple = new LavfiMtdIdetFrame(
					LavfiMtdIdetSingleFrameType.valueOf(
							rawFrames.get("multiple.current_frame").toUpperCase()),
					parseInt(rawFrames.get("multiple.tff")),
					parseInt(rawFrames.get("multiple.bff")),
					parseInt(rawFrames.get("multiple.progressive")),
					parseInt(rawFrames.get("multiple.undetermined")));
			final var repeated = new LavfiMtdIdetRepeatedFrame(
					LavfiMtdIdetRepeatedFrameType.valueOf(
							rawFrames.get("repeated.current_frame").toUpperCase()),
					parseInt(rawFrames.get("repeated.neither")),
					parseInt(rawFrames.get("repeated.top")),
					parseInt(rawFrames.get("repeated.bottom")));
			return Optional.ofNullable(new LavfiMtdIdet(single, multiple, repeated));
		} catch (final NullPointerException e) {
			return Optional.empty();
		}
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
				.reduce(new ArrayList<LavfiMtdEvent>(0),
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

}
