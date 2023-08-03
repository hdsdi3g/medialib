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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.event.Level;
import org.slf4j.spi.LoggingEventBuilder;

import net.datafaker.Faker;
import tv.hd3g.processlauncher.cmdline.ExecutableFinder;
import tv.hd3g.processlauncher.cmdline.Parameters;

class ExecutableToolTest {

	String execName;
	ExecutableFinder executableFinder;
	Parameters parameters;
	ExecutableTool exec;

	@Mock
	Consumer<ProcesslauncherBuilder> consumerLauncher;
	@Captor
	ArgumentCaptor<ProcesslauncherBuilder> launcherCaptor;

	@BeforeEach
	void init() throws Exception {
		openMocks(this).close();

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

			@Override
			public Consumer<ProcesslauncherBuilder> beforeExecute() {
				return consumerLauncher;
			}
		};
	}

	@AfterEach
	void ends() {
		verify(consumerLauncher, times(1)).accept(launcherCaptor.capture());
		assertNotNull(launcherCaptor.getValue());

		verifyNoMoreInteractions(consumerLauncher);
	}

	@Test
	void testExecute_logger() throws InterruptedException, ExecutionException, TimeoutException {
		final var result = exec.execute(executableFinder);

		final var capturedStdOutErrTextRetention = result.getTextRetention();
		assertNotNull(capturedStdOutErrTextRetention);
		assertNotNull(result.getLifecyle());

		assertEquals(capturedStdOutErrTextRetention, result.checkExecutionGetText());
		assertTrue(capturedStdOutErrTextRetention.getStdouterrLines(false)
				.anyMatch(line -> line.contains("version")));
	}

	@Test
	void testExecute_stdouterrlogger() throws InterruptedException, ExecutionException, TimeoutException {
		final var lines = new ArrayList<LineEntry>();
		final var result = exec.execute(executableFinder, lines::add);
		assertNotNull(result);
		result.waitForEnd();
		assertTrue(lines.stream().map(LineEntry::getLine).anyMatch(line -> line.contains("version")));
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
		final var selectedLevel = Faker.instance().options().option(Level.class);
		final var log = Mockito.mock(Logger.class);
		final var event = Mockito.mock(LoggingEventBuilder.class);

		when(log.isEnabledForLevel(selectedLevel)).thenReturn(true);
		when(log.atLevel(selectedLevel)).thenReturn(event);

		final var lines = new LinkedBlockingQueue<LineEntry>();
		final var result = exec.execute(executableFinder, log, line -> {
			lines.add(line);
			return selectedLevel;
		});
		result.waitForEnd();

		verify(log, atLeastOnce()).atLevel(selectedLevel);
		verify(log, atLeastOnce()).isEnabledForLevel(selectedLevel);

		final var msgs = ArgumentCaptor.forClass(String.class);
		verify(event, atLeastOnce()).log(msgs.capture());
		assertTrue(msgs.getValue().contains("❌"));
		assertTrue(msgs.getValue().contains("java"));
		
		verifyNoMoreInteractions(log, event);
	}

}
