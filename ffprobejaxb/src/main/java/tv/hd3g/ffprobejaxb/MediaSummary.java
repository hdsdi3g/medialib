/*
 * This file is part of ffprobejaxb.
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
 * Copyright (C) hdsdi3g for hd3g.tv 2023
 *
 */
package tv.hd3g.ffprobejaxb;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.ffmpeg.ffprobe.FormatType;
import org.ffmpeg.ffprobe.StreamDispositionType;
import org.ffmpeg.ffprobe.StreamType;
import org.ffmpeg.ffprobe.TagType;

public record MediaSummary(String format, List<String> streams) {

	static MediaSummary create(final FFprobeJAXB source) {
		final var format = source.getFormat();
		final var entries = new ArrayList<String>();
		entries.add(format.getFormatLongName());
		entries.add(computeDuration(format));
		entries.add(format.getSize() / 1024 / 1024 + " MB");

		if (format.getNbPrograms() > 0) {
			entries.add(format.getNbPrograms() + " program(s)");
		}
		if (source.getChapters().isEmpty() == false) {
			entries.add(source.getChapters().size() + " chapter(s)");
		}

		final var videos = source.getVideoStreams().map(MediaSummary::getVideoSummary);
		final var audios = source.getAudiosStreams().map(MediaSummary::getAudioSummary);
		final var others = computeOther(source);

		return new MediaSummary(
				entries.stream().collect(Collectors.joining(", ")),
				Stream.concat(videos, Stream.concat(audios, others)).toList());
	}

	static Optional<String> getValue(final String value) {
		return Optional.ofNullable(value).flatMap(v -> {
			if (v.isEmpty()) {
				return Optional.empty();
			}
			return Optional.ofNullable(v);
		});
	}

	static void addDisposition(final StreamDispositionType s, final List<String> entries) {
		if (s == null || s.getDefault() == 1 && s.getAttachedPic() == 0) {
			return;
		}
		if (s.getDefault() != 1) {
			entries.add("set not by default");
		}
		if (s.getAttachedPic() != 0) {
			entries.add("attached picture");
		}
	}

	static String getAudioSummary(final StreamType s) {
		final var entries = new ArrayList<String>();

		entries.add(s.getCodecType() + ": " + s.getCodecName());

		getValue(s.getProfile()).ifPresent(entries::add);

		getValue(s.getSampleFmt()).flatMap(f -> {
			if (f.equals("fltp") || s.getCodecName().contains(f)) {
				return Optional.empty();
			}
			return Optional.ofNullable(f);
		}).ifPresent(entries::add);

		if (s.getChannels() > 2) {
			entries.add(s.getChannelLayout() + " (" + s.getChannels() + " channels)");
		} else {
			entries.add(s.getChannelLayout());
		}

		entries.add("@ " + s.getSampleRate() + " Hz");
		entries.add("[" + s.getBitRate() / 1000 + " kbps]");

		addDisposition(s.getDisposition(), entries);
		return entries.stream().collect(Collectors.joining(" "));
	}

	static String getVideoSummary(final StreamType s) {
		final var entries = new ArrayList<String>();

		entries.add(s.getCodecType() + ": " + s.getCodecName());
		entries.add(s.getWidth() + "×" + s.getHeight());

		final var profile = getValue(s.getProfile());
		final var level = s.getLevel();
		if (profile.isPresent()) {
			if (level > 0) {
				entries.add(profile.get() + "/" + level);
			} else {
				entries.add(profile.get());
			}
		} else if (level > 0) {
			entries.add("Level: " + level);
		}

		if (s.getHasBFrames() > 0) {
			entries.add("Has B frames");
		}

		final var frameRate = getValue(s.getAvgFrameRate()).map(b -> {
			final var pos = b.indexOf("/");
			if (pos == -1) {
				return b;
			} else {
				final var l = new BigDecimal(Integer.valueOf(b.substring(0, pos)));
				final var r = new BigDecimal(Integer.valueOf(b.substring(pos + 1)));
				final var df = new DecimalFormat();
				df.setMaximumFractionDigits(3);
				df.setMinimumFractionDigits(0);
				df.setGroupingUsed(false);
				return df.format(l.divide(r).setScale(3));
			}
		}).orElse("?");
		entries.add("@ " + frameRate + " fps");

		final var bitrateKbps = (double) s.getBitRate() / 1000d;
		if (bitrateKbps < 10000) {
			entries.add("[" + Math.round(bitrateKbps) + " kbps]");
		} else {
			entries.add("[" + Math.round(bitrateKbps / 1000) + " Mbps]");
		}

		final var cpf = computePixelsFormat(s);
		if (cpf.isEmpty() == false) {
			entries.add(cpf);
		}

		if (s.getNbFrames() > 0) {
			entries.add("(" + s.getNbFrames() + " frms)");
		}

		addDisposition(s.getDisposition(), entries);
		return entries.stream().collect(Collectors.joining(" "));
	}

	static String computePixelsFormat(final StreamType s) {
		final var entries = new ArrayList<String>();
		getValue(s.getPixFmt()).ifPresent(entries::add);
		getValue(s.getColorRange()).map(v -> "rng:" + v.toUpperCase()).ifPresent(entries::add);

		Stream.concat(getValue(s.getColorSpace()).map(v -> "spce:" + v.toUpperCase()).stream(),
				Stream.concat(
						getValue(s.getColorTransfer()).map(v -> "tsfer:" + v.toUpperCase()).stream(),
						getValue(s.getColorPrimaries()).map(v -> "prim:" + v.toUpperCase()).stream()))
				.distinct()
				.forEach(entries::add);

		return entries.stream().collect(Collectors.joining("/"));
	}

	static void addZeros(final int value, final StringBuilder sbTime) {
		if (value < 10) {
			sbTime.append("0");
		}
		sbTime.append(value);
	}

	static String computeDuration(final FormatType format) {
		final var duration = Duration.ofMillis(Math.round(format.getDuration() * 1000f));
		final var sbTime = new StringBuilder();
		addZeros(duration.toHoursPart(), sbTime);
		sbTime.append(":");
		addZeros(duration.toMinutesPart(), sbTime);
		sbTime.append(":");
		addZeros(duration.toSecondsPart(), sbTime);
		return sbTime.toString();
	}

	private static Stream<String> computeOther(final FFprobeJAXB source) {
		return source.getStreams().stream()
				.filter(Predicate.not(FFprobeJAXB.filterAudioStream))
				.filter(Predicate.not(FFprobeJAXB.filterVideoStream))
				.map(v -> {
					final var name = getValue(v.getCodecName())
							.or(() -> getValue(v.getCodecTagString()))
							.orElse("");
					final var handler = v.getTag().stream()
							.filter(t -> "handler_name".equals(t.getKey()))
							.findFirst()
							.map(TagType::getValue)
							.map(t -> " (" + t + ")")
							.orElse("");

					final var tc = v.getTag().stream()
							.filter(t -> "timecode".equals(t.getKey()))
							.findFirst()
							.map(TagType::getValue)
							.map(t -> " " + t)
							.orElse("");

					return v.getCodecType() + ": " + name + handler + tc;
				});
	}

	@Override
	public String toString() {
		final ToIntFunction<String> computeHeaders = v -> {
			if (v.contains("video: ")) {
				return 0;
			}
			if (v.contains("audio: ")) {
				return 1;
			}
			return 2;
		};
		final ToIntFunction<String> computeBitrate = v -> {
			final var from = v.indexOf("[");
			final var toK = v.indexOf("kbps]");
			final var toM = v.indexOf("Mbps]");
			if (from == -1 || toK == -1 && toM == -1) {
				return 0;
			}
			if (toK > -1) {
				return Integer.valueOf(v.substring(from + 1, toK - 1));
			}
			return Integer.valueOf(v.substring(from + 1, toM - 1)) * 1000;
		};

		final var streamsCollected = streams.stream()
				.collect(groupingBy(s -> s, counting()))
				.entrySet().stream()
				.map(entry -> {
					if (entry.getValue() > 1) {
						return entry.getValue() + "× " + entry.getKey();
					} else {
						return entry.getKey();
					}
				})
				.sorted((l, r) -> {
					final var lV = computeHeaders.applyAsInt(l);
					final var rV = computeHeaders.applyAsInt(r);
					if (lV == rV) {
						return Integer.compare(computeBitrate.applyAsInt(r), computeBitrate.applyAsInt(l));
					}
					return Integer.compare(lV, rV);
				})
				.collect(Collectors.joining(", "));

		return format + ", " + streamsCollected;
	}

}
