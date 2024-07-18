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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import tv.hd3g.commons.testtools.Fake;
import tv.hd3g.commons.testtools.MockToolsExtendsJunit;
import tv.hd3g.fflauncher.processingtool.FFSourceDefinition;
import tv.hd3g.fflauncher.progress.FFprobeXMLProgressWatcher;
import tv.hd3g.processlauncher.ProcesslauncherLifecycle;
import tv.hd3g.processlauncher.processingtool.ExecutorWatcher;

@ExtendWith(MockToolsExtendsJunit.class)
class ContainerAnalyserBaseTest {

	class CAB extends ContainerAnalyserBase<Object, ExecutorWatcher> {

		protected CAB(final String execName, final ExecutorWatcher executorWatcher) {
			super(execName, executorWatcher);
		}

		@Override
		protected Object compute(final FFSourceDefinition sourceOrigin, final ProcesslauncherLifecycle lifeCycle) {
			throw new UnsupportedOperationException();
		}

		FFprobeXMLProgressWatcher getProgressWatcher() {
			return progressWatcher;
		}
	}

	@Mock
	FFprobeXMLProgressWatcher watcher;
	@Mock
	ExecutorWatcher executorWatcher;

	@Fake
	String execName;

	CAB cab;

	@BeforeEach
	void init() {
		cab = new CAB(execName, executorWatcher);
		cab.setProgressWatcher(watcher);
	}

	@Test
	void testGetWatcher() {
		assertEquals(watcher, cab.getProgressWatcher());
		cab.setProgressWatcher(null);
		assertEquals(null, cab.getProgressWatcher());
	}

	@Test
	void testGetFfprobe() {
		assertEquals("-hide_banner -show_frames -show_packets -print_format xml",
				cab.getFfprobe().getInternalParameters().toString());
	}

}
