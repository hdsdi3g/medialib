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

import java.util.Map;

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
									Map<String, Float> other) {
}
