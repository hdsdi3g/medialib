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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;

import lombok.Getter;

@Getter
public class LavfiMtdProgramFrames<T> {
	private final Map<LavfiMtdPosition, T> frames;

	/**
	 * @param transformer see LavfiRawMtdFrame.DEFAULT_KEY to extract the only data setup.
	 */
	public LavfiMtdProgramFrames(final List<? extends LavfiRawMtdFrame> extractedRawMtdFrames,
								 final String lavfiMtdKeyName,
								 final Function<Map<String, String>, Optional<T>> transformer) {
		final var tempFrames = new TreeMap<LavfiMtdPosition, T>();

		extractedRawMtdFrames.forEach(raw -> {
			final var rawValues = raw.getValuesByFilterKeysByFilterName().get(lavfiMtdKeyName);
			if (rawValues == null) {
				return;
			}
			final var oValue = transformer.apply(rawValues);
			oValue.ifPresent(v -> tempFrames.put(raw.getLavfiMtdPosition(), v));
		});

		frames = Collections.unmodifiableMap(tempFrames);
	}

}
