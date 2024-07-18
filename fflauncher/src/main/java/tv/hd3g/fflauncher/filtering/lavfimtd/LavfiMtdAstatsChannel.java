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
package tv.hd3g.fflauncher.filtering.lavfimtd;

import static java.lang.reflect.Modifier.isStatic;

import java.lang.reflect.Field;
import java.util.List;
import java.util.SequencedMap;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public record LavfiMtdAstatsChannel(
									/** Mean amplitude displacement from zero. */
									float dcOffset,
									/** Standard peak measured in dBFS */
									float peakLevel,
									/** Consecutive samples with the same value of the signal at its peak levels (i.e. either Min level or Max level) */
									long flatness,
									/** Number of occasions (not the number of samples) that the signal attained either Min level or Max level. */
									long peakCount,
									/** Minimum local peak measured in dBFS over a short window. */
									float noiseFloor,
									/** Number of occasions (not the number of samples) that the signal attained Noise floor. */
									long noiseFloorCount,
									/** Entropy measured across whole audio. Entropy of value near 1.0 is typically measured for white noise. */
									float entropy,
									/** Overall bit depth of audio, i.e. number of bits used for each sample */
									int bitDepth,
									/** Standard ratio of peak to RMS level (note: not in dB) */
									float crestFactor,
									/** Measured dynamic range of audio in dB */
									float dynamicRange,
									/** Flatness (i.e. consecutive samples with the same value) of the signal at its peak levels (i.e. either Min_level or Max_level) */
									float flatFactor,
									/** Maximal difference between two consecutive samples */
									float maxDifference,
									/** Maximal sample level */
									float maxLevel,
									/** Mean difference between two consecutive samples, i.e. the average of each difference between two consecutive samples */
									float meanDifference,
									/** Minimal difference between two consecutive samples */
									float minDifference,
									/** Minimal sample level */
									float minLevel,
									/** Root Mean Square difference between two consecutive samples */
									float rmsDifference,
									/** Standard RMS level measured in dBFS */
									float rmsLevel,
									/** Peak values for RMS level measured over a short window, measured in dBFS. */
									float rmsPeak,
									/** Trough values for RMS level measured over a short window, measured in dBFS. */
									float rmsTrough,
									/** Number of points where the waveform crosses the zero level axis */
									float zeroCrossings,
									/** Rate of Zero crossings and number of audio samples */
									float zeroCrossingsRate,
									/** Number of samples with an infinite value */
									long numberOfInfs,
									/** Number of samples with a NaN (not a number) value */
									long numberOfNaNs,
									/** Number of samples with a subnormal value */
									long numberOfDenormals,
									/** Number of samples */
									long numberOfSamples,
									/** Number of occasions that the absolute samples taken from the signal attained max absolute value of Min_level and Max_level */
									long absPeakCount,
									SequencedMap<String, Float> other) {

	private static final List<Field> declaredFields = Stream.of(LavfiMtdAstatsChannel.class.getDeclaredFields())
			.filter(f -> isStatic(f.getModifiers()) == false)
			.toList();

	public static Stream<String> getFieldNames(final UnaryOperator<String> nameAdapter) {
		return declaredFields.stream()
				.map(Field::getName)
				.map(nameAdapter);
	}

	public Stream<Number> getValues() {
		return declaredFields.stream()
				.filter(f -> f.getName().equals("other") == false)
				.map(f -> {
					try {
						return (Number) f.get(this);
					} catch (IllegalArgumentException | IllegalAccessException e) {
						throw new IllegalStateException(e);
					}
				});
	}

}
