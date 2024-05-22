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

import java.util.Collection;
import java.util.Optional;

import tv.hd3g.fflauncher.filtering.lavfimtd.LavfiMetadataFilterParser;

public record MediaAnalyserResult(LavfiMetadataFilterParser lavfiMetadatas,
								  Collection<MediaAnalyserSessionFilterContext> filters,
								  Optional<Integer> r128Target,
								  String ffmpegCommandLine) {

	public static final int R128_DEFAULT_LUFS_TARGET = -23;

	public boolean isEmpty() {
		return Optional.ofNullable(lavfiMetadatas)
				.map(l -> l.getReportCount() > 0 || l.getEventCount() > 0)
				.orElse(false) == false;
	}

}
