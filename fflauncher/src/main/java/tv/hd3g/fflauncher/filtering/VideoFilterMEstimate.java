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
 * https://www.ffmpeg.org/ffmpeg-filters.html#mestimate
 * No thread safe
 */
@Data
public class VideoFilterMEstimate implements VideoFilterSupplier {

	public enum Method {

		ESA,
		TSS,
		TDLS,
		NTSS,
		FSS,
		DS,
		HEXBS,
		EPZS,
		UMH;

		@Override
		public String toString() {
			return name().toLowerCase();
		}

	}

	private Method method;
	private int mbSize;
	private int searchParam;

	public VideoFilterMEstimate() {
		mbSize = -1;
		searchParam = -1;
	}

	@Override
	public Filter toFilter() {
		final var f = new Filter("mestimate");
		f.addOptionalArgument("method", method);
		f.addOptionalNonNegativeArgument("mb_size", mbSize);
		f.addOptionalNonNegativeArgument("search_param", searchParam);
		return f;
	}

}
