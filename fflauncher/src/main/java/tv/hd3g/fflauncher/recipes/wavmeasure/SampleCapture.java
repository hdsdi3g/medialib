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

import static java.lang.Math.sqrt;
import static tv.hd3g.fflauncher.recipes.wavmeasure.MeasuredWavEntry.HALF_16_BITS;
import static tv.hd3g.fflauncher.recipes.wavmeasure.WavMeasureSetup.SAMPLE_RATE;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import tv.hd3g.processlauncher.InputStreamConsumer;
import tv.hd3g.processlauncher.ProcesslauncherLifecycle;

@Slf4j
class SampleCapture implements InputStreamConsumer {

	private final byte[] buffer = new byte[0xFFFFF];

	private final float windowDurationMs;
	private final ShortBuffer currentSampleWindow;
	private final MeasuredWavEntry[] measuredWavEntriesList;
	private int currentEntryPos;

	SampleCapture(final int sampleWindowSize, final int sampleCount) {
		currentSampleWindow = ShortBuffer.allocate(sampleWindowSize);
		currentEntryPos = 0;
		measuredWavEntriesList = new MeasuredWavEntry[sampleCount];
		windowDurationMs = sampleWindowSize * 1000f / SAMPLE_RATE;

		log.debug(
				"Prepare audio measure with sampleWindowSize={} to windowDuration={} msec",
				sampleWindowSize, windowDurationMs);
	}

	@Override
	public void onProcessStart(final InputStream processInputStream, final ProcesslauncherLifecycle source) {
		try {
			int readed;
			while ((readed = processInputStream.read(buffer)) > 0) {
				onUpdateBuffer(readed);
			}
		} catch (final IOException e) {
			throw new UncheckedIOException("Can't read from stdout", e);
		}
	}

	@Override
	public void onClose(final ProcesslauncherLifecycle source) {
		if (currentSampleWindow.hasRemaining()) {
			onCloseCurrentSampleWindow();
		}
	}

	private void onUpdateBuffer(final int readSize) {
		final var samplesBuffer = ByteBuffer.wrap(buffer, 0, readSize).asShortBuffer();

		while (samplesBuffer.hasRemaining()) {
			while (samplesBuffer.hasRemaining() && currentSampleWindow.hasRemaining()) {
				currentSampleWindow.put(samplesBuffer.get());
			}
			if (currentSampleWindow.hasRemaining() == false) {
				onCloseCurrentSampleWindow();
			}
		}
	}

	private void onCloseCurrentSampleWindow() {
		if (measuredWavEntriesList.length == currentEntryPos) {
			/**
			 * Discard the last measured, because it's outside view (and not complete)
			 */
			currentSampleWindow.clear();
			return;
		}

		currentSampleWindow.flip();

		final var windowSize = (double) currentSampleWindow.remaining();
		int sample;
		var peakPositive = 0;
		var peakNegative = 0;
		var rmsPositive = 0l;
		var rmsNegative = 0l;

		while (currentSampleWindow.hasRemaining()) {
			sample = currentSampleWindow.get();
			if (sample > (short) -1) {
				peakPositive = peakPositive > sample ? peakPositive : sample;
				rmsPositive += sample * sample;
			} else {
				sample = Math.abs(sample);
				peakNegative = peakNegative > sample ? peakNegative : sample;
				rmsNegative += sample * sample;
			}
		}

		final var positionSec = windowDurationMs * currentEntryPos / 1000f;

		measuredWavEntriesList[currentEntryPos++] = new MeasuredWavEntry(
				positionSec,
				peakPositive / HALF_16_BITS,
				peakNegative / HALF_16_BITS,
				sqrt(rmsPositive / windowSize) / HALF_16_BITS,
				sqrt(rmsNegative / windowSize) / HALF_16_BITS);

		currentSampleWindow.clear();
	}

	MeasuredWav getMeasuredWav() {
		for (var pos = measuredWavEntriesList.length - 1; pos >= 0; pos--) {
			if (measuredWavEntriesList[pos] == null) {
				log.debug("Cancel MeasuredWav: no result at {}/{}", pos, measuredWavEntriesList.length);
				return null;
			}
		}

		return new MeasuredWav(List.of(measuredWavEntriesList));
	}

}
