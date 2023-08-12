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

import java.util.Optional;
import java.util.Set;

import tv.hd3g.fflauncher.filtering.lavfimtd.LavfiMetadataFilterParser;
import tv.hd3g.fflauncher.resultparser.Ebur128Summary;

public record MediaAnalyserResult(LavfiMetadataFilterParser lavfiMetadatas,
								  Ebur128Summary ebur128Summary,
								  Set<MediaAnalyserSessionFilterContext> filters) {

	public boolean isEmpty() {
		final var lavfiMetadatasPresence = Optional.ofNullable(lavfiMetadatas)
				.map(l -> l.getReportCount() > 0 || l.getEventCount() > 0)
				.orElse(false);

		final var ebur128SummaryPresence = Optional.ofNullable(ebur128Summary)
				.map(em -> em.isEmpty() == false)
				.orElse(false);

		return lavfiMetadatasPresence == false && ebur128SummaryPresence == false;
	}

}
