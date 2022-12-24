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
import tv.hd3g.fflauncher.filtering.VideoFilterIdet.LavfiMtdIdet;
import tv.hd3g.fflauncher.filtering.lavfimtd.LavfiMtdProgramFrames;
import tv.hd3g.fflauncher.filtering.lavfimtd.LavfiMtdProgramFramesExtractor;
import tv.hd3g.fflauncher.filtering.lavfimtd.LavfiRawMtdFrame;

/**
 * https://www.ffmpeg.org/ffmpeg-filters.html#idet
 * No thread safe
 */
@Data
public class VideoFilterIdet implements VideoFilterSupplier, LavfiMtdProgramFramesExtractor<LavfiMtdIdet> {

	private float intlThres;
	private float progThres;
	private float repThres;
	private int halfLife;
	private boolean analyzeInterlacedFlag;

	public VideoFilterIdet() {
		intlThres = -1;
		progThres = -1;
		repThres = -1;
		halfLife = -1;
	}

	@Override
	public Filter toFilter() {
		final var f = new Filter("idet");
		f.addOptionalArgument("analyze_interlaced_flag", analyzeInterlacedFlag, "1");
		f.addOptionalNonNegativeArgument("intl_thres", intlThres);
		f.addOptionalNonNegativeArgument("prog_thres", progThres);
		f.addOptionalNonNegativeArgument("half_life", halfLife);
		return f;
	}

	/**
	 * frame:119 pts:4966 pts_time:4.966
	 * -
	 * lavfi.idet.repeated.current_frame=neither
	 * lavfi.idet.repeated.neither=115.00
	 * lavfi.idet.repeated.top=2.00
	 * lavfi.idet.repeated.bottom=3.00
	 * -
	 * lavfi.idet.single.current_frame=progressive
	 * lavfi.idet.single.tff=0.00
	 * lavfi.idet.single.bff=0.00
	 * lavfi.idet.single.progressive=40.00
	 * lavfi.idet.single.undetermined=80.00
	 * -
	 * lavfi.idet.multiple.current_frame=progressive
	 * lavfi.idet.multiple.tff=0.00
	 * lavfi.idet.multiple.bff=0.00
	 * lavfi.idet.multiple.progressive=120.00
	 * lavfi.idet.multiple.undetermined=0.00
	 */
	@Override
	public LavfiMtdProgramFrames<LavfiMtdIdet> getMetadatas(final List<? extends LavfiRawMtdFrame> extractedRawMtdFrames) {
		return new LavfiMtdProgramFrames<>(extractedRawMtdFrames, "idet",
				rawFrames -> {
					try {
						final var single = new LavfiMtdIdetFrame(
								LavfiMtdIdetSingleFrameType.valueOf(
										rawFrames.get("single.current_frame").toUpperCase()),
								parseInt(rawFrames.get("single.tff")),
								parseInt(rawFrames.get("single.bff")),
								parseInt(rawFrames.get("single.progressive")),
								parseInt(rawFrames.get("single.undetermined")));
						final var multiple = new LavfiMtdIdetFrame(
								LavfiMtdIdetSingleFrameType.valueOf(
										rawFrames.get("multiple.current_frame").toUpperCase()),
								parseInt(rawFrames.get("multiple.tff")),
								parseInt(rawFrames.get("multiple.bff")),
								parseInt(rawFrames.get("multiple.progressive")),
								parseInt(rawFrames.get("multiple.undetermined")));
						final var repeated = new LavfiMtdIdetRepeatedFrame(
								LavfiMtdIdetRepeatedFrameType.valueOf(
										rawFrames.get("repeated.current_frame").toUpperCase()),
								parseInt(rawFrames.get("repeated.neither")),
								parseInt(rawFrames.get("repeated.top")),
								parseInt(rawFrames.get("repeated.bottom")));
						return Optional.ofNullable(new LavfiMtdIdet(single, multiple, repeated));
					} catch (final NullPointerException e) {
						return Optional.empty();
					}

				});
	}

	public enum LavfiMtdIdetSingleFrameType {
		/** top field first */
		TFF,
		/** bottom field first */
		BFF,
		PROGRESSIVE,
		UNDETERMINED;
	}

	public enum LavfiMtdIdetRepeatedFrameType {
		TOP,
		BOTTOM,
		NEITHER;
	}

	public record LavfiMtdIdetRepeatedFrame(LavfiMtdIdetRepeatedFrameType currentFrame,
											int neither,
											int top,
											int bottom) {
	}

	public record LavfiMtdIdetFrame(LavfiMtdIdetSingleFrameType currentFrame,
									/** top field first */
									int tff,
									/** bottom field first */
									int bff,
									int progressive,
									int undetermined) {
	}

	public record LavfiMtdIdet(LavfiMtdIdetFrame single,
							   LavfiMtdIdetFrame multiple,
							   LavfiMtdIdetRepeatedFrame repeated) {
	}

}
