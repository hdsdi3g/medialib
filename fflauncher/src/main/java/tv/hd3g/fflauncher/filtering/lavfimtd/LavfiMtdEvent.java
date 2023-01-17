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

import java.time.Duration;

/**
 * @param scope can be channel name/pos, or null
 */
public record LavfiMtdEvent(String name, String scope, Duration start, Duration end)
						   implements Comparable<LavfiMtdEvent> {

	LavfiMtdEvent(final String name, final String scope, final float start, final float end) {
		this(name, scope, secFloatToDuration(start), secFloatToDuration(end));
	}

	LavfiMtdEvent(final String name, final String scope, final Duration start, final float end) {
		this(name, scope, start, secFloatToDuration(end));
	}

	private static Duration secFloatToDuration(final float value) {
		return Duration.ofMillis(Math.round(value * 1000f));
	}

	@Override
	public int compareTo(final LavfiMtdEvent o) {
		return start.compareTo(o.start);
	}

}
