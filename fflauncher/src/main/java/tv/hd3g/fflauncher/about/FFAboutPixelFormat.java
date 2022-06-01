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

import static tv.hd3g.fflauncher.about.FFAboutPixelFormat.BitDepths.Unknown;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import tv.hd3g.fflauncher.UnknownFormatException;

public class FFAboutPixelFormat {

	public enum BitDepths {

		Unknown(""), // S115 NOSONAR
		BitDepths_0("0"), // S115 NOSONAR
		BitDepths_1("1"), // S115 NOSONAR
		BitDepths_10("10"), // S115 NOSONAR
		BitDepths_10_10_10("10-10-10"), // S115 NOSONAR
		BitDepths_10_10_10_10("10-10-10-10"), // S115 NOSONAR
		BitDepths_12("12"), // S115 NOSONAR
		BitDepths_1_2_1("1-2-1"), // S115 NOSONAR
		BitDepths_12_12_12("12-12-12"), // S115 NOSONAR
		BitDepths_12_12_12_12("12-12-12-12"), // S115 NOSONAR
		BitDepths_14("14"), // S115 NOSONAR
		BitDepths_14_14_14("14-14-14"), // S115 NOSONAR
		BitDepths_16("16"), // S115 NOSONAR
		BitDepths_16_16("16-16"), // S115 NOSONAR
		BitDepths_16_16_16("16-16-16"), // S115 NOSONAR
		BitDepths_16_16_16_16("16-16-16-16"), // S115 NOSONAR
		BitDepths_2_3_3("2-3-3"), // S115 NOSONAR
		BitDepths_2_4_2("2-4-2"), // S115 NOSONAR
		BitDepths_32("32"), // S115 NOSONAR
		BitDepths_32_32_32("32-32-32"), // S115 NOSONAR
		BitDepths_32_32_32_32("32-32-32-32"), // S115 NOSONAR
		BitDepths_3_3_2("3-3-2"), // S115 NOSONAR
		BitDepths_4_4_4("4-4-4"), // S115 NOSONAR
		BitDepths_4_8_4("4-8-4"), // S115 NOSONAR
		BitDepths_5_5_5("5-5-5"), // S115 NOSONAR
		BitDepths_5_6_5("5-6-5"), // S115 NOSONAR
		BitDepths_8("8"), // S115 NOSONAR
		BitDepths_8_8("8-8"), // S115 NOSONAR
		BitDepths_8_8_8("8-8-8"), // S115 NOSONAR
		BitDepths_8_8_8_8("8-8-8-8"), // S115 NOSONAR
		BitDepths_9("9"), // S115 NOSONAR
		BitDepths_9_9_9("9-9-9"), // S115 NOSONAR
		BitDepths_9_9_9_9("9-9-9-9"); // S115 NOSONAR

		public final String tag;

		BitDepths(final String tag) {
			this.tag = tag;
		}

		@Override
		public String toString() {
			return tag;
		}

		public static BitDepths getFromTag(final String tag) {
			Objects.requireNonNull(tag);
			return Stream.of(values())
			        .filter(bD -> bD.tag.equals(tag))
			        .findFirst()
			        .orElse(Unknown);
		}

	}

	static List<FFAboutPixelFormat> parsePixelsFormats(final List<String> lines) {
		return lines.stream()
		        .map(String::trim)
		        .filter(line -> (line.toLowerCase().startsWith("Pixel formats:".toLowerCase()) == false))
		        .filter(line -> (line.contains("=") == false))
		        .filter(line -> (line.toLowerCase().startsWith("FLAGS".toLowerCase()) == false))
		        .filter(line -> (line.startsWith("-----") == false))
		        .map(FFAboutPixelFormat::new)
		        .collect(Collectors.toUnmodifiableList());
	}

	public final boolean supportedInput;
	public final boolean supportedOutput;
	public final boolean hardwareAccelerated;
	public final boolean paletted;
	public final boolean bitstream;
	public final int nbComponents;
	public final int bitsPerPixel;
	public final BitDepths bitDepths;
	public final String tag;

	FFAboutPixelFormat(final String line) {

		final var lineBlocs = Arrays.stream(line.split(" ")).filter(lb -> lb.trim().equals("") == false).map(
		        String::trim).collect(Collectors.toUnmodifiableList());

		if (lineBlocs.size() < 4 || lineBlocs.size() > 5) {
			throw new UnknownFormatException("Can't parse line: \"" + line + "\"");
		}

		supportedInput = lineBlocs.get(0).contains("I");
		supportedOutput = lineBlocs.get(0).contains("O");
		hardwareAccelerated = lineBlocs.get(0).contains("H");
		paletted = lineBlocs.get(0).contains("P");
		bitstream = lineBlocs.get(0).contains("B");
		tag = lineBlocs.get(1);
		nbComponents = Integer.parseInt(lineBlocs.get(2));
		bitsPerPixel = Integer.parseInt(lineBlocs.get(3));
		if (lineBlocs.size() == 5) {
			bitDepths = BitDepths.getFromTag(lineBlocs.get(4));
		} else {
			bitDepths = Unknown;
		}
	}

}
