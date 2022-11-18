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
 * https://www.ffmpeg.org/ffmpeg-filters.html#freezedetect
 * lavfi.freezedetect.freeze_start
 * lavfi.freezedetect.freeze_duration
 * lavfi.freezedetect.freeze_end
 * No thread safe
 */
@Data
public class VideoFilterFreezedetect implements VideoFilterSupplier {

	private float noiseToleranceRatio;
	private int noiseToleranceDb;
	private Duration freezeDuration;

	public VideoFilterFreezedetect() {
		noiseToleranceRatio = -1;
		noiseToleranceDb = Integer.MIN_VALUE;
	}

	@Override
	public Filter toFilter() {
		final var f = new Filter("freezedetect");
		f.addOptionalDurationSecArgument("duration", freezeDuration);
		f.addOptionalArgument("noise", noiseToleranceDb > Integer.MIN_VALUE, "-" + Math.abs(noiseToleranceDb) + "dB");
		f.addOptionalNonNegativeArgument("noise", noiseToleranceRatio);
		return f;
	}

}
