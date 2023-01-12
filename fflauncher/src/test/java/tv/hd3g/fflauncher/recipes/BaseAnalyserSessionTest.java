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
 * Copyright (C) hdsdi3g for hd3g.tv 2023
 *
 */
package tv.hd3g.fflauncher.recipes;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.MockitoAnnotations.openMocks;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import tv.hd3g.fflauncher.FFbase;

class BaseAnalyserSessionTest {

	BaseAnalyserSession bas;

	@Mock
	ScheduledExecutorService maxExecTimeScheduler;
	@Mock
	FFbase ffbase;

	@BeforeEach
	void init() throws Exception {
		openMocks(this).close();
		bas = new BaseAnalyserSession();
	}

	@AfterEach
	void ends() {
		verifyNoMoreInteractions(maxExecTimeScheduler, ffbase);
	}

	@Test
	void testSetMaxExecutionTime() {
		assertFalse(bas.setMaxExecutionTime(Duration.ZERO, maxExecTimeScheduler));
		assertTrue(bas.setMaxExecutionTime(Duration.ofDays(1), maxExecTimeScheduler));
	}

	@Test
	void testApplyMaxExecTime() {
		bas.applyMaxExecTime(ffbase);
		verifyNoInteractions(maxExecTimeScheduler, ffbase);

		bas.setMaxExecutionTime(Duration.ZERO, maxExecTimeScheduler);
		bas.applyMaxExecTime(ffbase);
		verifyNoInteractions(maxExecTimeScheduler, ffbase);

		bas.setMaxExecutionTime(Duration.ofDays(1), maxExecTimeScheduler);
		bas.applyMaxExecTime(ffbase);

		verify(ffbase, times(1)).setMaxExecTimeScheduler(maxExecTimeScheduler);
		verify(ffbase, times(1)).setMaxExecutionTimeForShortCommands(Duration.ofDays(1).toSeconds(), TimeUnit.SECONDS);
	}
}
