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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static tv.hd3g.processlauncher.EndStatus.KILLED;

import java.util.function.Predicate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import tv.hd3g.commons.testtools.MockToolsExtendsJunit;
import tv.hd3g.processlauncher.DirectStandardOutputStdErrRetention;
import tv.hd3g.processlauncher.InputStreamConsumer;
import tv.hd3g.processlauncher.InvalidExecution;
import tv.hd3g.processlauncher.ProcesslauncherBuilder;
import tv.hd3g.processlauncher.ProcesslauncherLifecycle;

@ExtendWith(MockToolsExtendsJunit.class)
class DirectStdoutGetStderrWatcherTest {
	@Mock
	InputStreamConsumer stdOutConsumer;
	@Mock
	Predicate<String> filterOutErrorLines;
	@Mock
	ProcesslauncherBuilder builder;
	@Mock
	ProcesslauncherLifecycle lifeCycle;

	@Captor
	ArgumentCaptor<DirectStandardOutputStdErrRetention> directStreamsCapture;

	DirectStdoutGetStderrWatcher w;
	InvalidExecution invalidExecution;

	@BeforeEach
	void init() {
		w = new DirectStdoutGetStderrWatcher();
		w.setStdOutConsumer(stdOutConsumer);
		w.setFilterOutErrorLines(filterOutErrorLines);

		when(lifeCycle.getFullCommandLine()).thenReturn("");
		when(lifeCycle.getEndStatus()).thenReturn(KILLED);
		when(lifeCycle.getExitCode()).thenReturn(0);
		invalidExecution = new InvalidExecution(lifeCycle);
	}

	@AfterEach
	void end() {
		verify(lifeCycle, atLeast(1)).getFullCommandLine();
		verify(lifeCycle, atLeast(1)).getEndStatus();
		verify(lifeCycle, atLeast(1)).getExitCode();
	}

	@Test
	void testSetupWatcherRun() {
		w.setupWatcherRun(builder);
		verify(builder, times(1)).setCaptureStandardOutput(directStreamsCapture.capture());
		assertNotNull(directStreamsCapture.getValue());
	}

	@Test
	void testAfterStartProcess() {
		w.afterStartProcess(lifeCycle);
		verify(lifeCycle, times(1)).checkExecution();
	}

	@Test
	void testAfterStartProcess_withError() {
		when(lifeCycle.checkExecution()).thenThrow(invalidExecution);
		assertThrows(InvalidExecution.class, () -> w.afterStartProcess(lifeCycle));
		verify(lifeCycle, times(1)).checkExecution();
	}

}
