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

import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import tv.hd3g.fflauncher.UnknownFormatException;

public class FFAboutFormat {

	static List<FFAboutFormat> parseFormats(final List<String> lines) {
		return lines.stream()
				.map(String::trim)
				.filter(line -> (line.toLowerCase().startsWith("File formats:".toLowerCase()) == false))
				.filter(line -> (line.toLowerCase().startsWith("D. = Demuxing supported".toLowerCase()) == false))
				.filter(line -> (line.toLowerCase().startsWith(".E = Muxing supported".toLowerCase()) == false))
				.filter(line -> (line.startsWith("--") == false))
				.map(FFAboutFormat::new)
				.toList();
	}

	public final boolean demuxing;
	public final boolean muxing;

	/**
	 * Like "asf"
	 */
	public final String name;

	/**
	 * Like "mov, mp4, m4a, 3gp, 3g2, mj2"
	 */
	public final Set<String> alternateTags;

	/**
	 * Like "ASF (Advanced / Active Streaming Format)"
	 */
	public final String longName;

	FFAboutFormat(final String line) {

		final var lineBlocs = Arrays.stream(line.split(" "))
				.filter(lb -> lb.trim().equals("") == false)
				.map(String::trim)
				.toList();

		if (lineBlocs.size() < 2) {
			throw new UnknownFormatException("Can't parse line: \"" + line + "\"");
		}

		demuxing = lineBlocs.get(0).trim().contains("D");
		muxing = lineBlocs.get(0).trim().contains("E");

		if (lineBlocs.get(1).contains(",")) {
			name = Arrays.stream(lineBlocs.get(1).trim().split(",")).findFirst().orElse("");
			alternateTags = unmodifiableSet(Arrays.stream(lineBlocs.get(1).trim().split(",")).collect(toSet()));
		} else {
			name = lineBlocs.get(1);
			alternateTags = Collections.singleton(name);
		}

		longName = lineBlocs.stream().filter(lb -> lb.trim().equals("") == false).skip(2).collect(joining(" "));
	}

	@Override
	public String toString() {
		final var sb = new StringBuilder();

		if (longName.isBlank() == false) {
			sb.append(longName);
			sb.append(" ");
		}
		sb.append("[");
		sb.append(name);
		if (alternateTags.size() > 1) {
			sb.append(", ");
			sb.append(alternateTags.stream().filter(t -> t.equals(name) == false).collect(Collectors.joining(", ")));
		}
		sb.append("] ");

		if (muxing && demuxing) {
			sb.append("muxing and demuxing supported");
		} else if (muxing) {
			sb.append("muxing only supported");
		} else {
			sb.append("demuxing only supported");
		}

		return sb.toString();
	}
}
