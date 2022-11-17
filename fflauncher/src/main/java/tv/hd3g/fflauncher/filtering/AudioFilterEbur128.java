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
package tv.hd3g.fflauncher.filtering;

import java.awt.Point;
import java.util.Optional;
import java.util.Set;

import lombok.Data;

/**
 * https://www.ffmpeg.org/ffmpeg-filters.html#ebur128-1
 */
@Data
public class AudioFilterEbur128 implements FilterSupplier {

	public enum Framelog {
		QUIET,
		INFO,
		VERBOSE;

		@Override
		public String toString() {
			return name().toLowerCase();
		}

	}

	public enum Peak {
		NONE,
		SAMPLE,
		TRUE;

		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}

	public enum Gauge {
		MOMENTARY,
		SHORTTERM;

		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}

	public enum Scale {
		ABSOLUTE,
		RELATIVE;

		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}

	private boolean video;
	private Point size;
	private int meter;
	private boolean metadata;
	private Framelog framelog;
	private Set<Peak> peakMode;
	private boolean dualmono;
	private float panlaw;
	private int target;
	private Gauge gauge;
	private Scale scale;

	public AudioFilterEbur128() {
		meter = -1;
		panlaw = Float.MIN_VALUE;
		target = Integer.MIN_VALUE;
	}

	@Override
	public Filter toFilter() {
		final var f = new Filter("ebur128");
		f.addOptionalArgument("video", video, "1");
		f.addArgument("size", Optional.ofNullable(size).map(s -> s.x + "x" + s.y));
		f.addOptionalNonNegativeArgument("meter", meter);
		f.addOptionalArgument("metadata", metadata, "1");
		f.addOptionalArgument("dualmono", dualmono, "true");
		f.addOptionalArgument("target", target > Integer.MIN_VALUE, String.valueOf(target));
		f.addOptionalArgument("framelog", framelog);
		f.addOptionalArgument("gauge", gauge);
		f.addOptionalArgument("scale", scale);
		f.addOptionalArgument("panlaw", panlaw > Float.MIN_VALUE, String.valueOf(panlaw));
		if (peakMode != null) {
			f.addArgument("peak", peakMode, "+");
		}
		return f;
	}

}
