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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import tv.hd3g.fflauncher.processingtool.FFSourceDefinition;
import tv.hd3g.fflauncher.progress.FFprobeXMLProgressConsumer;
import tv.hd3g.fflauncher.progress.FFprobeXMLProgressWatcher;
import tv.hd3g.processlauncher.LineEntry;
import tv.hd3g.processlauncher.ProcesslauncherLifecycle;
import tv.hd3g.processlauncher.processingtool.CallbackWatcher;

@ExtendWith(MockToolsExtendsJunit.class)
class ContainerAnalyserExtractTest {

	class CAE extends ContainerAnalyserExtract {

		public CAE(final String execName) {
			super(execName);
			callbackWatcher = executorWatcher;
		}

	}

	@Mock
	FFprobeXMLProgressWatcher progressWatcher;
	@Mock
	FFprobeXMLProgressConsumer fFprobeXMLProgressConsumer;
	@Mock
	ProcesslauncherLifecycle lifeCycle;
	@Mock
	FFSourceDefinition sourceOrigin;

	@Fake
	String execName;
	@Fake
	String stdOutLine;
	@Fake
	String stdErrLine;
	@Fake
	String fullCommandLine;

	CAE cae;
	CallbackWatcher callbackWatcher;

	@BeforeEach
	void init() {
		when(lifeCycle.getFullCommandLine()).thenReturn(fullCommandLine);
		cae = new CAE(execName);
	}

	@Test
	void testSetProgressWatcher_compute() {
		when(progressWatcher.createProgress(cae)).thenReturn(fFprobeXMLProgressConsumer);
		cae.setProgressWatcher(progressWatcher);
		verify(progressWatcher, times(1)).createProgress(cae);

		final var stdOutErrConsumer = callbackWatcher.getStdOutErrConsumer();
		stdOutErrConsumer.accept(new LineEntry(0, stdOutLine, false, lifeCycle));
		stdOutErrConsumer.accept(new LineEntry(0, stdErrLine, true, lifeCycle));

		final var result = cae.compute(sourceOrigin, lifeCycle);
		assertNotNull(result);

		assertEquals(fullCommandLine, result.ffprobeCommandLine());
		assertThat(result.sysOut()).containsExactly(stdOutLine);

		verify(fFprobeXMLProgressConsumer, times(1)).waitForEnd();
		verify(fFprobeXMLProgressConsumer, times(1)).accept(stdOutLine);
		verify(lifeCycle, atLeastOnce()).getFullCommandLine();
	}

	@Test
	void testCompute() {
		final var stdOutErrConsumer = callbackWatcher.getStdOutErrConsumer();
		stdOutErrConsumer.accept(new LineEntry(0, stdOutLine, false, lifeCycle));
		stdOutErrConsumer.accept(new LineEntry(0, stdErrLine, true, lifeCycle));

		final var result = cae.compute(sourceOrigin, lifeCycle);
		assertNotNull(result);

		assertEquals(fullCommandLine, result.ffprobeCommandLine());
		assertThat(result.sysOut()).containsExactly(stdOutLine);
		verify(lifeCycle, atLeastOnce()).getFullCommandLine();
	}

}
