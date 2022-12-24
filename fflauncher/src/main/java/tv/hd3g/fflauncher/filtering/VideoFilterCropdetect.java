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

import java.util.List;
import java.util.Optional;

import lombok.Data;
import tv.hd3g.fflauncher.filtering.VideoFilterCropdetect.LavfiMtdCropdetect;
import tv.hd3g.fflauncher.filtering.lavfimtd.LavfiMtdProgramFrames;
import tv.hd3g.fflauncher.filtering.lavfimtd.LavfiMtdProgramFramesExtractor;
import tv.hd3g.fflauncher.filtering.lavfimtd.LavfiRawMtdFrame;

/**
 * https://www.ffmpeg.org/ffmpeg-filters.html#cropdetect
 * No thread safe
 */
@Data
public class VideoFilterCropdetect implements VideoFilterSupplier, LavfiMtdProgramFramesExtractor<LavfiMtdCropdetect> {

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

	/**
	 * frame:1801 pts:60041 pts_time:60.041
	 * lavfi.cropdetect.x1=0
	 * lavfi.cropdetect.x2=479
	 * lavfi.cropdetect.y1=0
	 * lavfi.cropdetect.y2=479
	 * lavfi.cropdetect.w=480
	 * lavfi.cropdetect.h=480
	 * lavfi.cropdetect.x=0
	 * lavfi.cropdetect.y=0
	 * frame:7616 pts:317340 pts_time:317.34
	 * lavfi.cropdetect.x1=0
	 * lavfi.cropdetect.x2=1919
	 * lavfi.cropdetect.y1=0
	 * lavfi.cropdetect.y2=1079
	 * lavfi.cropdetect.w=1920
	 * lavfi.cropdetect.h=1072
	 * lavfi.cropdetect.x=0
	 * lavfi.cropdetect.y=4
	 */
	@Override
	public LavfiMtdProgramFrames<LavfiMtdCropdetect> getMetadatas(final List<? extends LavfiRawMtdFrame> extractedRawMtdFrames) {
		return new LavfiMtdProgramFrames<>(extractedRawMtdFrames, "cropdetect", rawFrames -> {
			try {
				final var x1 = Integer.parseInt(rawFrames.get("x1"));
				final var x2 = Integer.parseInt(rawFrames.get("x2"));
				final var y1 = Integer.parseInt(rawFrames.get("y1"));
				final var y2 = Integer.parseInt(rawFrames.get("y2"));
				final var w = Integer.parseInt(rawFrames.get("w"));
				final var h = Integer.parseInt(rawFrames.get("h"));
				final var x = Integer.parseInt(rawFrames.get("x"));
				final var y = Integer.parseInt(rawFrames.get("y"));
				return Optional.ofNullable(new LavfiMtdCropdetect(x1, x2, y1, y2, w, h, x, y));
			} catch (final NullPointerException e) {
				return Optional.empty();
			}
		});
	}

	public record LavfiMtdCropdetect(int x1, int x2, int y1, int y2, int w, int h, int x, int y) {
	}

}
