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
package tv.hd3g.fflauncher.progress;

import static java.util.stream.Collectors.toUnmodifiableMap;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

public class ProgressBlock {

	private final Map<String, String> items;

	/**
	 * CAN BE
	 * [frame=619,
	 * fps=30.39,
	 * stream_0_0_q=-0.0,
	 * bitrate=N/A,
	 * total_size=N/A,
	 * out_time_us=20633313,
	 * out_time_ms=20633313,
	 * out_time=00:00:20.633313,
	 * dup_frames=0,
	 * drop_frames=0,
	 * speed=1.01x,
	 * progress=end]
	 * =======
	 * OR JUST
	 * =======
	 * [bitrate=N/A,
	 * total_size=N/A,
	 * out_time_us=1582993,
	 * out_time_ms=1582993,
	 * out_time=00:00:01.582993,
	 * dup_frames=0,
	 * drop_frames=0,
	 * speed=1.58x,
	 * progress=continue]
	 */
	ProgressBlock(final List<String> lines) {
		final var countTo = lines.get(lines.size() - 1).startsWith("progress=") ? lines.size() - 1 : lines.size();
		final var lastIndexOfProgress = IntStream.range(0, countTo)
				.filter(pos -> lines.get(pos).startsWith("progress="))
				.reduce((l, r) -> r)
				.orElse(-1);

		items = lines.stream().skip(lastIndexOfProgress + 1l)
				.collect(toUnmodifiableMap(this::splitLeft, this::splitRight));
	}

	private String splitLeft(final String line) {
		final var pos = line.indexOf("=");
		if (pos < 1) {
			throw new IllegalArgumentException("Invalid entry: \"" + line + "\"");
		}
		return line.substring(0, pos);
	}

	private String splitRight(final String line) {
		final var pos = line.indexOf("=");
		if (pos + 1 == line.length()) {
			throw new IllegalArgumentException("Invalid entry: \"" + line + "\"");
		}
		return line.substring(pos + 1, line.length());
	}

	public boolean isEnd() {
		return items.getOrDefault("progress", "continue").equals("end");
	}

	public Optional<Integer> getFrame() {
		return Optional.ofNullable(items.get("frame")).map(Integer::valueOf);
	}

	public Optional<Float> getFPS() {
		return Optional.ofNullable(items.get("fps")).map(Float::valueOf);
	}

	public Optional<Float> getBitrate() {
		return Optional.ofNullable(items.get("bitrate"))
				.filter(b -> b.equalsIgnoreCase("N/A") == false)
				.map(Float::valueOf);
	}

	public Optional<Long> getTotalSize() {
		return Optional.ofNullable(items.get("total_size"))
				.filter(b -> b.equalsIgnoreCase("N/A") == false)
				.map(Long::valueOf);
	}

	public int getDupFrames() {
		return Integer.valueOf(items.getOrDefault("dup_frames", "0"));
	}

	public int getDropFrames() {
		return Integer.valueOf(items.getOrDefault("drop_frames", "0"));
	}

	public Float getSpeedX() {
		final var speed = items.getOrDefault("speed", "0");
		if (speed.toLowerCase().endsWith("x")) {
			return Float.valueOf(speed.substring(0, speed.length() - 1));
		}
		return Float.valueOf(speed);
	}

	public long getOutTimeUs() {
		return Long.valueOf(items.getOrDefault("out_time_us", "0"));
	}

	public Duration getOutTimeMs() {
		return Duration.ofMillis(Long.valueOf(items.getOrDefault("out_time_ms", "0")));
	}

	public String getOutTime() {
		return items.getOrDefault("out_time", "00:00:00.000000");
	}

	/**
	 * stream_0_0_q=-0.0
	 */
	private record EntryStreamQ(String k, Float v) {
		public EntryStreamQ(final String k, final String v) {
			this(k.substring("stream_".length(),
					k.lastIndexOf("_")), Float.valueOf(v));
		}
	}

	public Map<String, Float> getStreamQ() {
		return items.keySet().stream()
				.filter(k -> k.startsWith("stream_"))
				.map(k -> new EntryStreamQ(k, items.get(k)))
				.collect(toUnmodifiableMap(EntryStreamQ::k, EntryStreamQ::v));
	}

	@Override
	public String toString() {
		final var builder = new StringBuilder();
		builder.append("ProgressBlock [items=");
		builder.append(items);
		builder.append("]");
		return builder.toString();
	}

}
