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

import static org.apache.logging.log4j.Level.ALL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import tv.hd3g.processlauncher.cmdline.ExecutableFinder;
import tv.hd3g.processlauncher.cmdline.Parameters;

class ExecutableToolTest {

	String execName;
	ExecutableFinder executableFinder;
	Parameters parameters;
	ExecutableTool exec;

	@BeforeEach
	void init() throws Exception {
		execName = "java";
		executableFinder = new ExecutableFinder();
		parameters = Parameters.of("-version");

		exec = new ExecutableTool() {

			@Override
			public String getExecutableName() {
				return execName;
			}

			@Override
			public Parameters getReadyToRunParameters() {
				return parameters;
			}
		};
	}

	@Test
	void testExecute() throws InterruptedException, ExecutionException, TimeoutException {
		final var result = exec.execute(executableFinder);

		final var capturedStdOutErrTextRetention = result.getTextRetention();
		assertNotNull(capturedStdOutErrTextRetention);
		assertNotNull(result.getLifecyle());

		assertEquals(capturedStdOutErrTextRetention, result.checkExecutionGetText());
		assertTrue(capturedStdOutErrTextRetention.getStdouterrLines(false)
		        .anyMatch(line -> line.contains("version")));
	}

	@Test
	void checkExecutionGetText_withError() {
		parameters.clear().addBulkParameters("-thiswillneverexec");
		final var result = exec.execute(executableFinder);

		final var afterWait = result.waitForEnd();
		assertNotNull(afterWait);
		assertThrows(InvalidExecution.class, () -> afterWait.checkExecutionGetText());
	}

	@Test
	void waitForEnd_isReallydone() {
		final var result = exec.execute(executableFinder);
		assertTrue(result.waitForEnd().getLifecyle().isCorrectlyDone());
	}

	@Test
	void waitForEndAndCheckExecution_ok() {
		final var result = exec.execute(executableFinder).waitForEndAndCheckExecution();
		assertNotNull(result);
		assertTrue(result.getLifecyle().isCorrectlyDone());

		final var capturedStdOutErrTextRetention = result.getTextRetention();
		assertNotNull(capturedStdOutErrTextRetention);
		assertNotNull(result.getLifecyle());

		assertEquals(capturedStdOutErrTextRetention, result.checkExecutionGetText());
		assertTrue(capturedStdOutErrTextRetention.getStdouterrLines(false)
		        .anyMatch(line -> line.contains("version")));
	}

	@Test
	void waitForEndAndCheckExecution_error() {
		parameters.clear().addBulkParameters("-thiswillneverexec");
		final var result = exec.execute(executableFinder);
		assertThrows(InvalidExecution.class, () -> result.waitForEndAndCheckExecution());
	}

	@Test
	void testExecute_Logger() {
		final var log = Mockito.mock(Logger.class);
		when(log.isEnabled(ALL)).thenReturn(true);

		final var lines = new LinkedBlockingQueue<LineEntry>();
		final var result = exec.execute(executableFinder, log, line -> {
			lines.add(line);
			return ALL;
		});
		result.waitForEnd();

		final var msgs = ArgumentCaptor.forClass(Object.class);
		verify(log, atLeastOnce()).log(eq(ALL), msgs.capture());
		assertTrue(lines.size() > 0);
		assertEquals(msgs.getAllValues().size(), lines.size());

		verify(log, atLeastOnce()).isEnabled(eq(ALL));
		verifyNoMoreInteractions(log);
	}

}
