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
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class FilterParser {

	private final FilterParserSimpleChar[] filterChars;

	/**
	 * @param rawFilterChain the full -vf/-af... command, with ",;[]"....
	 */
	FilterParser(final String rawFilterChain) {
		filterChars = new FilterParserSimpleChar[rawFilterChain.length()];
		for (var pos = 0; pos < rawFilterChain.length(); pos++) {
			filterChars[pos] = new FilterParserSimpleChar(rawFilterChain.charAt(pos));
		}
	}

	/**
	 * @return full mutable lists / ordered maps
	 */
	public static <T extends FilterParserDefinition> List<List<T>> fullParsing(final String rawFilterChain,
	                                                                           final Supplier<T> filterDefSupplier) {
		return new ArrayList<>(new FilterParser(rawFilterChain)
		        .getGraphBranchs().stream()
		        .map(graphBranch -> new ArrayList<>(graphBranch.getRawChains().stream()
		                .map(chain -> {
			                final var definition = filterDefSupplier.get();
			                definition.setSourceBlocks(new ArrayList<>(chain.getSourceBlocks()));
			                definition.setDestBlocks(new ArrayList<>(chain.getDestBlocks()));

			                final var filter = chain.getFilter();
			                definition.setFilterName(filter.getFilterName());
			                definition.setArguments(new ArrayList<>(filter.getFilterArguments()));
			                return definition;
		                }).collect(Collectors.toUnmodifiableList())))
		        .collect(Collectors.toUnmodifiableList()));
	}

	/**
	 * @return the original command with escape/quotes securized
	 */
	List<FilterParserChars> getUnescapeAndUnQuoted() {
		final var result = new ArrayList<FilterParserChars>();

		var inEscaped = false;
		var inQuoted = false;
		var captured = new StringBuilder();
		for (var pos = 0; pos < filterChars.length; pos++) {
			final var current = filterChars[pos];

			if (inEscaped) {
				inEscaped = false;
				if (inQuoted) {
					current.write(captured);
				} else {
					result.add(current.toEscapedFilterChars());
				}
			} else if (current.isEscape()) {
				inEscaped = true;
			} else if (current.isQuoted()) {
				current.write(captured);
				if (inQuoted) {
					result.add(new FilterParserChars(captured));
					captured = new StringBuilder();
				}
				inQuoted = inQuoted == false;
			} else if (inQuoted) {
				current.write(captured);
			} else if (current.isSpace() == false) {
				result.add(current.toFilterChars());
			}
		}

		return Collections.unmodifiableList(result);
	}

	/**
	 * @return filter graph branchs (separated with ";")
	 */
	List<FilterParserGraphBranch> getGraphBranchs() {
		final var list = getUnescapeAndUnQuoted();

		final var result = new ArrayList<FilterParserGraphBranch>();
		List<FilterParserChars> chain = new ArrayList<>();

		for (var pos = 0; pos < list.size(); pos++) {
			final var current = list.get(pos);

			if (current.isSemicolon()) {
				if (chain.isEmpty() == false) {
					result.add(new FilterParserGraphBranch(chain));
					chain = new ArrayList<>();
				}
			} else {
				chain.add(current);
			}
		}

		if (chain.isEmpty() == false) {
			result.add(new FilterParserGraphBranch(chain));
		}

		return Collections.unmodifiableList(result);
	}

}
