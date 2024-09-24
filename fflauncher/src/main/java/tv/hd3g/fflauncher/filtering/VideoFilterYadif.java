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
 * https://ffmpeg.org/ffmpeg-filters.html#yadif-1
 */
@Data
public class VideoFilterYadif implements VideoFilterSupplier {

	public enum Mode {

		SEND_FRAME("send_frame"),
		SEND_FIELD("send_field"),
		SEND_FRAME_NO_SPATIAL("send_frame_nospatial"),
		SEND_FIELD_NO_SPATIAL("send_field_nospatial");

		protected final String value;

		Mode(final String value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return value;
		}
	}

	public enum Parity {
		TFF,
		BFF,
		AUTO;

		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}

	public enum Deint {
		ALL,
		INTERLACED;

		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}

	private Mode mode;
	private Parity parity;
	private Deint deint;

	@Override
	public Filter toFilter() {
		final var f = new Filter("yadif");
		f.addOptionalArgument("mode", mode);
		f.addOptionalArgument("parity", parity);
		f.addOptionalArgument("deint", deint);
		return f;
	}

}
