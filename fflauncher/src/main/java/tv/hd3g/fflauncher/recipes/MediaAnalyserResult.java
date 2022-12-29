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
package tv.hd3g.fflauncher.recipes;

import java.util.List;

import tv.hd3g.fflauncher.filtering.lavfimtd.LavfiRawMtdFrame;
import tv.hd3g.fflauncher.resultparser.Ebur128Summary;

public record MediaAnalyserResult(MediaAnalyserSession session,
								  List<LavfiRawMtdFrame> lavfiMetadatas,
								  Ebur128Summary ebur128Summary) {

}