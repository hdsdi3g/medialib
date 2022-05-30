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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Point;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tv.hd3g.processlauncher.cmdline.Parameters;

class InputGeneratorsTraitsTest {

	FFmpeg ffmpeg;

	@BeforeEach
	void init() {
		ffmpeg = new FFmpeg("ffmpeg", new Parameters());
	}

	@Test
	void testAddSmptehdbarsGeneratorAsInputSource() {
		ffmpeg.addSmptehdbarsGeneratorAsInputSource(new Point(1920, 1080), 5, "25");
		assertEquals("-f lavfi -i smptehdbars=duration=5:size=1920x1080:rate=25",
		        ffmpeg.getReadyToRunParameters().toString());
	}

	@Test
	void testAddSineAudioGeneratorAsInputSource() {
		ffmpeg.addSineAudioGeneratorAsInputSource(1000, 4, 48000);
		assertEquals("-f lavfi -i sine=duration=4:frequency=1000:sample_rate=48000",
		        ffmpeg.getReadyToRunParameters().toString());
	}
}
