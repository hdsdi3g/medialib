/*
 * This file is part of processlauncher.
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
package tv.hd3g.processlauncher.processingtool;

import static java.util.Objects.requireNonNull;

import lombok.Getter;

/**
 * O -> Session S -> Result<T> -> T
 * @param <O> Origin source type
 * @param <P> Setup (command line parameters)
 * @param <T> type handled by result (final produced type)
 * @param <W> Process watcher
 */
@Getter
public final class ProcessingToolResult<O, P extends ParametersProvider, T, W extends ExecutorWatcher> {

	private final ProcessingToolBuilder<O, P, T, W> builder;
	private final T result;
	private final String fullCommandLine;

	public ProcessingToolResult(final ProcessingToolBuilder<O, P, T, W> builder,
								final String fullCommandLine,
								final T processedResult) {
		this.builder = requireNonNull(builder, "\"builder\" can't to be null");
		this.fullCommandLine = requireNonNull(fullCommandLine, "\"fullCommandLine\" can't to be null");
		result = processedResult;
	}

}
