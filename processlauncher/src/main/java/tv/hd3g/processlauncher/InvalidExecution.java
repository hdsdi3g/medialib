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
 * Copyright (C) hdsdi3g for hd3g.tv 2019
 *
 */
package tv.hd3g.processlauncher;

import java.util.Optional;

public class InvalidExecution extends RuntimeException {

	private final String stdErr;
	private final transient ProcesslauncherLifecycle processlauncherLifecycle;

	InvalidExecution(final ProcesslauncherLifecycle processlauncherLifecycle) {
		super("Can't execute correcly " + processlauncherLifecycle.getFullCommandLine() + " ["
		      + processlauncherLifecycle.getEndStatus() + "#" + processlauncherLifecycle.getExitCode() + "]");
		this.processlauncherLifecycle = processlauncherLifecycle;
		stdErr = null;
	}

	InvalidExecution(final ProcesslauncherLifecycle processlauncherLifecycle, final String stdErr) {
		super("Can't execute correcly " + processlauncherLifecycle.getFullCommandLine() + " ["
		      + processlauncherLifecycle.getEndStatus() + "#" + processlauncherLifecycle.getExitCode() + "]");
		this.processlauncherLifecycle = processlauncherLifecycle;
		this.stdErr = stdErr;
	}

	public InvalidExecution injectStdErr(final String stdErr) {
		return new InvalidExecution(processlauncherLifecycle, stdErr);
	}

	public synchronized String getStdErr() {
		return stdErr;
	}

	@Override
	public String getMessage() {
		return super.getMessage() + Optional.ofNullable(stdErr)
		        .filter(s -> s.isEmpty() == false)
		        .map(s -> " return \"" + s + "\"")
		        .orElse("");
	}

}
