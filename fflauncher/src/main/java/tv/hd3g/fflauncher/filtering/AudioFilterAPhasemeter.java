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

import java.time.Duration;

import lombok.Data;

/**
 * https://www.ffmpeg.org/ffmpeg-filters.html#phasing-detection
 */
@Data
public class AudioFilterAPhasemeter implements AudioFilterSupplier {
	public static final String APHASEMETER = "aphasemeter";

	private boolean phasing;
	private boolean video;
	private float tolerance;
	private int angle;
	private Duration duration;

	public AudioFilterAPhasemeter() {
		tolerance = -1;
		angle = -1;
	}

	@Override
	public Filter toFilter() {
		final var f = new Filter(APHASEMETER);
		f.addOptionalArgument("phasing", phasing, "1");
		f.addOptionalDurationSecArgument("duration", duration);
		f.addOptionalNonNegativeArgument("tolerance", tolerance);
		f.addOptionalNonNegativeArgument("angle", angle);
		if (video == false) {
			f.addArgument("video", "0");
		}
		return f;
	}

}
