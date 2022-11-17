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
 * https://www.ffmpeg.org/ffmpeg-filters.html#metadata_002c-ametadata
 * Non thread safe
 */
@Data
public class VideoFilterMetadata implements FilterSupplier {

	public enum Mode {
		SELECT,
		ADD,
		MODIFY,
		DELETE,
		PRINT;

		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}

	public enum Function {
		SAME_STR("same_str"),
		STARTS_WITH("starts_with"),
		LESS("less"),
		EQUAL("equal"),
		GREATER("greater"),
		EXPR("expr"),
		ENDS_WITH("ends_with");

		private final String value;

		Function(final String value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return value;
		}
	}

	private final Mode mode;
	private String key;
	private String value;
	private Function function;
	private String expr;
	private String file;
	private boolean direct;

	public VideoFilterMetadata(final Mode mode) {
		this.mode = mode;
	}

	@Override
	public Filter toFilter() {
		final var f = new Filter("metadata", new FilterArgument("mode", mode));
		f.addOptionalArgument("key", key);
		f.addOptionalArgument("value", value);
		f.addOptionalArgument("function", function);
		f.addOptionalArgument("expr", expr);
		f.addOptionalArgument("file", file);
		f.addOptionalArgument("direct", direct);
		return f;
	}

}
