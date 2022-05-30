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

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

class FilterParserBaseChainFilter {

	protected final List<FilterParserChars> content;

	FilterParserBaseChainFilter(final List<FilterParserChars> content) {
		this.content = Collections.unmodifiableList(content);
	}

	/**
	 * This should be used only for test purposes
	 * @param content do not manage escape chars, quoted or spaces.
	 */
	FilterParserBaseChainFilter(final String content) {
		this(content.chars()
		        .mapToObj(c -> (char) c)
		        .map(FilterParserChars::new)
		        .collect(Collectors.toUnmodifiableList()));
	}

	@Override
	public String toString() {
		return content.stream()
		        .map(FilterParserChars::toString)
		        .collect(Collectors.joining());
	}

	List<FilterParserChars> getContent() {
		return content;
	}

	@Override
	public int hashCode() {
		return Objects.hash(content);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final var other = (FilterParserBaseChainFilter) obj;
		return Objects.equals(content, other.content);
	}

}
