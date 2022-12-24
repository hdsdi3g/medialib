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

import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Optional;

import lombok.Data;
import tv.hd3g.fflauncher.filtering.VideoFilterSiti.LavfiMtdSiti;
import tv.hd3g.fflauncher.filtering.lavfimtd.LavfiMtdProgramFrames;
import tv.hd3g.fflauncher.filtering.lavfimtd.LavfiMtdProgramFramesExtractor;
import tv.hd3g.fflauncher.filtering.lavfimtd.LavfiRawMtdFrame;

/**
 * https://ffmpeg.org/ffmpeg-filters.html#siti-1
 */
@Data
public class VideoFilterSiti implements VideoFilterSupplier, LavfiMtdProgramFramesExtractor<LavfiMtdSiti> {

	private boolean printSummary;

	@Override
	public Filter toFilter() {
		final var f = new Filter("siti");
		f.addOptionalArgument("print_summary", printSummary, "1");
		return f;
	}

	/**
	 * frame:119 pts:4966 pts_time:4.966
	 * lavfi.siti.si=11.67
	 * lavfi.siti.ti=5.60
	 */
	@Override
	public LavfiMtdProgramFrames<LavfiMtdSiti> getMetadatas(final List<? extends LavfiRawMtdFrame> extractedRawMtdFrames) {
		return new LavfiMtdProgramFrames<>(extractedRawMtdFrames, "siti",
				rawFrames -> {
					final var si = rawFrames.get("si");
					final var ti = rawFrames.get("ti");
					if (si == null || ti == null) {
						return Optional.empty();
					}
					return Optional.ofNullable(new LavfiMtdSiti(Float.valueOf(si), Float.valueOf(ti)));
				});
	}

	public static LavfiMtdSitiSummary computeSitiStats(final LavfiMtdProgramFrames<LavfiMtdSiti> frames) {
		final var si = frames.getFrames().values().stream().mapToDouble(f -> f.si).summaryStatistics();
		final var ti = frames.getFrames().values().stream().mapToDouble(f -> f.ti).summaryStatistics();
		return new LavfiMtdSitiSummary(si, ti);
	}

	public record LavfiMtdSiti(float si, float ti) {
	}

	public record LavfiMtdSitiSummary(DoubleSummaryStatistics si, DoubleSummaryStatistics ti) {
	}

}
