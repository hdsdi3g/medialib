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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import tv.hd3g.commons.testtools.Fake;
import tv.hd3g.commons.testtools.MockToolsExtendsJunit;
import tv.hd3g.processlauncher.ProcesslauncherLifecycle;

@ExtendWith(MockToolsExtendsJunit.class)
class SampleCaptureTest {

	@Mock
	ProcesslauncherLifecycle source;

	@Fake(min = 100, max = 1000)
	int scSize;
	@Fake(min = 2000, max = 20000)
	int samples;

	int resultSize;
	InputStream processInputStream;
	SampleCapture sc;

	@BeforeEach
	void init() {
		resultSize = samples / scSize;

		sc = new SampleCapture(scSize, resultSize);
		final var max16bits = Math.pow(2, 16);

		final var bBuffer = ByteBuffer.allocate(samples * 2);
		IntStream.range(0, bBuffer.remaining() / 2)
				.mapToDouble(i -> i / 100d)
				.map(Math::sin)
				.map(i -> i / 2)
				.map(i -> i + 0.5d)
				.map(i -> i * max16bits)
				.map(Math::floor)
				.mapToLong(Math::round)
				.forEach(v -> bBuffer.putShort((short) v));

		processInputStream = new ByteArrayInputStream(bBuffer.array());
	}

	@Test
	void testProcess() throws IOException {
		sc.onProcessStart(processInputStream, source);
		sc.onClose(source);
		assertEquals(0, processInputStream.available());

		final var mw = sc.getMeasuredWav();
		assertEquals(resultSize, mw.entries().size());

		assertEquals(resultSize,
				mw.entries().stream()
						.map(MeasuredWavEntry::position)
						.distinct()
						.count());
	}

}
