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
 * Copyright (C) hdsdi3g for hd3g.tv 2024
 *
 */
package tv.hd3g.fflauncher.recipes.wavmeasure;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.time.Duration;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import tv.hd3g.commons.testtools.Fake;
import tv.hd3g.commons.testtools.MockToolsExtendsJunit;
import tv.hd3g.fflauncher.FFmpeg;
import tv.hd3g.fflauncher.SimpleSourceTraits;

@ExtendWith(MockToolsExtendsJunit.class)
class WavMeasureSetupTest {

	@Mock
	Duration fileDuration;
	@Mock
	Consumer<SimpleSourceTraits> setFFmpegSource;
	@Mock
	FFmpeg ffmpeg;
	@Fake
	String source;
	@Fake
	File sourceFile;

	@Fake(min = 1, max = 100000)
	int outputWide;
	@Fake(min = -10000, max = -1)
	int invalidOutputWide;

	WavMeasureSetup wms;

	@Test
	void testInvalidOutputWide() {
		assertThrows(IllegalArgumentException.class,
				() -> new WavMeasureSetup(source, fileDuration, 0));
		assertThrows(IllegalArgumentException.class,
				() -> new WavMeasureSetup(source, fileDuration, invalidOutputWide));
	}

	@Test
	void testApplySource_setFFmpegSource() {
		wms = new WavMeasureSetup(setFFmpegSource, fileDuration, outputWide);
		wms.applySource(ffmpeg);
		verify(setFFmpegSource, times(1)).accept(ffmpeg);
	}

	@Test
	void testApplySource_file() {
		wms = new WavMeasureSetup(sourceFile, fileDuration, outputWide);
		wms.applySource(ffmpeg);
		verify(ffmpeg, times(1)).addSimpleInputSource(sourceFile);
	}

	@Test
	void testApplySource_string() {
		wms = new WavMeasureSetup(source, fileDuration, outputWide);
		wms.applySource(ffmpeg);
		verify(ffmpeg, times(1)).addSimpleInputSource(source);
	}

}
