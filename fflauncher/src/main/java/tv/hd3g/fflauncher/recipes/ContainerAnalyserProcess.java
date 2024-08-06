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
package tv.hd3g.fflauncher.recipes;

import tv.hd3g.fflauncher.ffprobecontainer.FFprobeResultSAX;
import tv.hd3g.fflauncher.processingtool.FFSourceDefinition;
import tv.hd3g.fflauncher.progress.FFprobeXMLProgressWatcher;
import tv.hd3g.processlauncher.ProcesslauncherLifecycle;
import tv.hd3g.processlauncher.processingtool.DirectStdoutGetStderrWatcher;

public class ContainerAnalyserProcess extends
									  ContainerAnalyserBase<ContainerAnalyserProcessResult, DirectStdoutGetStderrWatcher> {

	private final FFprobeResultSAX ffprobeResultSAX;

	protected ContainerAnalyserProcess(final String execName,
									   final FFprobeResultSAX ffprobeResultSAX,
									   final DirectStdoutGetStderrWatcher executorWatcher) {
		super(execName, executorWatcher);
		this.ffprobeResultSAX = ffprobeResultSAX;
		executorWatcher.setStdOutConsumer(ffprobeResultSAX);
	}

	public ContainerAnalyserProcess(final String execName) {
		this(execName, new FFprobeResultSAX(), new DirectStdoutGetStderrWatcher());
	}

	@Override
	public void setProgressWatcher(final FFprobeXMLProgressWatcher progressWatcher) {
		super.setProgressWatcher(progressWatcher);
		progressWatcher.createHandler(this).ifPresent(ffprobeResultSAX::setProgressHandler);
	}

	@Override
	protected ContainerAnalyserProcessResult compute(final FFSourceDefinition sourceOrigin,
													 final ProcesslauncherLifecycle lifeCycle) {
		return ffprobeResultSAX.getResult(lifeCycle.getFullCommandLine());
	}

}
