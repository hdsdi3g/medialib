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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.atLeastOnce;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import tv.hd3g.commons.testtools.Fake;
import tv.hd3g.commons.testtools.MockToolsExtendsJunit;
import tv.hd3g.fflauncher.ffprobecontainer.FFprobeResultSAX;
import tv.hd3g.fflauncher.processingtool.FFSourceDefinition;
import tv.hd3g.processlauncher.ProcesslauncherLifecycle;
import tv.hd3g.processlauncher.processingtool.DirectStdoutGetStderrWatcher;

@ExtendWith(MockToolsExtendsJunit.class)
class ContainerAnalyserProcessTest {

	class CAP extends ContainerAnalyserProcess {

		public CAP(final String execName,
				   final FFprobeResultSAX ffprobeResultSAX,
				   final DirectStdoutGetStderrWatcher executorWatcher) {
			super(execName, ffprobeResultSAX, executorWatcher);

		}

	}

	@Mock
	ProcesslauncherLifecycle lifeCycle;
	@Mock
	FFSourceDefinition sourceOrigin;
	@Mock
	FFprobeResultSAX ffprobeResultSAX;
	@Mock
	DirectStdoutGetStderrWatcher executorWatcher;

	@Fake
	String execName;
	@Fake
	String fullCommandLine;

	CAP cap;

	@BeforeEach
	void init() {
		when(lifeCycle.getFullCommandLine()).thenReturn(fullCommandLine);
		cap = new CAP(execName, ffprobeResultSAX, executorWatcher);
		verify(executorWatcher, times(1)).setStdOutConsumer(ffprobeResultSAX);

		new ContainerAnalyserProcess(execName);
	}

	@Test
	void testCompute() {
		cap.compute(sourceOrigin, lifeCycle);

		verify(ffprobeResultSAX, times(1)).getResult(fullCommandLine);
		verify(lifeCycle, atLeastOnce()).getFullCommandLine();
	}

}
