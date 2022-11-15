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
 * Copyright (C) hdsdi3g for hd3g.tv 2020
 *
 */
package tv.hd3g.fflauncher;

import java.awt.Point;

import tv.hd3g.fflauncher.filtering.Filter;

public interface InputGeneratorsTraits extends SimpleSourceTraits {

	default void addSmptehdbarsGeneratorAsInputSource(final Point resolution,
													  final int durationInSec,
													  final String frameRate) {
		final var f = new Filter("smptehdbars");
		f.addArgument("duration", durationInSec);
		f.addArgument("size", resolution.x + "x" + resolution.y);
		f.addArgument("rate", frameRate);
		addSimpleInputSource(f.toString(), "-f", "lavfi");
	}

	default void addSineAudioGeneratorAsInputSource(final int frequency,
													final int durationInSec,
													final int sampleRate) {
		final var f = new Filter("sine");
		f.addArgument("duration", durationInSec);
		f.addArgument("frequency", frequency);
		f.addArgument("sample_rate", sampleRate);
		addSimpleInputSource(f.toString(), "-f", "lavfi");
	}

}
