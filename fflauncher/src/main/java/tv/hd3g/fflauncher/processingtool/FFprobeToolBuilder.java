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
package tv.hd3g.fflauncher.processingtool;

import static java.util.Objects.requireNonNull;

import java.io.File;

import lombok.Getter;
import tv.hd3g.fflauncher.FFprobe;
import tv.hd3g.processlauncher.processingtool.ExecutorWatcher;
import tv.hd3g.processlauncher.processingtool.ProcessingToolBuilder;
import tv.hd3g.processlauncher.processingtool.ProcessingToolResult;

public abstract class FFprobeToolBuilder<T, W extends ExecutorWatcher>
										extends ProcessingToolBuilder<FFSourceDefinition, FFprobe, T, W> {

	@Getter
	protected final FFprobe ffprobe;

	protected FFprobeToolBuilder(final FFprobe ffprobe, final W watcher) {
		super(ffprobe.getExecutableName(), watcher);
		this.ffprobe = requireNonNull(ffprobe, "\"ffprobe\" can't to be null");
		callbacks.add(ffprobe.makeConversionHooks());
	}

	@Override
	protected FFprobe getParametersProvider(final FFSourceDefinition sourceOrigin) {
		sourceOrigin.applySourceToFF(ffprobe);
		return ffprobe;
	}

	public final ProcessingToolResult<FFSourceDefinition, FFprobe, T, W> process(final File source) {
		return process(f -> f.addSimpleInputSource(source));
	}

	public final ProcessingToolResult<FFSourceDefinition, FFprobe, T, W> process(final String source) {
		return process(f -> f.addSimpleInputSource(source));
	}

}
