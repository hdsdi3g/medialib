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
 * Copyright (C) hdsdi3g for hd3g.tv 2020
 *
 */
package tv.hd3g.fflauncher.filtering.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import tv.hd3g.fflauncher.filtering.FilterArgument;

class FilterParserFilter extends FilterParserBaseChainFilter {

	FilterParserFilter(final List<FilterParserChars> content) {
		super(content);
	}

	FilterParserFilter(final String content) {
		super(content);
	}

	String getFilterName() {
		return content.stream()
				.takeWhile(c -> c.isEquals() == false)
				.map(FilterParserChars::toString)
				.collect(Collectors.joining());
	}

	List<FilterArgument> getFilterArguments() {
		final var sub = content.stream()
				.dropWhile(c -> c.isEquals() == false)
				.skip(1)
				.toList();

		final var entries = new ArrayList<FilterArgument>();
		var currentParam = new ArrayList<FilterParserChars>();
		for (var pos = 0; pos < sub.size(); pos++) {
			final var current = sub.get(pos);

			if (current.isColon()) {
				if (currentParam.isEmpty() == false) {
					entries.add(getSetupKV(currentParam));
					currentParam = new ArrayList<>();
				}
			} else {
				currentParam.add(current);
			}
		}
		if (currentParam.isEmpty() == false) {
			entries.add(getSetupKV(currentParam));
		}

		return Collections.unmodifiableList(entries);
	}

	static FilterArgument getSetupKV(final List<FilterParserChars> param) {
		String key = null;
		String value = null;

		var sb = new StringBuilder();
		for (var pos = 0; pos < param.size(); pos++) {
			final var current = param.get(pos);

			if (current.isEquals()) {
				if (sb.length() == 0) {
					continue;
				}
				if (key == null) {
					key = sb.toString();
				} else {
					value = sb.toString();
				}
				sb = new StringBuilder();
			} else {
				current.write(sb);
			}
		}

		if (sb.length() > 0) {
			if (key == null) {
				key = sb.toString();
			} else {
				value = sb.toString();
			}
		}

		return new FilterArgument(key, value);
	}

}
