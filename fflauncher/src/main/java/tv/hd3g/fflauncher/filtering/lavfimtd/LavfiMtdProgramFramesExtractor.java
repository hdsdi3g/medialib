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
 * Copyright (C) hdsdi3g for hd3g.tv 2022
 *
 */
package tv.hd3g.fflauncher.filtering.lavfimtd;

import java.util.List;

public interface LavfiMtdProgramFramesExtractor<T> {

	LavfiMtdProgramFrames<T> getMetadatas(List<? extends LavfiRawMtdFrame> extractedRawMtdFrames);

	/**
	 * From a float string chain
	 */
	default int parseInt(final String floatString) {
		return Math.round(Float.parseFloat(floatString));
	}

	/**
	 * From a double string chain
	 */
	default long parseLong(final String doubleString) {
		return Math.round(Double.parseDouble(doubleString));
	}
}
