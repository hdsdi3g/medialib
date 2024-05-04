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
 * Copyright (C) hdsdi3g for hd3g.tv 2018
 *
 */
package tv.hd3g.fflauncher.about;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import tv.hd3g.fflauncher.UnknownFormatException;

public class FFAboutCodec {

	private static final String CAN_T_PARSE_LINE = "Can't parse line: \"";

	static List<FFAboutCodec> parse(final List<String> lines) {
		return lines.stream()
				.map(String::trim)
				.dropWhile(line -> line.startsWith("-") == false && line.endsWith("-") == false)
				.filter(line -> line.startsWith("-") == false && line.endsWith("-") == false)
				.map(FFAboutCodec::new)
				.toList();
	}

	public enum CodecType {
		VIDEO,
		AUDIO,
		SUBTITLE,
		DATA;
	}

	public final boolean decodingSupported;
	public final boolean encodingSupported;
	public final CodecType type;
	public final boolean intraFrameOnly;
	public final boolean lossyCompression;
	public final boolean losslessCompression;

	public final Set<String> encoders;
	public final Set<String> decoders;

	/**
	 * Like "dpx"
	 */
	public final String name;

	/**
	 * Like "DPX (Digital Picture Exchange) image"
	 */
	public final String longName;

	private static CodecType letterToCodecType(final String line, final char letter) {
		if (letter == 'V') {
			return CodecType.VIDEO;
		} else if (letter == 'A') {
			return CodecType.AUDIO;
		} else if (letter == 'S') {
			return CodecType.SUBTITLE;
		} else if (letter == 'D') {
			return CodecType.DATA;
		} else {
			throw new UnknownFormatException(CAN_T_PARSE_LINE + line + "\" (missing codec type)");
		}
	}

	private static void negativeToOutOfBoundException(final int value, final String text) {
		if (value == -1) {
			throw new IndexOutOfBoundsException(text);
		}
	}

	FFAboutCodec(final String line) {
		final var lineBlocs = line.split(" ");

		if (lineBlocs.length < 3) {
			throw new UnknownFormatException(CAN_T_PARSE_LINE + line + "\"");
		}

		/**
		 * Parse "codec type zone"
		 */

		decodingSupported = lineBlocs[0].charAt(0) == 'D';
		encodingSupported = lineBlocs[0].charAt(1) == 'E';
		type = letterToCodecType(line, lineBlocs[0].charAt(2));
		intraFrameOnly = lineBlocs[0].charAt(3) == 'I';
		lossyCompression = lineBlocs[0].charAt(4) == 'L';
		losslessCompression = lineBlocs[0].charAt(5) == 'S';

		if (lineBlocs[0].substring(3).chars().noneMatch(i -> (i == 'I' || i == 'L' || i == 'S' || i == '.'))) {
			throw new UnknownFormatException(CAN_T_PARSE_LINE + line + "\" (invalid ends for codec type)");
		}

		name = lineBlocs[1].trim();

		/**
		 * Like "Dirac (decoders: dirac libschroedinger ) (encoders: vc2 libschroedinger )"
		 */
		final var raw_long_name = Arrays.stream(lineBlocs)
				.filter(lb -> lb.trim().equals("") == false)
				.skip(2)
				.collect(Collectors.joining(" "));

		final var parDecoders = "(decoders:";
		final var decoders_tag_pos = raw_long_name.indexOf(parDecoders);
		final var encoders_tag_pos = raw_long_name.indexOf("(encoders:");

		if (decoders_tag_pos > -1 || encoders_tag_pos > -1) {
			if (decoders_tag_pos > -1) {
				final var decoders_tag_end_pos = raw_long_name.indexOf(')', decoders_tag_pos);
				negativeToOutOfBoundException(decoders_tag_end_pos, "Can't found \")\" in \"" + raw_long_name + "\"");
				decoders = Collections.unmodifiableSet(Arrays.stream(raw_long_name.substring(decoders_tag_pos
																							 + parDecoders.length(),
						decoders_tag_end_pos).trim().split(" ")).distinct().collect(Collectors.toSet()));
			} else {
				decoders = Collections.emptySet();
			}

			if (encoders_tag_pos > -1) {
				final var encoders_tag_end_pos = raw_long_name.indexOf(')', encoders_tag_pos);
				negativeToOutOfBoundException(encoders_tag_end_pos, "Can't found \")\" in \"" + raw_long_name + "\"");
				encoders = Collections.unmodifiableSet(Arrays.stream(raw_long_name.substring(encoders_tag_pos
																							 + parDecoders.length(),
						encoders_tag_end_pos).trim().split(" ")).distinct().collect(Collectors.toSet()));
			} else {
				encoders = Collections.emptySet();
			}

			longName = extractLongnameFromRawLongName(raw_long_name, decoders_tag_pos, encoders_tag_pos);
		} else {
			encoders = Collections.emptySet();
			decoders = Collections.emptySet();
			longName = raw_long_name;
		}
	}

	private static String extractLongnameFromRawLongName(final String raw_long_name,
														 final int decoders_tag_pos,
														 final int encoders_tag_pos) {
		if (decoders_tag_pos > -1 && encoders_tag_pos > -1) {
			return raw_long_name.substring(0, Math.min(decoders_tag_pos - 1, encoders_tag_pos - 1));
		} else if (decoders_tag_pos > -1) {
			return raw_long_name.substring(0, decoders_tag_pos - 1);
		} else {
			return raw_long_name.substring(0, encoders_tag_pos - 1);
		}
	}

	@Override
	public String toString() {
		final var sb = new StringBuilder();

		sb.append(longName);
		sb.append(" [");
		sb.append(name);
		sb.append("] ");

		sb.append(type.toString().toLowerCase());

		if (decodingSupported && encodingSupported) {
			sb.append(" encoding and decoding supported");
		} else if (decodingSupported) {
			sb.append(" decoding only supported");
		} else {
			sb.append(" encoding only supported");
		}

		if (intraFrameOnly) {
			sb.append(", intra frame-only codec");
		}
		if (lossyCompression) {
			sb.append(", lossy compression");
		}
		if (losslessCompression) {
			sb.append(", lossless compression");
		}

		return sb.toString();
	}
}
