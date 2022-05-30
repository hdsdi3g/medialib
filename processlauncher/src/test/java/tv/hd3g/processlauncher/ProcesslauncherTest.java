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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ProcesslauncherTest {

	private final List<ExecutionCallbacker> executionCallbackers;
	private final ExecutionTimeLimiter executionTimeLimiter;
	private final CaptureStandardOutput captureStandardOutput;
	private final ExternalProcessStartup externalProcessStartup;
	private final ProcessBuilder processBuilder;
	private final String fullCommandLine;
	private final ProcesslauncherBuilder processlauncherBuilder;

	private final Processlauncher pl;

	ProcesslauncherTest() {
		executionCallbackers = new ArrayList<>();
		executionTimeLimiter = Mockito.mock(ExecutionTimeLimiter.class);
		captureStandardOutput = Mockito.mock(CaptureStandardOutput.class);
		externalProcessStartup = Mockito.mock(ExternalProcessStartup.class);
		processBuilder = new ProcessBuilder("");
		fullCommandLine = "aaa bbb ccc";
		processlauncherBuilder = Mockito.mock(ProcesslauncherBuilder.class);

		Mockito.when(processlauncherBuilder.getExecutionCallbackers())
		        .thenReturn(executionCallbackers);
		Mockito.when(processlauncherBuilder.getExecutionTimeLimiter())
		        .thenReturn(Optional.ofNullable(executionTimeLimiter));
		Mockito.when(processlauncherBuilder.getCaptureStandardOutput())
		        .thenReturn(Optional.ofNullable(captureStandardOutput));
		Mockito.when(processlauncherBuilder.getExternalProcessStartup())
		        .thenReturn(Optional.ofNullable(externalProcessStartup));
		Mockito.when(processlauncherBuilder.makeProcessBuilder()).thenReturn(processBuilder);
		Mockito.when(processlauncherBuilder.getFullCommandLine()).thenReturn(fullCommandLine);

		pl = new Processlauncher(processlauncherBuilder);
	}

	@Test
	void testGetExecutionCallbackers() {
		assertTrue(CollectionUtils.isEqualCollection(executionCallbackers, pl.getExecutionCallbackers()));
	}

	@Test
	void testGetExecutionTimeLimiter() {
		assertEquals(executionTimeLimiter, pl.getExecutionTimeLimiter().get());
	}

	@Test
	void testGetCaptureStandardOutput() {
		assertEquals(captureStandardOutput, pl.getCaptureStandardOutput().get());
	}

	@Test
	void testGetExternalProcessStartup() {
		assertEquals(externalProcessStartup, pl.getExternalProcessStartup().get());
	}

	@Test
	void testIsExecCodeMustBeZero() {
		assertFalse(pl.isExecCodeMustBeZero());
	}

	@Test
	void testGetProcessBuilder() {
		assertEquals(processBuilder, pl.getProcessBuilder());
	}

	@Test
	void testGetFullCommandLine() {
		assertEquals(fullCommandLine, pl.getFullCommandLine());
	}

	@Test
	void testGetProcesslauncherBuilder() {
		assertEquals(processlauncherBuilder, pl.getProcesslauncherBuilder());
	}

	@Test
	void testToString() {
		assertEquals(fullCommandLine, pl.toString());
	}

	@Test
	void testGetExecutableName() {
		assertEquals(processlauncherBuilder.getExecutableName(), pl.getExecutableName());
	}

}
