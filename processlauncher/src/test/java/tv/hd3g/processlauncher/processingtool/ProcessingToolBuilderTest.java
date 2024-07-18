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

import static java.util.Optional.empty;
import static java.util.concurrent.Executors.newScheduledThreadPool;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;

import tv.hd3g.commons.testtools.Fake;
import tv.hd3g.commons.testtools.MockToolsExtendsJunit;
import tv.hd3g.processlauncher.Processlauncher;
import tv.hd3g.processlauncher.ProcesslauncherBuilder;
import tv.hd3g.processlauncher.ProcesslauncherLifecycle;
import tv.hd3g.processlauncher.cmdline.CommandLine;
import tv.hd3g.processlauncher.cmdline.ExecutableFinder;
import tv.hd3g.processlauncher.cmdline.Parameters;

@ExtendWith(MockToolsExtendsJunit.class)
class ProcessingToolBuilderTest {

	class PTB extends ProcessingToolBuilder<Object, ParametersProvider, Object, ExecutorWatcher> {

		protected PTB(final String execName,
					  final ExecutorWatcher executorWatcher) {
			super(execName, executorWatcher);
			callbacks.add(callback);
		}

		@Override
		protected ParametersProvider getParametersProvider(final Object so) {
			assertEquals(sourceOrigin, so);
			return parametersProvider;
		}

		@Override
		protected Object compute(final Object so, final ProcesslauncherLifecycle lc) {
			assertEquals(sourceOrigin, so);
			assertEquals(lifeCycle, lc);
			return result;
		}

		@Override
		protected ProcesslauncherBuilder createProcesslauncherBuilder(final CommandLine c) {
			when(commandLine.getExecutable()).thenReturn(execFile);
			when(commandLine.getParameters()).thenReturn(parameters);
			when(commandLine.getExecutableFinder()).thenReturn(empty());
			when(parameters.getParameters()).thenReturn(allParams);
			when(parameters.duplicate()).thenReturn(parameters);

			super.createProcesslauncherBuilder(c);

			verify(commandLine, times(1)).getExecutable();
			verify(commandLine, times(1)).getParameters();
			verify(commandLine, times(1)).getExecutableFinder();
			verify(parameters, times(1)).getParameters();
			verify(parameters, times(1)).duplicate();

			assertEquals(commandLine, c);
			return pBuilder;
		}

		@Override
		protected CommandLine createCommandLine(final Parameters p) throws IOException {
			when(executableFinder.get(execName)).thenReturn(execFile);
			super.createCommandLine(p);
			verify(executableFinder, times(1)).get(execName);

			assertEquals(parameters, p);
			return commandLine;
		}

	}

	@Mock
	ExecutorWatcher executorWatcher;
	@Mock
	ParametersProvider parametersProvider;
	@Mock
	ExecutableFinder executableFinder;
	@Mock
	Object sourceOrigin;
	@Mock
	Object result;
	@Mock
	ProcesslauncherLifecycle lifeCycle;
	@Mock
	ProcessingToolCallback callback;
	@Mock
	Parameters parameters;
	@Mock
	CommandLine commandLine;
	@Mock
	Processlauncher processlauncher;
	@Mock
	ProcesslauncherBuilder pBuilder;
	@Mock
	ScheduledExecutorService maxExecTimeScheduler;

	@Fake
	String execName;
	@Fake
	String fullCommandLine;
	@Fake
	String param;
	@Fake
	File execFile;
	File workingDirectory;

	PTB ptb;
	List<String> allParams;

	@BeforeEach
	void init() {
		ptb = new PTB(execName, executorWatcher);
		ptb.setExecutableFinder(executableFinder);
		workingDirectory = new File(".");
		allParams = List.of(param);
		verifyNoInteractions(callback);
	}

	@AfterEach
	void end() {
		assertThat(execFile).doesNotExist();
	}

	@Test
	void testSetMaxExecutionTime() {
		assertFalse(ptb.setMaxExecutionTime(Duration.ZERO, null));
		assertTrue(ptb.setMaxExecutionTime(Duration.ofDays(1), newScheduledThreadPool(1)));
	}

	@Test
	void testSetWorkingDirectory() throws IOException {
		ptb.setWorkingDirectory(new File("."));

		try {
			ptb.setWorkingDirectory(new File("./DontExists"));
			Assertions.fail();
		} catch (final IOException e) {// NOSONAR
		}
		try {
			ptb.setWorkingDirectory(new File("pom.xml"));
			Assertions.fail();
		} catch (final IOException e) {// NOSONAR
		}
	}

	@ParameterizedTest
	@ValueSource(booleans = { true, false })
	void testProcess(final Boolean addWorkingDirectoryAndMaxExecTimeScheduler) throws IOException {

		when(parametersProvider.getReadyToRunParameters()).thenReturn(parameters);
		when(pBuilder.start()).thenReturn(lifeCycle);
		when(lifeCycle.getFullCommandLine()).thenReturn(fullCommandLine);

		if (addWorkingDirectoryAndMaxExecTimeScheduler) {
			assertTrue(ptb.setMaxExecutionTime(Duration.ofDays(1), maxExecTimeScheduler));
			ptb.setWorkingDirectory(workingDirectory);
		}

		final var pResult = ptb.process(sourceOrigin);
		assertNotNull(pResult);
		assertEquals(ptb, pResult.getBuilder());
		assertEquals(fullCommandLine, pResult.getFullCommandLine());
		assertEquals(result, pResult.getResult());

		verify(callback, times(1)).prepareParameters(parameters);
		verify(callback, times(1)).beforeRun(pBuilder);
		verify(pBuilder, times(1)).addExecutionCallbacker(callback);
		verify(pBuilder, times(1)).start();
		verify(parametersProvider, times(1)).getReadyToRunParameters();
		verify(lifeCycle, atLeastOnce()).getFullCommandLine();
		verify(executorWatcher, times(1)).setupWatcherRun(pBuilder);
		verify(executorWatcher, times(1)).afterStartProcess(lifeCycle);

		if (addWorkingDirectoryAndMaxExecTimeScheduler) {
			verify(pBuilder, times(1)).setExecutionTimeLimiter(Duration.ofDays(1), maxExecTimeScheduler);
			verify(pBuilder, times(1)).setWorkingDirectory(workingDirectory);
		}
	}

	@Test
	void testDryRunCallbacks() {
		ptb.dryRunCallbacks(parameters, pBuilder, lifeCycle);

		final var inOrder = inOrder(callback);
		inOrder.verify(callback, times(1)).prepareParameters(parameters);
		inOrder.verify(callback, times(1)).beforeRun(pBuilder);
		inOrder.verify(callback, times(1)).postStartupExecution(lifeCycle);
		inOrder.verify(callback, times(1)).onEndExecution(lifeCycle);
	}

}
