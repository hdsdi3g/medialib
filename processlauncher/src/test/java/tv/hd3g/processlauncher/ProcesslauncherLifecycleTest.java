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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import tv.hd3g.processlauncher.cmdline.CommandLine;
import tv.hd3g.processlauncher.cmdline.ExecutableFinder;
import tv.hd3g.processlauncher.cmdline.Parameters;
import tv.hd3g.processlauncher.demo.DemoExecEmpty;

class ProcesslauncherLifecycleTest {

	private final long beforeStartDate;
	private final long afterEndDate;
	private final Processlauncher launcher;
	private final ProcesslauncherBuilder processlauncherBuilder;
	private final ProcesslauncherLifecycle p;

	ProcesslauncherLifecycleTest() throws IOException {
		beforeStartDate = System.currentTimeMillis() - 100;
		final var parameters = Parameters.of("-cp", System.getProperty("java.class.path"), DemoExecEmpty.class
		        .getName());
		final var cmd = new CommandLine("java", parameters, new ExecutableFinder());
		processlauncherBuilder = new ProcesslauncherBuilder(cmd);
		launcher = new Processlauncher(processlauncherBuilder);
		p = new ProcesslauncherLifecycle(launcher).waitForEnd(500, TimeUnit.MILLISECONDS);
		afterEndDate = System.currentTimeMillis() + 100;
	}

	@Test
	void testStatues() {
		assertEquals(launcher, p.getLauncher());
		assertNotNull(p.getProcess());
		assertFalse(p.getProcess().isAlive());
		assertFalse(p.isRunning());
		assertFalse(p.isKilled());
		assertFalse(p.isTooLongTime());
		assertEquals(0, (int) p.getExitCode());
		assertEquals(EndStatus.CORRECTLY_DONE, p.getEndStatus());
		assertTrue(p.isCorrectlyDone());

		MatcherAssert.assertThat(beforeStartDate, Matchers.lessThanOrEqualTo(p.getStartDate()));
		MatcherAssert.assertThat(afterEndDate, Matchers.greaterThanOrEqualTo(p.getEndDate()));

		MatcherAssert.assertThat(0L, Matchers.lessThan(p.getUptime(TimeUnit.NANOSECONDS)));
		MatcherAssert.assertThat(0L, Matchers.lessThanOrEqualTo(p.getCPUDuration(TimeUnit.NANOSECONDS)));
		assertNotNull(p.getUserExec());
		assertTrue(p.getPID().isPresent());
		MatcherAssert.assertThat(0L, Matchers.lessThan(p.getPID().get()));
		assertEquals(p.getProcess().pid(), (long) p.getPID().get());
		/**
		 * Flacky on Linux
		 * assertTrue(result.getUserExec().get().endsWith(System.getProperty("user.name")));
		 */

		/**
		 * Should do nothing
		 */
		p.kill();
		assertFalse(p.isKilled());
		p.waitForEnd();
		p.checkExecution();
	}
}
