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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.ffmpeg.ffprobe.FormatType;
import org.ffmpeg.ffprobe.StreamDispositionType;
import org.ffmpeg.ffprobe.StreamType;
import org.ffmpeg.ffprobe.TagType;

public record MediaSummary(String format, List<String> streams) {
	private static final DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);

	static MediaSummary create(final FFprobeJAXB source) {
		final var format = source.getFormat();
		final var entries = new ArrayList<String>();
		entries.add(format.getFormatLongName());
		entries.add(computeDuration(format));
		if (format.getSize() <= 1024 * 1024) {
			entries.add(format.getSize() + " bytes");
		} else {
			entries.add(format.getSize() / 1024 / 1024 + " MB");
		}

		if (format.getNbPrograms() > 0) {
			entries.add(format.getNbPrograms() + " program(s)");
		}
		if (source.getChapters().isEmpty() == false) {
			entries.add(source.getChapters().size() + " chapter(s)");
		}

		if (source.getVideoStreams().anyMatch(f -> f.getBitRate() != null) == false
			&& source.getAudiosStreams().anyMatch(f -> f.getBitRate() != null) == false) {
			Optional.ofNullable(format.getBitRate()).ifPresent(b -> {
				final var bitrateKbps = (double) b / 1000d;
				if (bitrateKbps < 10000) {
					entries.add(Math.round(bitrateKbps) + " kbps");
				} else {
					entries.add(Math.round(bitrateKbps / 1000) + " Mbps");
				}
			});
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

	private static Stream<String> optDisposition(final int value, final String label) {
		if (value == 1) {
			return Stream.of(label);
		}
		return Stream.empty();
	}

	public static Stream<String> resumeDispositions(final StreamDispositionType s) {
		if (s == null) {
			return Stream.of();
		}
		return Stream.of(
				optDisposition(s.getDefault(), "default stream"),
				optDisposition(s.getAttachedPic(), "attached picture"),
				optDisposition(s.getTimedThumbnails(), "timed thumbnails"),
				optDisposition(s.getStillImage(), "still image"),
				optDisposition(s.getHearingImpaired(), "hearing impaired"),
				optDisposition(s.getVisualImpaired(), "visual impaired"),
				optDisposition(s.getDub(), "dub"),
				optDisposition(s.getOriginal(), "original"),
				optDisposition(s.getComment(), "comment"),
				optDisposition(s.getLyrics(), "lyrics"),
				optDisposition(s.getKaraoke(), "karaoke"),
				optDisposition(s.getForced(), "forced"),
				optDisposition(s.getCleanEffects(), "clean effects"),
				optDisposition(s.getCaptions(), "captions"),
				optDisposition(s.getDescriptions(), "descriptions"),
				optDisposition(s.getMetadata(), "metadata"),
				optDisposition(s.getDependent(), "dependent"))
				.flatMap(Function.identity());
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
			if (s.getChannelLayout() != null) {
				entries.add(s.getChannelLayout() + " (" + s.getChannels() + " channels)");
			} else {
				entries.add(s.getChannels() + " channels");
			}
		} else if (s.getChannelLayout() != null) {
			entries.add(s.getChannelLayout());
		} else if (s.getChannels() == 2) {
			entries.add("2 channels");
		} else {
			entries.add("mono");
		}

		Optional.ofNullable(s.getSampleRate())
				.ifPresent(sr -> entries.add("@ " + sr + " Hz"));

		Optional.ofNullable(s.getBitRate())
				.ifPresent(b -> entries.add("[" + b / 1000 + " kbps]"));

		final var dispositions = resumeDispositions(s.getDisposition()).collect(joining(", "));
		if (dispositions.isEmpty() == false) {
			entries.add(dispositions);
		}
		return entries.stream().collect(joining(" "));
	}

	static String getVideoSummary(final StreamType s) {
		final var entries = new ArrayList<String>();

		entries.add(s.getCodecType() + ": " + s.getCodecName());

		if (s.getWidth() != null && s.getHeight() != null) {
			entries.add(s.getWidth() + "×" + s.getHeight());
		}

		final var profile = getValue(s.getProfile()).filter(p -> p.equals("0") == false);
		final var level = Optional.ofNullable(s.getLevel()).orElse(0);
		if (profile.isPresent()) {
			if (level > 0) {
				entries.add(profile.get() + "/" + getLevelTag(s.getCodecName(), level));
			} else {
				entries.add(profile.get());
			}
		} else if (level > 0) {
			entries.add(getLevelTag(s.getCodecName(), level));
		}

		if (s.getHasBFrames() != null && s.getHasBFrames() > 0) {
			entries.add("with B frames");
		}

		final var frameRate = getValue(s.getAvgFrameRate()).map(b -> {
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

		Optional.ofNullable(s.getBitRate()).ifPresent(b -> {
			final var bitrateKbps = (double) b / 1000d;
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

		if (s.getNbFrames() != null && s.getNbFrames() > 0) {
			entries.add("(" + s.getNbFrames() + " frms)");
		}

		final var dispositions = resumeDispositions(s.getDisposition()).collect(joining(", "));
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

	static String computePixelsFormat(final StreamType s) {
		final var entries = new ArrayList<String>();
		getValue(s.getPixFmt()).ifPresent(entries::add);
		getValue(s.getColorRange()).map(v -> "colRange:" + v.toUpperCase()).ifPresent(entries::add);

		final var oColorSpace = getValue(s.getColorSpace());
		final var oColorTransfer = getValue(s.getColorTransfer());
		final var oColorPrimaries = getValue(s.getColorPrimaries());

		if (oColorSpace.isPresent()
			&& oColorSpace.equals(oColorTransfer)
			&& oColorSpace.equals(oColorPrimaries)) {
			oColorSpace.map(String::toUpperCase).ifPresent(entries::add);
		} else {
			Stream.concat(oColorSpace.map(v -> "colSpace:" + v.toUpperCase()).stream(),
					Stream.concat(
							oColorTransfer.map(v -> "colTransfer:" + v.toUpperCase()).stream(),
							oColorPrimaries.map(v -> "colPrimaries:" + v.toUpperCase()).stream()))
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

	public static String computeDuration(final FormatType format) {
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
