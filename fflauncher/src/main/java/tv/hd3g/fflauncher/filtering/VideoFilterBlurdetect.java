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

import static tv.hd3g.fflauncher.filtering.lavfimtd.LavfiRawMtdFrame.DEFAULT_KEY;

import java.util.List;
import java.util.Optional;

import lombok.Data;
import tv.hd3g.fflauncher.filtering.VideoFilterBlurdetect.LavfiMtdBlurdetect;
import tv.hd3g.fflauncher.filtering.lavfimtd.LavfiMtdProgramFrames;
import tv.hd3g.fflauncher.filtering.lavfimtd.LavfiMtdProgramFramesExtractor;
import tv.hd3g.fflauncher.filtering.lavfimtd.LavfiRawMtdFrame;

/**
 * https://www.ffmpeg.org/ffmpeg-filters.html#blurdetect-1
 * No thread safe
 */
@Data
public class VideoFilterBlurdetect implements VideoFilterSupplier, LavfiMtdProgramFramesExtractor<LavfiMtdBlurdetect> {

	private float low;
	private float high;
	private int radius;
	private float blockPct;
	private float blockWidth;
	private float blockHeight;
	private int planes;

	public VideoFilterBlurdetect() {
		low = -1;
		high = -1;
		radius = -1;
		blockPct = -1;
		blockWidth = -1;
		blockHeight = -1;
		planes = -1;
	}

	@Override
	public Filter toFilter() {
		final var f = new Filter("blurdetect");
		f.addOptionalNonNegativeArgument("low", low);
		f.addOptionalNonNegativeArgument("high", high);
		f.addOptionalNonNegativeArgument("radius", radius);
		f.addOptionalNonNegativeArgument("block_pct", blockPct);
		f.addOptionalNonNegativeArgument("block_width", blockWidth);
		f.addOptionalNonNegativeArgument("block_height", blockHeight);
		f.addOptionalNonNegativeArgument("planes", planes);
		return f;
	}

	/**
	 * frame:114 pts:4757 pts_time:4.757
	 * lavfi.blur=5.744382
	 */
	@Override
	public LavfiMtdProgramFrames<LavfiMtdBlurdetect> getMetadatas(final List<? extends LavfiRawMtdFrame> extractedRawMtdFrames) {
		return new LavfiMtdProgramFrames<>(extractedRawMtdFrames, "blur",
				rawFrames -> Optional.ofNullable(rawFrames.get(DEFAULT_KEY))
						.map(Float::parseFloat)
						.map(LavfiMtdBlurdetect::new));
	}

	public record LavfiMtdBlurdetect(float blur) {
	}

}
