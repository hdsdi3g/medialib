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
 * https://www.ffmpeg.org/ffmpeg-filters.html#blockdetect-1
 * No thread safe
 */
@Data
public class VideoFilterBlockdetect implements FilterSupplier {

	private int periodMin;
	private int periodMax;
	private int planes;

	public VideoFilterBlockdetect() {
		periodMin = -1;
		periodMax = -1;
		planes = -1;
	}

	@Override
	public Filter toFilter() {
		final var f = new Filter("blockdetect");
		f.addOptionalNonNegativeArgument("period_min", periodMin);
		f.addOptionalNonNegativeArgument("period_max", periodMax);
		f.addOptionalNonNegativeArgument("planes", planes);
		return f;
	}

}
