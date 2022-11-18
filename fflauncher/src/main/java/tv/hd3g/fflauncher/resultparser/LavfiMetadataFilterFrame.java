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

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toMap;
import static tv.hd3g.fflauncher.recipes.MediaAnalyser.assertAndParse;
import static tv.hd3g.fflauncher.recipes.MediaAnalyser.splitter;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class LavfiMetadataFilterFrame {
	private final int frame;
	private final long pts;
	private final float ptsTime;
	private Map<String, Map<String, String>> valuesByFilterKeysByFilterName;

	LavfiMetadataFilterFrame(final int frame, final long pts, final float ptsTime) {
		this.frame = frame;
		this.pts = pts;
		this.ptsTime = ptsTime;
	}

	/**
	 * lavfi.aphasemeter.phase=1.000000
	 * lavfi.aphasemeter.mono_start=18.461
	 * lavfi.aphasemeter.phase=0.992454
	 * lavfi.aphasemeter.mono_end=25.981
	 * lavfi.aphasemeter.mono_duration=2.94
	 * lavfi.astats.1.DC_offset=0.000001
	 * lavfi.astats.1.Peak_level=-0.622282
	 * lavfi.astats.1.Flat_factor=0.000000
	 * lavfi.astats.1.Peak_count=2.000000
	 * === TO ===
	 * [...]
	 * aphasemeter => mono_duration => 2.94
	 * astats => 1.Peak_level => -0.622282
	 * [...]
	 */
	void setRawLines(final Stream<String> sLines) {
		final var lines = sLines
				.map(line -> assertAndParse(line, "lavfi."))
				.toList();

		valuesByFilterKeysByFilterName = lines.stream()
				.collect(groupingBy(
						line -> splitter(line, '.', 1).get(0),
						HashMap::new,
						mapping(line -> splitter(line, '='),
								toMap(
										line -> {
											final var kv = line.get(0);
											final var pos = kv.indexOf(".");
											return kv.substring(pos + 1);
										},
										line -> line.get(1)))));
	}

}
