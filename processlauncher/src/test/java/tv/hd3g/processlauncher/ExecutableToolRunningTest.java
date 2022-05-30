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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.function.Predicate;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

class ExecutableToolRunningTest {
	final static Predicate<String> filterOutErrorLines = p -> true;

	@Mock
	CapturedStdOutErrTextRetention textRetention;
	@Mock
	ProcesslauncherLifecycle lifecyle;
	@Mock
	ExecutableTool execTool;

	ExecutableToolRunning etr;

	@BeforeEach
	void init() throws Exception {
		MockitoAnnotations.openMocks(this).close();
		etr = new ExecutableToolRunning(textRetention, lifecyle, execTool);
	}

	@AfterEach
	void end() {
		verifyNoMoreInteractions(textRetention, lifecyle, execTool);
	}

	@Test
	void testGetTextRetention() {
		assertEquals(textRetention, etr.getTextRetention());
	}

	@Test
	void testGetLifecyle() {
		assertEquals(lifecyle, etr.getLifecyle());
	}

	@Test
	void testCheckExecutionGetText() {
		assertEquals(textRetention, etr.checkExecutionGetText());
		verify(lifecyle, Mockito.times(1)).checkExecution();
		verify(textRetention, Mockito.times(1)).waitForClosedStreams();
	}

	@Test
	void testWaitForEnd() {
		assertEquals(etr, etr.waitForEnd());
		verify(lifecyle, Mockito.times(1)).waitForEnd();
	}

	@Test
	void testWaitForEndAndCheckExecution() {
		assertEquals(etr, etr.waitForEndAndCheckExecution());
		verify(lifecyle, Mockito.times(1)).waitForEnd();
		verify(lifecyle, Mockito.times(1)).checkExecution();
	}

	@Test
	void testCheckExecutionGetText_error() {
		Mockito.doThrow(new InvalidExecution(lifecyle)).when(lifecyle).checkExecution();
		when(execTool.filterOutErrorLines()).thenReturn(filterOutErrorLines);
		when(textRetention.getStderrLines(false)).thenReturn(Stream.empty());

		assertThrows(InvalidExecution.class, () -> etr.checkExecutionGetText());

		verify(lifecyle, Mockito.times(1)).checkExecution();
		verify(lifecyle, Mockito.times(2)).getFullCommandLine();
		verify(lifecyle, Mockito.times(2)).getEndStatus();
		verify(lifecyle, Mockito.times(2)).getExitCode();
		verify(textRetention, Mockito.times(1)).getStderrLines(eq(false));
		verify(execTool, Mockito.times(1)).filterOutErrorLines();
	}

	@Test
	void testWaitForEndAndCheckExecution_error() {
		Mockito.doThrow(new InvalidExecution(lifecyle)).when(lifecyle).checkExecution();
		when(execTool.filterOutErrorLines()).thenReturn(filterOutErrorLines);
		when(textRetention.getStderrLines(false)).thenReturn(Stream.empty());

		assertThrows(InvalidExecution.class, () -> etr.waitForEndAndCheckExecution());

		verify(lifecyle, Mockito.times(1)).waitForEnd();
		verify(lifecyle, Mockito.times(1)).checkExecution();
		verify(lifecyle, Mockito.times(2)).getFullCommandLine();
		verify(lifecyle, Mockito.times(2)).getEndStatus();
		verify(lifecyle, Mockito.times(2)).getExitCode();
		verify(textRetention, Mockito.times(1)).getStderrLines(eq(false));
		verify(execTool, Mockito.times(1)).filterOutErrorLines();
	}

}
