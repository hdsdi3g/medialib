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

class FilterParserChain extends FilterParserBaseChainFilter {
	private static final String INVALID_MISSING = "Invalid ], missing [ in ";

	FilterParserChain(final List<FilterParserChars> content) {
		super(content);
	}

	FilterParserChain(final String content) {
		super(content);
	}

	List<String> getSourceBlocks() {
		final var list = new ArrayList<String>();

		StringBuilder captured = null;
		for (var pos = 0; pos < content.size(); pos++) {
			final var current = content.get(pos);

			if (current.isBracketOpen()) {
				captured = new StringBuilder();
			} else if (current.isBracketClose()) {
				if (captured != null) {
					list.add(captured.toString());
					captured = null;
				} else {
					throw new IllegalArgumentException(INVALID_MISSING + toString());
				}
			} else if (captured != null) {
				current.write(captured);
			} else {
				break;
			}
		}

		return Collections.unmodifiableList(list);
	}

	List<String> getDestBlocks() {// NOSONAR S3776
		final var list = new ArrayList<String>();

		StringBuilder capture = null;
		var isAfterSource = false;
		var isInSourceBlocks = false;
		var isInDestBlock = false;

		for (var pos = 0; pos < content.size(); pos++) {
			final var current = content.get(pos);

			/**
			 * Pass the first brackets zone
			 */
			if (isAfterSource == false) {
				if (current.isBracketOpen()) {
					isInSourceBlocks = true;
				} else if (current.isBracketClose() && isInSourceBlocks) {
					isInSourceBlocks = false;
				} else if (current.isBracketClose() && isInSourceBlocks == false) {
					throw new IllegalArgumentException(INVALID_MISSING + toString());
				} else if (isInSourceBlocks == false) {
					isAfterSource = true;
				}
			} else {
				/**
				 * Pass the filter setup zone
				 */
				if (isInDestBlock == false) {
					if (current.isBracketOpen()) {
						isInDestBlock = true;
					} else if (current.isBracketClose()) {
						throw new IllegalArgumentException(INVALID_MISSING + toString());
					} else {
						continue;
					}
				}

				/**
				 * The end brackets zone
				 */
				if (current.isBracketOpen()) {
					capture = new StringBuilder();
				} else if (current.isBracketClose()) {
					if (capture != null) {
						list.add(capture.toString());
						capture = null;
					} else {
						throw new IllegalArgumentException(INVALID_MISSING + toString());
					}
				} else if (capture != null) {
					current.write(capture);
				} else {
					throw new IllegalArgumentException("Invalid content after dest blocks in " + toString());
				}
			}
		}

		if (capture != null) {
			throw new IllegalArgumentException("Missing ] on the end in " + toString());
		}

		return Collections.unmodifiableList(list);
	}

	FilterParserFilter getFilter() {
		final var list = new ArrayList<FilterParserChars>();

		var isAfterSource = false;
		var isInSourceBlocks = false;
		for (var pos = 0; pos < content.size(); pos++) {// NOSONAR S135
			final var current = content.get(pos);

			/**
			 * Pass the first brackets zone
			 */
			if (isAfterSource == false) {
				if (current.isBracketOpen()) {
					isInSourceBlocks = true;
					continue;
				} else if (current.isBracketClose() && isInSourceBlocks) {
					isInSourceBlocks = false;
					continue;
				} else if (current.isBracketClose() && isInSourceBlocks == false) {
					throw new IllegalArgumentException(INVALID_MISSING + toString());
				} else if (isInSourceBlocks == false) {
					isAfterSource = true;
				} else {
					continue;
				}
			}

			if (current.isBracketOpen()
			    || current.isBracketClose()) {
				/**
				 * The end brackets zone
				 */
				break;
			}

			list.add(current);
		}

		if (isInSourceBlocks) {
			throw new IllegalArgumentException("Missing ] in " + toString());
		}

		return new FilterParserFilter(list);
	}

}
