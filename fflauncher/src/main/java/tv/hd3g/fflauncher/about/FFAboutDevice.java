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

import java.util.List;
import java.util.stream.Collectors;

public class FFAboutDevice extends FFAboutFormat {

	static List<FFAboutDevice> parseDevices(final List<String> lines) {
		return lines.stream()
		        .map(String::trim)
		        .filter(line -> (line.toLowerCase().startsWith("Devices:".toLowerCase()) == false))
		        .filter(line -> (line.toLowerCase().startsWith("D. = Demuxing supported".toLowerCase()) == false))
		        .filter(line -> (line.toLowerCase().startsWith(".E = Muxing supported".toLowerCase()) == false))
		        .filter(line -> (line.startsWith("--") == false))
		        .map(FFAboutDevice::new)
		        .collect(Collectors.toUnmodifiableList());
	}

	FFAboutDevice(final String line) {
		super(line);
	}

}
