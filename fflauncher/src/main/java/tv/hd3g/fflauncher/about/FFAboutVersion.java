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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FFAboutVersion {

	private static final String HEADER_CONFIGURATION = "configuration:";

	private static final Logger log = LogManager.getLogger();

	/**
	 * Like "4.0 Copyright (c) 2000-2018 the FFmpeg developers"
	 */
	public final String headerVersion;

	/**
	 * Like "gcc 6.3.0 (Debian 6.3.0-18+deb9u1) 20170516" or "gcc 7.3.0 (GCC)"
	 */
	public final String builtWith;

	/**
	 * unmodifiableSet, like "yasm, gpl, version3, nonfree, libmp3lame, libbluray..."
	 */
	public final Set<String> configuration;

	/**
	 * Like "--as=yasm --enable-gpl --enable-version3 --enable-nonfree --enable-libmp3lame" ...
	 */
	public final String rawConfiguration;

	/**
	 * Like "56. 14.100 / 56. 14.100"
	 */
	public final String libavutilVersion;
	/**
	 * Like "56. 14.100 / 56. 14.100"
	 */
	public final String libavcodecVersion;
	/**
	 * Like "56. 14.100 / 56. 14.100"
	 */
	public final String libavformatVersion;
	/**
	 * Like "56. 14.100 / 56. 14.100"
	 */
	public final String libavdeviceVersion;
	/**
	 * Like "56. 14.100 / 56. 14.100"
	 */
	public final String libavfilterVersion;
	/**
	 * Like "56. 14.100 / 56. 14.100"
	 */
	public final String libswscaleVersion;
	/**
	 * Like "56. 14.100 / 56. 14.100"
	 */
	public final String libswresampleVersion;
	/**
	 * Like "56. 14.100 / 56. 14.100"
	 */
	public final String libpostprocVersion;

	FFAboutVersion(final List<String> processResult) {
		headerVersion = processResult.stream().filter(l -> l.startsWith("ffmpeg version ")).findFirst().orElse(
		        "ffmpeg version ?").substring("ffmpeg version ".length()).trim();

		builtWith = processResult.stream().filter(l -> l.startsWith("built with ")).findFirst().orElse("built with ?")
		        .substring("built with ".length()).trim();

		rawConfiguration = processResult.stream().filter(l -> l.startsWith(HEADER_CONFIGURATION)).findFirst().orElse(
		        HEADER_CONFIGURATION).substring(HEADER_CONFIGURATION.length()).trim();

		configuration = Collections.unmodifiableSet(Arrays.stream(rawConfiguration.split(" ")).map(c -> {
			if (c.startsWith("--enable-")) {
				return c.substring("--enable-".length()).trim();
			} else if (c.startsWith("--as=")) {
				return c.substring("--as=".length()).trim();
			}
			return c.trim();
		}).distinct().collect(Collectors.toSet()));

		log.debug(() -> "\"" + rawConfiguration + "\" <-> configuration: " + configuration);

		libavutilVersion = extractLibavVersion("libavutil", processResult);
		libavcodecVersion = extractLibavVersion("libavcodec", processResult);
		libavformatVersion = extractLibavVersion("libavformat", processResult);
		libavdeviceVersion = extractLibavVersion("libavdevice", processResult);
		libavfilterVersion = extractLibavVersion("libavfilter", processResult);
		libswscaleVersion = extractLibavVersion("libswscale", processResult);
		libswresampleVersion = extractLibavVersion("libswresample", processResult);
		libpostprocVersion = extractLibavVersion("libpostproc", processResult);
	}

	/**
	 * @return headerVersion
	 */
	@Override
	public String toString() {
		return headerVersion;
	}

	private static String extractLibavVersion(final String key, final List<String> lines) {

		final var line = lines.stream().filter(l -> l.startsWith(key)).findFirst().orElse(key + "      ?.?.?");

		/**
		 * libavutil 56. 14.100 / 56. 14.100
		 */
		return line.substring(key.length()).trim();
	}

}
