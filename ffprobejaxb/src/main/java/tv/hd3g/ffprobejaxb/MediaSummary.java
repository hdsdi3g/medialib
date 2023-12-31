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
import static java.util.stream.Collectors.joining;
import static tv.hd3g.ffprobejaxb.FFprobeReference.filterAudioStream;
import static tv.hd3g.ffprobejaxb.FFprobeReference.filterVideoStream;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import tv.hd3g.ffprobejaxb.data.FFProbeFormat;
import tv.hd3g.ffprobejaxb.data.FFProbeKeyValue;
import tv.hd3g.ffprobejaxb.data.FFProbeStream;

public record MediaSummary(String format, List<String> streams) {
	private static final DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);

	static MediaSummary create(final FFprobeJAXB source) {
		final var format = source.getFormat()
				.orElseThrow(() -> new IllegalArgumentException("Can't found FFprobe format to produce Summary"));
		final var chapters = source.getChapters();

		final var entries = new ArrayList<String>();
		entries.add(format.formatLongName());
		entries.add(computeDuration(format));
		source.getTimecode(true)
				.map(t -> "TCIN: " + t)
				.ifPresent(entries::add);

		if (format.size() <= 1024 * 1024) {
			entries.add(format.size() + " bytes");
		} else {
			entries.add(format.size() / 1024 / 1024 + " MB");
		}

		if (format.nbPrograms() > 0) {
			entries.add(format.nbPrograms() + " program" +
						(format.nbPrograms() > 1 ? "s" : ""));
		}
		if (chapters.isEmpty() == false) {
			entries.add(chapters.size() + " chapter"
						+ (chapters.size() > 1 ? "s" : ""));
		}

		if (source.getVideoStreams().anyMatch(f -> f.bitRate() > 0) == false
			&& source.getAudioStreams().anyMatch(f -> f.bitRate() > 0) == false) {
			Optional.ofNullable(format.bitRate()).ifPresent(b -> {
				final var bitrateKbps = (double) b / 1000d;
				if (bitrateKbps < 10000) {
					entries.add(Math.round(bitrateKbps) + " kbps");
				} else {
					entries.add(Math.round(bitrateKbps / 1000) + " Mbps");
				}
			});
		}

		final var videos = source.getVideoStreams().map(MediaSummary::getVideoSummary);
		final var audios = source.getAudioStreams().map(MediaSummary::getAudioSummary);
		final var others = computeOther(source);

		return new MediaSummary(
				entries.stream().collect(Collectors.joining(", ")),
				Stream.of(videos, audios, others)
						.flatMap(s -> s)
						.toList());
	}

	static Optional<String> getValue(final String value) {
		return Optional.ofNullable(value).flatMap(v -> {
			if (v.isEmpty()) {
				return Optional.empty();
			}
			return Optional.ofNullable(v);
		});
	}

	static String getAudioSummary(final FFProbeStream s) {
		final var entries = new ArrayList<String>();

		entries.add(s.codecType() + ": " + s.codecName());

		getValue(s.profile()).ifPresent(entries::add);

		getValue(s.sampleFmt()).flatMap(f -> {
			if (f.equals("fltp") || f.equals("s16p") || s.codecName().contains(f)) {
				return Optional.empty();
			}
			return Optional.ofNullable(f);
		}).ifPresent(entries::add);

		if (s.channels() > 2) {
			if (s.channelLayout() != null) {
				entries.add(s.channelLayout() + " (" + s.channels() + " channels)");
			} else {
				entries.add(s.channels() + " channels");
			}
		} else if (s.channelLayout() != null) {
			entries.add(s.channelLayout());
		} else if (s.channels() == 2) {
			entries.add("2 channels");
		} else {
			entries.add("mono");
		}

		Optional.ofNullable(s.sampleRate())
				.ifPresent(sr -> entries.add("@ " + sr + " Hz"));

		Optional.ofNullable(s.bitRate())
				.filter(b -> b > 100)
				.ifPresent(b -> entries.add("[" + b / 1000 + " kbps]"));

		final var dispositions = s.disposition().resumeDispositions().collect(joining(", "));
		if (dispositions.isEmpty() == false) {
			entries.add(dispositions);
		}
		return entries.stream().collect(joining(" "));
	}

	static String getVideoSummary(final FFProbeStream s) {
		final var entries = new ArrayList<String>();

		entries.add(s.codecType() + ": " + s.codecName());

		if (s.width() > 0 && s.height() > 0) {
			entries.add(s.width() + "×" + s.height());
		}

		final var profile = getValue(s.profile()).filter(p -> p.equals("0") == false);
		final var level = Optional.ofNullable(s.level()).orElse(0);
		if (profile.isPresent()) {
			if (level > 0) {
				entries.add(profile.get() + "/" + getLevelTag(s.codecName(), level));
			} else {
				entries.add(profile.get());
			}
		} else if (level > 0) {
			entries.add(getLevelTag(s.codecName(), level));
		}

		if (s.hasBFrames()) {
			entries.add("with B frames");
		}

		final var frameRate = getValue(s.avgFrameRate()).map(b -> {
			final var pos = b.indexOf("/");
			if (pos == -1) {
				return b;
			} else {
				final var l = Double.valueOf(b.substring(0, pos));
				final var r = Double.valueOf(b.substring(pos + 1));
				final var df = new DecimalFormat();
				df.setDecimalFormatSymbols(symbols);
				df.setMaximumFractionDigits(3);
				df.setMinimumFractionDigits(0);
				df.setGroupingUsed(false);
				return df.format(l / r);
			}
		}).orElse("?");

		entries.add("@ " + frameRate + " fps");

		Optional.ofNullable(s.bitRate()).ifPresent(b -> {
			final var bitrateKbps = (double) b / 1000d;
			if (bitrateKbps < 1) {
				return;
			}
			if (bitrateKbps < 10000) {
				entries.add("[" + Math.round(bitrateKbps) + " kbps]");
			} else {
				entries.add("[" + Math.round(bitrateKbps / 1000) + " Mbps]");
			}
		});

		final var cpf = computePixelsFormat(s);
		if (cpf.isEmpty() == false) {
			entries.add(cpf);
		}

		if (s.hasBFrames() && s.nbFrames() > 0) {
			entries.add("(" + s.nbFrames() + " frms)");
		}

		final var dispositions = s.disposition().resumeDispositions().collect(joining(", "));
		if (dispositions.isEmpty() == false) {
			entries.add(dispositions);
		}
		return entries.stream().collect(joining(" "));
	}

	/*
	* */

	public static String getLevelTag(final String videoCodec, final int rawLevel) {
		return switch (videoCodec) {
		/**
		 * From https://github.com/FFmpeg/FFmpeg/blob/c7bfc826c351534262a9ee8ab39aa1fa0efe06a7/libavcodec/mpeg12enc.c#L1214C1-L1217C30
		 */
		case "mpeg1video", "mpeg2video", "mpegvideo" -> switch (rawLevel) {
		case 4 -> "High";
		case 6 -> "High 1440";
		case 8 -> "Main";
		case 10 -> "Low";
		default -> "L" + rawLevel;
		};
		/**
		 * From https://github.com/FFmpeg/FFmpeg/blob/c7bfc826c351534262a9ee8ab39aa1fa0efe06a7/libavcodec/h264_metadata_bsf.c#L677
		 */
		case "h264", "avc" -> switch (rawLevel) {
		case 10 -> "1";
		case 9 -> "1b";
		case 11 -> "1.1";
		case 12 -> "1.2";
		case 13 -> "1.3";
		case 20 -> "2";
		case 21 -> "2.1";
		case 22 -> "2.2";
		case 30 -> "3";
		case 31 -> "3.1";
		case 32 -> "3.2";
		case 40 -> "4";
		case 41 -> "4.1";
		case 42 -> "4.2";
		case 50 -> "5";
		case 51 -> "5.1";
		case 52 -> "5.2";
		case 60 -> "6";
		case 61 -> "6.1";
		case 62 -> "6.2";
		default -> "L" + rawLevel;
		};
		/**
		 * From https://github.com/FFmpeg/FFmpeg/blob/c7bfc826c351534262a9ee8ab39aa1fa0efe06a7/libavcodec/h265_metadata_bsf.c#L463
		 */
		case "hevc", "h265" -> switch (rawLevel) {
		case 30 -> "1";
		case 60 -> "2";
		case 63 -> "2.1";
		case 90 -> "3";
		case 93 -> "3.1";
		case 120 -> "4";
		case 123 -> "4.1";
		case 150 -> "5";
		case 153 -> "5.1";
		case 156 -> "5.2";
		case 180 -> "6";
		case 183 -> "6.1";
		case 186 -> "6.2";
		case 255 -> "8.5";
		default -> "L" + rawLevel;
		};
		/**
		 * From https://github.com/FFmpeg/FFmpeg/blob/c7bfc826c351534262a9ee8ab39aa1fa0efe06a7/libavcodec/libsvtav1.c#L620
		 */
		case "av1" -> switch (rawLevel) {
		case 20 -> "2.0";
		case 21 -> "2.1";
		case 22 -> "2.2";
		case 23 -> "2.3";
		case 30 -> "3.0";
		case 31 -> "3.1";
		case 32 -> "3.2";
		case 33 -> "3.3";
		case 40 -> "4.0";
		case 41 -> "4.1";
		case 42 -> "4.2";
		case 43 -> "4.3";
		case 50 -> "5.0";
		case 51 -> "5.1";
		case 52 -> "5.2";
		case 53 -> "5.3";
		case 60 -> "6.0";
		case 61 -> "6.1";
		case 62 -> "6.2";
		case 63 -> "6.3";
		case 70 -> "7.0";
		case 71 -> "7.1";
		case 72 -> "7.2";
		case 73 -> "7.3";
		default -> "L" + rawLevel;
		};
		default -> "L" + rawLevel;
		};
	}

	static String computePixelsFormat(final FFProbeStream s) {
		final var entries = new ArrayList<String>();
		getValue(s.pixFmt()).ifPresent(entries::add);
		getValue(s.colorRange()).map(v -> "colRange:" + v.toUpperCase()).ifPresent(entries::add);

		final var oColorSpace = getValue(s.colorSpace());
		final var oColorTransfer = getValue(s.colorTransfer());
		final var oColorPrimaries = getValue(s.colorPrimaries());

		if (oColorSpace.isPresent()
			&& oColorSpace.equals(oColorTransfer)
			&& oColorSpace.equals(oColorPrimaries)) {
			oColorSpace.map(String::toUpperCase).ifPresent(entries::add);
		} else {
			Stream.of(
					oColorSpace.map(v -> "colSpace:" + v.toUpperCase()).stream(),
					oColorTransfer.map(v -> "colTransfer:" + v.toUpperCase()).stream(),
					oColorPrimaries.map(v -> "colPrimaries:" + v.toUpperCase()).stream())
					.flatMap(f -> f)
					.forEach(entries::add);
		}

		return entries.stream().collect(Collectors.joining("/"));
	}

	static void addZeros(final int value, final StringBuilder sbTime) {
		if (value < 10) {
			sbTime.append("0");
		}
		sbTime.append(value);
	}

	public static String computeDuration(final FFProbeFormat format) {
		final var duration = Duration.ofMillis(Math.round(format.duration() * 1000f));
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
				.filter(Predicate.not(filterAudioStream))
				.filter(Predicate.not(filterVideoStream))
				.map(v -> {
					final var name = getValue(v.codecName())
							.or(() -> getValue(v.codecTagString()))
							.orElse("");
					final var handler = v.tags().stream()
							.filter(t -> "handler_name".equals(t.key()))
							.findFirst()
							.map(FFProbeKeyValue::value)
							.map(t -> " (" + t + ")")
							.orElse("");
					return v.codecType() + ": " + name + handler;
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
