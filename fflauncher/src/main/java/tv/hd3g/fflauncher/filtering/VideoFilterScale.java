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

import lombok.Data;

/**
 * https://ffmpeg.org/ffmpeg-filters.html#scale-1
 */
@Data
public class VideoFilterScale implements VideoFilterSupplier {

	public enum Eval {
		INIT,
		FRAME;

		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}

	public enum Interl {
		FORCE(1),
		NOT_APPLY(0),
		ONLY_FLAGGED(-1);

		protected final int value;

		Interl(final int value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return String.valueOf(value);
		}
	}

	public enum ChromaSampleLocation {
		AUTO,
		UNKNOWN,
		LEFT,
		CENTER,
		TOPLEFT,
		TOP,
		BOTTOMLEFT,
		BOTTOM;

		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}

	public enum ForceOriginalAspectRatio {
		DISABLE,
		DECREASE,
		INCREASE;

		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}

	private String width;
	private String height;
	private Eval eval;
	private Interl interl;
	private String flags;
	private String param0;
	private String param1;
	private String size;
	private String inColorMatrix;
	private String outColorMatrix;
	private String inRange;
	private String outRange;
	private ChromaSampleLocation inChromaLoc;
	private ChromaSampleLocation outChromaLoc;
	private ForceOriginalAspectRatio forceOriginalAspectRatio;
	private String forceDivisibleBy;

	@Override
	public Filter toFilter() {
		final var f = new Filter("scale");
		f.addOptionalArgument("width", width);
		f.addOptionalArgument("height", height);
		f.addOptionalArgument("eval", eval);
		f.addOptionalArgument("interl", interl);
		f.addOptionalArgument("flags", flags);
		f.addOptionalArgument("param0", param0);
		f.addOptionalArgument("param1", param1);
		f.addOptionalArgument("size", size);
		f.addOptionalArgument("in_color_matrix", inColorMatrix);
		f.addOptionalArgument("out_color_matrix", outColorMatrix);
		f.addOptionalArgument("in_range", inRange);
		f.addOptionalArgument("out_range", outRange);
		f.addOptionalArgument("in_chroma_loc", inChromaLoc);
		f.addOptionalArgument("out_chroma_loc", outChromaLoc);
		f.addOptionalArgument("force_original_aspect_ratio", forceOriginalAspectRatio);
		f.addOptionalArgument("force_divisible_by", forceDivisibleBy);
		return f;
	}

}
