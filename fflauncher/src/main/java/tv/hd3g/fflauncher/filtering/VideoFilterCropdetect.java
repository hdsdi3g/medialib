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

import lombok.Data;

/**
 * https://www.ffmpeg.org/ffmpeg-filters.html#cropdetect
 * No thread safe
 */
@Data
public class VideoFilterCropdetect implements FilterSupplier {

	public enum Mode {
		BLACK,
		/**
		 * You should use "-flags2 +export_mvs" before "-i file" with this mode.
		 * OR "mestimate" filter before this filter.
		 */
		MVEDGES;

		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}

	private final Mode mode;
	private int limit;
	private int round;
	private int skip;
	private int reset;
	private int mvThreshold;
	private int low;
	private int high;

	/**
	 * set -1 to default
	 */
	public VideoFilterCropdetect(final Mode mode) {
		this.mode = mode;
		limit = -1;
		round = -1;
		skip = -1;
		reset = -1;
		mvThreshold = -1;
		low = -1;
		high = -1;
	}

	@Override
	public Filter toFilter() {
		final var f = new Filter("cropdetect", new FilterArgument("mode", mode));
		f.addOptionalNonNegativeArgument("limit", limit);
		f.addOptionalNonNegativeArgument("round", round);
		f.addOptionalNonNegativeArgument("skip", skip);
		f.addOptionalNonNegativeArgument("reset", reset);
		f.addOptionalNonNegativeArgument("mv_threshold", mvThreshold);
		f.addOptionalNonNegativeArgument("low", low);
		f.addOptionalNonNegativeArgument("high", high);
		return f;
	}

}
