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
import tv.hd3g.fflauncher.filtering.VideoFilterBlockdetect.LavfiMtdBlockdetect;
import tv.hd3g.fflauncher.filtering.lavfimtd.LavfiMtdProgramFrames;
import tv.hd3g.fflauncher.filtering.lavfimtd.LavfiMtdProgramFramesExtractor;
import tv.hd3g.fflauncher.filtering.lavfimtd.LavfiRawMtdFrame;

/**
 * https://www.ffmpeg.org/ffmpeg-filters.html#blockdetect-1
 * No thread safe
 */
@Data
public class VideoFilterBlockdetect implements VideoFilterSupplier,
									LavfiMtdProgramFramesExtractor<LavfiMtdBlockdetect> {

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

	/**
	 * frame:111 pts:4632 pts_time:4.632
	 * lavfi.block=2.204194
	 */
	@Override
	public LavfiMtdProgramFrames<LavfiMtdBlockdetect> getMetadatas(final List<? extends LavfiRawMtdFrame> extractedRawMtdFrames) {
		return new LavfiMtdProgramFrames<>(extractedRawMtdFrames, "block",
				rawFrames -> Optional.ofNullable(rawFrames.get(DEFAULT_KEY))
						.map(Float::parseFloat)
						.map(LavfiMtdBlockdetect::new));
	}

	public record LavfiMtdBlockdetect(float block) {
	}

}
