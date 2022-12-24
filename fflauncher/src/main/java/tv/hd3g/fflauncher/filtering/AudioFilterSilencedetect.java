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
 * https://www.ffmpeg.org/ffmpeg-filters.html#silencedetect
 * lavfi.silence_start/lavfi.silence_start.X
 * lavfi.silence_duration/lavfi.silence_duration.X,
 * lavfi.silence_end/lavfi.silence_end.X
 */
@Data
public class AudioFilterSilencedetect implements AudioFilterSupplier, LavfiMtdProgramEventsExtractor {

	private boolean mono;
	private Duration duration;
	private int noiseDb;
	private float noiseRatio;

	public AudioFilterSilencedetect() {
		noiseDb = -1;
		noiseRatio = -1;
	}

	@Override
	public Filter toFilter() {
		final var f = new Filter("silencedetect");
		f.addOptionalArgument("mono", mono, "1");
		f.addOptionalDurationSecArgument("duration", duration);
		f.addOptionalNonNegativeArgument("noise", noiseDb);
		f.addOptionalNonNegativeArgument("noise", noiseRatio);
		return f;
	}

	/**
	 * frame:3306 pts:3173808 pts_time:66.121
	 * lavfi.silence_start.1=65.1366
	 * lavfi.silence_start.2=65.1366
	 * frame:3332 pts:3198768 pts_time:66.641
	 * lavfi.silence_end.1=66.6474
	 * lavfi.silence_duration.1=1.51079
	 * lavfi.silence_end.2=66.6474
	 * lavfi.silence_duration.2=1.51079 *
	 */
	@Override
	public LavfiMtdProgramEvents getEvents(final List<? extends LavfiRawMtdFrame> extractedRawMtdFrames) {
		return new LavfiMtdProgramEvents(extractedRawMtdFrames, "silence");
	}

}
