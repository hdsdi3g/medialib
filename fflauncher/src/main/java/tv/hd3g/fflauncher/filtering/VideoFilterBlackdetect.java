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
import java.util.List;

import lombok.Data;
import tv.hd3g.fflauncher.filtering.lavfimtd.LavfiMtdProgramEvents;
import tv.hd3g.fflauncher.filtering.lavfimtd.LavfiMtdProgramEventsExtractor;
import tv.hd3g.fflauncher.filtering.lavfimtd.LavfiRawMtdFrame;

/**
 * https://www.ffmpeg.org/ffmpeg-filters.html#blackdetect
 */
@Data
public class VideoFilterBlackdetect implements VideoFilterSupplier, LavfiMtdProgramEventsExtractor {

	private Duration blackMinDuration;
	private float pictureBlackRatioTh;
	private float pixelBlackTh;

	public VideoFilterBlackdetect() {
		pictureBlackRatioTh = -1;
		pixelBlackTh = -1;
	}

	@Override
	public Filter toFilter() {
		final var f = new Filter("blackdetect");
		f.addOptionalDurationSecMsArgument("black_min_duration", blackMinDuration);
		f.addOptionalNonNegativeArgument("picture_black_ratio_th", pictureBlackRatioTh);
		f.addOptionalNonNegativeArgument("pixel_black_th", pixelBlackTh);
		return f;
	}

	/**
	 * frame:0 pts:7 pts_time:0.007
	 * lavfi.black_start=0.007
	 * frame:2 pts:90 pts_time:0.09
	 * lavfi.black_end=0.09
	 * frame:108 pts:4507 pts_time:4.507
	 * lavfi.black_start=4.507
	 * frame:116 pts:4841 pts_time:4.841
	 * lavfi.black_end=4.841
	 */
	@Override
	public LavfiMtdProgramEvents getEvents(final List<? extends LavfiRawMtdFrame> extractedRawMtdFrames) {
		return new LavfiMtdProgramEvents(extractedRawMtdFrames, "black");
	}

}
