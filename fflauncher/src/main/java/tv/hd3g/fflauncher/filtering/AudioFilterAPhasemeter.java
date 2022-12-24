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
import java.util.Optional;

import lombok.Data;
import tv.hd3g.fflauncher.filtering.AudioFilterAPhasemeter.LavfiMtdAPhaseMeter;
import tv.hd3g.fflauncher.filtering.lavfimtd.LavfiMtdProgramEvents;
import tv.hd3g.fflauncher.filtering.lavfimtd.LavfiMtdProgramEventsExtractor;
import tv.hd3g.fflauncher.filtering.lavfimtd.LavfiMtdProgramFrames;
import tv.hd3g.fflauncher.filtering.lavfimtd.LavfiMtdProgramFramesExtractor;
import tv.hd3g.fflauncher.filtering.lavfimtd.LavfiRawMtdFrame;

/**
 * https://www.ffmpeg.org/ffmpeg-filters.html#phasing-detection
 */
@Data
public class AudioFilterAPhasemeter implements AudioFilterSupplier,
									LavfiMtdProgramFramesExtractor<LavfiMtdAPhaseMeter>,
									LavfiMtdProgramEventsExtractor {
	private static final String APHASEMETER = "aphasemeter";

	private boolean phasing;
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
		return f;
	}

	/**
	 * frame:1022 pts:981168 pts_time:20.441
	 * lavfi.aphasemeter.phase=1.000000
	 * lavfi.aphasemeter.mono_start=18.461
	 * frame:1299 pts:1247088 pts_time:25.981
	 * lavfi.aphasemeter.phase=0.992454
	 * lavfi.aphasemeter.mono_end=25.981
	 * lavfi.aphasemeter.mono_duration=2.94
	 */
	@Override
	public LavfiMtdProgramFrames<LavfiMtdAPhaseMeter> getMetadatas(final List<? extends LavfiRawMtdFrame> extractedRawMtdFrames) {
		return new LavfiMtdProgramFrames<>(extractedRawMtdFrames, APHASEMETER,
				rawFrames -> Optional.ofNullable(rawFrames.get("phase"))
						.map(Float::parseFloat)
						.map(LavfiMtdAPhaseMeter::new));
	}

	public record LavfiMtdAPhaseMeter(float phase) {
	}

	@Override
	public LavfiMtdProgramEvents getEvents(final List<? extends LavfiRawMtdFrame> extractedRawMtdFrames) {
		return new LavfiMtdProgramEvents(extractedRawMtdFrames, APHASEMETER, "mono");
	}

}
