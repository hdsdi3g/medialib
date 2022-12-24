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

import static java.util.stream.Collectors.toUnmodifiableMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import lombok.Data;
import tv.hd3g.fflauncher.filtering.AudioFilterAstats.LavfiMtdAstats;
import tv.hd3g.fflauncher.filtering.lavfimtd.LavfiMtdProgramFrames;
import tv.hd3g.fflauncher.filtering.lavfimtd.LavfiMtdProgramFramesExtractor;
import tv.hd3g.fflauncher.filtering.lavfimtd.LavfiRawMtdFrame;

/**
 * https://www.ffmpeg.org/ffmpeg-filters.html#astats-1
 * No thread safe
 * lavfi.astats.*
 */
@Data
public class AudioFilterAstats implements AudioFilterSupplier, LavfiMtdProgramFramesExtractor<LavfiMtdAstats> {

	private int length;
	private String metadata;
	private int reset;
	private String measurePerchannel;
	private String measureOverall;

	public AudioFilterAstats() {
		length = -1;
		reset = -1;
	}

	/**
	 * This implementation choose only some values added here (others are redundant or not really usefull in audio).
	 * Each added variable weighed down the overall data result.
	 * @return this
	 */
	public AudioFilterAstats setSelectedMetadatas() {
		metadata = "1";
		measurePerchannel = "DC_offset+Peak_level+Flat_factor+Peak_count+Noise_floor+Noise_floor_count+Entropy";
		measureOverall = "none";
		return this;
	}

	@Override
	public Filter toFilter() {
		final var f = new Filter("astats");
		f.addOptionalNonNegativeArgument("length", length);
		f.addOptionalNonNegativeArgument("reset", reset);
		f.addOptionalArgument("metadata", metadata);
		f.addOptionalArgument("measure_perchannel", measurePerchannel);
		f.addOptionalArgument("measure_overall", measureOverall);
		return f;
	}

	/**
	 * frame:87883 pts:84367728 pts_time:1757.66
	 * lavfi.astats.1.DC_offset=0.000001
	 * lavfi.astats.1.Peak_level=-0.622282
	 * lavfi.astats.1.Flat_factor=0.000000
	 * lavfi.astats.1.Peak_count=2.000000
	 * lavfi.astats.1.Noise_floor=-78.266739
	 * lavfi.astats.1.Noise_floor_count=708.000000
	 * lavfi.astats.1.Entropy=0.788192
	 * lavfi.astats.2.DC_offset=0.000002
	 * lavfi.astats.2.Peak_level=-0.622282
	 * lavfi.astats.2.Flat_factor=0.000000
	 * lavfi.astats.2.Peak_count=2.000000
	 * lavfi.astats.2.Noise_floor=-78.266739
	 * lavfi.astats.2.Noise_floor_count=1074.000000
	 * lavfi.astats.2.Entropy=0.788152
	 */
	@Override
	public LavfiMtdProgramFrames<LavfiMtdAstats> getMetadatas(final List<? extends LavfiRawMtdFrame> extractedRawMtdFrames) {
		return new LavfiMtdProgramFrames<>(extractedRawMtdFrames, "astats", rawFrames -> {// NOSONAR S5612
			final var channelsContent = new ArrayList<Map<String, String>>();

			rawFrames.entrySet().forEach(entry -> {
				final var keyLine = entry.getKey();
				final var channelId = Integer.valueOf(keyLine.substring(0, keyLine.indexOf("."))) - 1;
				final var key = keyLine.substring(keyLine.indexOf(".") + 1, keyLine.length());

				while (channelId >= channelsContent.size()) {
					channelsContent.add(new HashMap<>());
				}
				channelsContent.get(channelId).put(key, entry.getValue());
			});

			final var channels = channelsContent.stream()
					.map(content -> {
						final var dcOffset = Optional.ofNullable(content.remove("DC_offset"))
								.map(Float::valueOf)
								.orElse(Float.NaN);
						final var peakLevel = Optional.ofNullable(content.remove("Peak_level"))
								.map(Float::valueOf)
								.orElse(Float.NaN);
						final var flatFactor = Optional.ofNullable(content.remove("Flat_factor"))
								.map(Float::valueOf)
								.orElse(Float.NaN);
						final var peakCount = Optional.ofNullable(content.remove("Peak_count"))
								.map(this::parseLong)
								.orElse(0L);
						final var noiseFloor = Optional.ofNullable(content.remove("Noise_floor"))
								.map(Float::valueOf)
								.orElse(Float.NaN);
						final var noiseFloorCount = Optional.ofNullable(content.remove("Noise_floor_count"))
								.map(this::parseLong)
								.orElse(0L);
						final var entropy = Optional.ofNullable(content.remove("Entropy"))
								.map(Float::valueOf)
								.orElse(Float.NaN);
						final var other = content.entrySet().stream()
								.collect(toUnmodifiableMap(Entry::getKey, entry -> Float.valueOf(entry.getValue())));

						return new LavfiMtdAstatsChannel(
								dcOffset,
								peakLevel,
								flatFactor,
								peakCount,
								noiseFloor,
								noiseFloorCount,
								entropy,
								other);
					})
					.toList();

			if (channels.isEmpty()) {
				return Optional.empty();
			}

			return Optional.ofNullable(new LavfiMtdAstats(channels));
		});
	}

	public record LavfiMtdAstatsChannel(
										/** Mean amplitude displacement from zero. */
										float dcOffset,
										/** Standard peak measured in dBFS */
										float peakLevel,
										/** Flatness (i.e. consecutive samples with the same value) of the signal at its peak levels (i.e. either Min level or Max level) */
										float flatFactor,
										/** Number of occasions (not the number of samples) that the signal attained either Min level or Max level. */
										long peakCount,
										/** Minimum local peak measured in dBFS over a short window. */
										float noiseFloor,
										/** Number of occasions (not the number of samples) that the signal attained Noise floor. */
										long noiseFloorCount,
										/** Entropy measured across whole audio. Entropy of value near 1.0 is typically measured for white noise. */
										float entropy,
										Map<String, Float> other) {
	}

	public record LavfiMtdAstats(
								 /** #0 is the first channel */
								 List<LavfiMtdAstatsChannel> channels) {

	}

}
