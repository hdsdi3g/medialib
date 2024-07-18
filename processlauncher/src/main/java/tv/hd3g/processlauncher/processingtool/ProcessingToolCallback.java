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

import tv.hd3g.processlauncher.ExecutionCallbacker;
import tv.hd3g.processlauncher.ProcesslauncherBuilder;
import tv.hd3g.processlauncher.ProcesslauncherLifecycle;
import tv.hd3g.processlauncher.cmdline.Parameters;

public interface ProcessingToolCallback extends ExecutionCallbacker {

	/**
	 * 1/4
	 */
	default void prepareParameters(final Parameters parameters) {
	}

	/**
	 * 2/4
	 */
	default void beforeRun(final ProcesslauncherBuilder pBuilder) {
	}

	/**
	 * 3/4
	 */
	@Override
	default void postStartupExecution(final ProcesslauncherLifecycle processlauncherLifecycle) {
	}

	/**
	 * 4/4
	 */
	@Override
	default void onEndExecution(final ProcesslauncherLifecycle processlauncherLifecycle) {
	}

}
