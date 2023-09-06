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
package tv.hd3g.fflauncher.filtering;

import lombok.Data;

/**
 * https://www.ffmpeg.org/ffmpeg-filters.html#idet
 * No thread safe
 */
@Data
public class VideoFilterIdet implements VideoFilterSupplier {

	private float intlThres;
	private float progThres;
	private float repThres;
	private int halfLife;
	private boolean analyzeInterlacedFlag;

	public VideoFilterIdet() {
		intlThres = -1;
		progThres = -1;
		repThres = -1;
		halfLife = -1;
	}

	@Override
	public Filter toFilter() {
		final var f = new Filter("idet");
		f.addOptionalArgument("analyze_interlaced_flag", analyzeInterlacedFlag, "1");
		f.addOptionalNonNegativeArgument("intl_thres", intlThres);
		f.addOptionalNonNegativeArgument("prog_thres", progThres);
		f.addOptionalNonNegativeArgument("rep_thres", repThres);
		f.addOptionalNonNegativeArgument("half_life", halfLife);
		return f;
	}

}
