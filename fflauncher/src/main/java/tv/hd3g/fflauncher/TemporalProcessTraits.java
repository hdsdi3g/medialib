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
 * Copyright (C) hdsdi3g for hd3g.tv 2023
 *
 */
package tv.hd3g.fflauncher;

import static java.lang.Long.parseLong;
import static java.lang.Math.abs;
import static java.time.Duration.ZERO;
import static java.time.temporal.ChronoField.MILLI_OF_DAY;
import static java.util.Objects.requireNonNull;
import static java.util.TimeZone.getTimeZone;
import static org.apache.commons.lang3.StringUtils.rightPad;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public interface TemporalProcessTraits extends InternalParametersSupplier {

	/**
	 * Don't manage precision under 1ms.
	 */
	static String positionToFFmpegPosition(final Duration pos) {
		final var formatStr = abs(pos.getNano()) > 0 ? "HH:mm:ss.SSS" : "HH:mm:ss";

		final var format = new SimpleDateFormat(formatStr);
		format.setTimeZone(getTimeZone("UTC"));
		final var negative = pos.isNegative() ? "-" : "";
		return negative + format.format(new Date(abs(pos.toMillis())));
	}

	/**
	 * https://ffmpeg.org/ffmpeg-utils.html#time-duration-syntax
	 * [-][HH:]MM:SS[.m...]
	 * [-]S+[.m...][s|ms|us]
	 */
	static Duration ffmpegDurationToDuration(final String ffDuration) {
		requireNonNull(ffDuration);
		if (ffDuration.isEmpty()) {
			return ZERO;
		}
		final var isNegative = ffDuration.startsWith("-");
		final var rawDuration = isNegative ? ffDuration.substring(1) : ffDuration;

		final long computedDurationµs;
		if (rawDuration.contains(":")) {
			/**
			 * [HH:]MM:SS[.m...]
			 */
			final var formatter = DateTimeFormatter.ofPattern("HH:mm[:ss[.SSS]]");
			formatter.withZone(ZoneId.of("UTC"));
			computedDurationµs = formatter.parse(rawDuration).getLong(MILLI_OF_DAY) * 1000l;
		} else {
			/**
			 * S+[.m...][s|ms|us]
			 */

			final String decimal;
			final long multiplierToUs;
			if (rawDuration.endsWith("ms")) {
				decimal = rawDuration.substring(0, rawDuration.length() - 2);
				multiplierToUs = 1000l;
			} else if (rawDuration.endsWith("us")) {
				decimal = rawDuration.substring(0, rawDuration.length() - 2);
				multiplierToUs = 1_000_000l;
			} else if (rawDuration.endsWith("s")) {
				decimal = rawDuration.substring(0, rawDuration.length() - 1);
				multiplierToUs = 1l;
			} else {
				decimal = rawDuration;
				multiplierToUs = 1l;
			}

			final var dot = decimal.indexOf(".");
			final long leftDotValue;
			final long rightDotValue;
			if (dot > 0) {
				leftDotValue = parseLong(decimal.substring(0, dot));
				rightDotValue = parseLong(rightPad(decimal.substring(dot + 1), 6, "0"));
			} else {
				leftDotValue = parseLong(decimal);
				rightDotValue = 0;
			}

			computedDurationµs = (leftDotValue * 1_000_000l + rightDotValue) / multiplierToUs;
		}

		final var durationMs = isNegative ? -computedDurationµs : computedDurationµs;
		return Duration.ofNanos(durationMs * 1000l);
	}

	/**
	 * When used as an output option (before an output url), stop writing the output after its duration reaches duration.
	 * duration must be a time duration specification, see (ffmpeg-utils)the Time duration section in the ffmpeg-utils(1) manual.
	 **/
	default void addDuration(final String duration) {
		getInternalParameters().addParameters("-t", duration);
	}

	/**
	 * Stop writing the output or reading the input at position.
	 * position must be a time duration specification, see (ffmpeg-utils)the Time duration section in the ffmpeg-utils(1) manual.
	 */
	default void addToDuration(final String position) {
		getInternalParameters().addParameters("-to", position);
	}

	/**
	 * When used as an output option (before an output url), stop writing the output after its duration reaches duration.
	 * duration must be a time duration specification, see (ffmpeg-utils)the Time duration section in the ffmpeg-utils(1) manual.
	 **/
	default void addDuration(final Duration duration) {
		getInternalParameters().addParameters("-t", positionToFFmpegPosition(duration));
	}

	/**
	 * Stop writing the output or reading the input at position.
	 * position must be a time duration specification, see (ffmpeg-utils)the Time duration section in the ffmpeg-utils(1) manual.
	 */
	default void addToDuration(final Duration position) {
		getInternalParameters().addParameters("-to", positionToFFmpegPosition(position));
	}

	/**
	 * Like the -ss option but relative to the "end of file". That is negative values are earlier in the file, 0 is at EOF.
	 * position must be a time duration specification, see (ffmpeg-utils)the Time duration section in the ffmpeg-utils(1) manual.
	 */
	default void addEndPosition(final String position) {
		getInternalParameters().addParameters("-sseof", position);
	}

	/**
	 * Like the -ss option but relative to the "end of file". That is negative values are earlier in the file, 0 is at EOF.
	 * position must be a time duration specification, see (ffmpeg-utils)the Time duration section in the ffmpeg-utils(1) manual.
	 */
	default void addEndPosition(final Duration position) {
		getInternalParameters().addParameters("-sseof", positionToFFmpegPosition(position));
	}

}
