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
package tv.hd3g.fflauncher.filtering;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class VideoFilterFormat implements VideoFilterSupplier {

	private List<String> pixFmts;
	private List<String> colorSpaces;
	private List<String> colorRanges;

	public VideoFilterFormat() {
		pixFmts = new ArrayList<>();
		colorSpaces = new ArrayList<>();
		colorRanges = new ArrayList<>();
	}

	@Override
	public Filter toFilter() {
		final var f = new Filter("format");
		f.addArgument("pix_fmts", pixFmts, "|");
		f.addArgument("color_spaces", colorSpaces, "|");
		f.addArgument("color_ranges", colorRanges, "|");
		return f;
	}

}