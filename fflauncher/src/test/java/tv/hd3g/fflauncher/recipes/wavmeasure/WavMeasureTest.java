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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static tv.hd3g.fflauncher.SimpleSourceTraits.addSineAudioGeneratorAsInputSource;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import tv.hd3g.commons.testtools.MockToolsExtendsJunit;

@ExtendWith(MockToolsExtendsJunit.class)
class WavMeasureTest {

	@Test
	void testE2E() {
		final var duration = 5;
		final var outputWide = 500;

		final var vm = new WavMeasure("ffmpeg");
		final var setup = new WavMeasureSetup(
				ffmpeg -> addSineAudioGeneratorAsInputSource(ffmpeg, 50, duration, 48000),
				Duration.ofSeconds(duration),
				outputWide);
		final var list = vm.process(setup).getResult();
		assertThat(list.entries())
				.size()
				.isEqualTo(outputWide);

		assertEquals(12,
				list.entries().stream()
						.map(MeasuredWavEntry::rmsPositive)
						.distinct()
						.count());

		assertEquals(outputWide,
				list.entries().stream()
						.map(MeasuredWavEntry::position)
						.distinct()
						.count());
	}
}
