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
 * Copyright (C) hdsdi3g for hd3g.tv 2021
 *
 */
package tv.hd3g.processlauncher;

import java.util.stream.Collectors;

public class ExecutableToolRunning {
	private final CapturedStdOutErrTextRetention textRetention;
	private final ProcesslauncherLifecycle lifecyle;
	private final ExecutableTool execTool;

	ExecutableToolRunning(final CapturedStdOutErrTextRetention textRetention,
	                      final ProcesslauncherLifecycle lifecyle,
	                      final ExecutableTool execTool) {
		this.textRetention = textRetention;
		this.lifecyle = lifecyle;
		this.execTool = execTool;
	}

	public CapturedStdOutErrTextRetention getTextRetention() {
		return textRetention;
	}

	public ProcesslauncherLifecycle getLifecyle() {
		return lifecyle;
	}

	/**
	 * Can throw an InvalidExecution, with stderr embedded.
	 * Blocking call (with CapturedStdOutErrTextRetention::waitForClosedStream)
	 */
	public CapturedStdOutErrTextRetention checkExecutionGetText() {
		try {
			lifecyle.checkExecution();
			textRetention.waitForClosedStreams();
			return textRetention;
		} catch (final InvalidExecution e) {
			throw e.injectStdErr(textRetention.getStderrLines(false)
			        .filter(execTool.filterOutErrorLines())
			        .map(String::trim).collect(Collectors.joining("|")));
		}
	}

	/**
	 * Don't checks end status (ok/error).
	 * @return this
	 */
	public ExecutableToolRunning waitForEnd() {
		lifecyle.waitForEnd();
		return this;
	}

	/**
	 * Merge waitForEnd and checkExecutionGetText behaviors
	 */
	public ExecutableToolRunning waitForEndAndCheckExecution() {
		waitForEnd();
		try {
			lifecyle.checkExecution();
		} catch (final InvalidExecution e) {
			throw e.injectStdErr(textRetention.getStderrLines(false)
			        .filter(execTool.filterOutErrorLines())
			        .map(String::trim).collect(Collectors.joining("|")));
		}
		return this;
	}

}
