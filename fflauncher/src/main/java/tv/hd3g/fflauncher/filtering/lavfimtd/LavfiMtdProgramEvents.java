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
package tv.hd3g.fflauncher.filtering.lavfimtd;

import static java.lang.Float.NaN;
import static java.util.Collections.unmodifiableList;
import static tv.hd3g.fflauncher.filtering.lavfimtd.LavfiRawMtdFrame.DEFAULT_KEY;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.Getter;

@Getter
public class LavfiMtdProgramEvents {
	private static final String SUFFIX_END = "_end";
	private static final String SUFFIX_START = "_start";

	private static Logger log = LogManager.getLogger();

	private final List<LavfiMtdEvent> events;

	private enum FrameType {
		START,
		END;
	}

	private record FrameDef(LavfiRawMtdFrame frame, FrameType type, String scope, String time) {
	}

	private LavfiMtdProgramEvents(final List<? extends LavfiRawMtdFrame> extractedRawMtdFrames,
								  final String lavfiMtdFilterKeyName,
								  final String lavfiMtdKeyName,
								  final String eventsNameToSet) {
		events = unmodifiableList(extractedRawMtdFrames.stream()
				.filter(frame -> {
					final var entries = frame.getValuesByFilterKeysByFilterName();
					final var itemKeys = entries.keySet();
					if (itemKeys.contains(lavfiMtdFilterKeyName)) {
						final var subItemKeys = entries.get(lavfiMtdFilterKeyName).keySet();
						return subItemKeys.contains(lavfiMtdKeyName + SUFFIX_START)
							   || subItemKeys.contains(lavfiMtdKeyName + SUFFIX_END);
					}
					return itemKeys.contains(lavfiMtdFilterKeyName + SUFFIX_START)
						   || itemKeys.contains(lavfiMtdFilterKeyName + SUFFIX_END);
				})
				.flatMap(frame -> {
					final var entries = frame.getValuesByFilterKeysByFilterName();

					final var itemKeys = entries.keySet();
					if (itemKeys.contains(lavfiMtdFilterKeyName)) {
						return Stream.of(mapStyle0(lavfiMtdFilterKeyName, lavfiMtdKeyName, frame, entries));
					}

					if (itemKeys.contains(lavfiMtdFilterKeyName + SUFFIX_START)) {
						return mapStyle1(lavfiMtdFilterKeyName + SUFFIX_START,
								lavfiMtdKeyName, frame, entries, FrameType.START);
					} else if (itemKeys.contains(lavfiMtdFilterKeyName + SUFFIX_END)) {
						return mapStyle1(lavfiMtdFilterKeyName + SUFFIX_END,
								lavfiMtdKeyName, frame, entries, FrameType.END);
					}

					throw new IllegalStateException("Can't extract to event: " + frame);
				})
				.reduce(new ArrayList<LavfiMtdEvent>(0),
						(list, frameDef) -> {
							if (frameDef.type == FrameType.START) {
								list.add(new LavfiMtdEvent(eventsNameToSet, frameDef.scope, Float.valueOf(
										frameDef.time), NaN));
							} else {
								final var lastPos = reverseSearch(list, eventsNameToSet, frameDef.scope);
								if (lastPos == -1) {
									log.warn("Can't found start event for {}", frameDef);
									return list;
								}
								final var previousStart = list.get(lastPos);
								list.set(lastPos,
										new LavfiMtdEvent(
												eventsNameToSet, frameDef.scope, previousStart.start(), Float.valueOf(
														frameDef.time)));
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
	private FrameDef mapStyle0(final String lavfiMtdFilterKeyName,
							   final String lavfiMtdKeyName,
							   final LavfiRawMtdFrame frame,
							   final Map<String, Map<String, String>> entries) {
		final var subEntry = entries.get(lavfiMtdFilterKeyName);
		if (subEntry.containsKey(lavfiMtdKeyName + SUFFIX_START)) {
			return new FrameDef(frame, FrameType.START, null, subEntry.get(lavfiMtdKeyName + SUFFIX_START));
		} else if (subEntry.containsKey(lavfiMtdKeyName + SUFFIX_END)) {
			return new FrameDef(frame, FrameType.END, null, subEntry.get(lavfiMtdKeyName + SUFFIX_END));
		}

		throw new IllegalStateException("Can't extract to event (mapStyle0): " + frame);
	}

	private Stream<FrameDef> mapStyle1(final String keyName,
									   final String lavfiMtdKeyName,
									   final LavfiRawMtdFrame frame,
									   final Map<String, Map<String, String>> entries,
									   final FrameType type) {
		final var subEntry = entries.get(keyName);
		if (subEntry.containsKey(lavfiMtdKeyName)) {
			/**
			 * lavfi.black_start=0.007
			 */
			return Stream.of(new FrameDef(frame, type, null, subEntry.get(lavfiMtdKeyName)));
		} else {
			/**
			 * lavfi.silence_start.1=65.1366
			 * lavfi.silence_start.2=65.1366
			 */
			return subEntry.entrySet().stream()
					.map(entry -> new FrameDef(frame, type, entry.getKey(), entry.getValue()));
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

	public LavfiMtdProgramEvents(final List<? extends LavfiRawMtdFrame> extractedRawMtdFrames,
								 final String lavfiMtdKeyName) {
		this(extractedRawMtdFrames, lavfiMtdKeyName, DEFAULT_KEY, lavfiMtdKeyName);
	}

	public LavfiMtdProgramEvents(final List<? extends LavfiRawMtdFrame> extractedRawMtdFrames,
								 final String lavfiMtdFilterKeyName,
								 final String lavfiMtdKeyName) {
		this(extractedRawMtdFrames, lavfiMtdFilterKeyName, lavfiMtdKeyName, lavfiMtdKeyName);
	}

}
