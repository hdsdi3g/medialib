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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class CapturedStdOutErrToPrintStreamTest {

	private static final String execName = "launchedexec";
	private final Processlauncher launcher;

	CapturedStdOutErrToPrintStreamTest() {
		launcher = Mockito.mock(Processlauncher.class);
		Mockito.when(launcher.getExecutableName()).thenReturn(execName);
	}

	private long pid;
	private CapturedStdOutErrToPrintStream capture;
	private PrintStream printStreamStdOut;
	private PrintStream printStreamStdErr;
	private ByteArrayOutputStream outStreamContent;
	private ByteArrayOutputStream errStreamContent;
	private ProcesslauncherLifecycle source;

	@BeforeEach
	void setUp() throws Exception {
		pid = Math.floorMod(Math.abs(new Random().nextLong()), 1000L);
		outStreamContent = new ByteArrayOutputStream();
		errStreamContent = new ByteArrayOutputStream();
		printStreamStdOut = new PrintStream(outStreamContent);
		printStreamStdErr = new PrintStream(errStreamContent);
		capture = new CapturedStdOutErrToPrintStream(printStreamStdOut, printStreamStdErr);
		source = Mockito.mock(ProcesslauncherLifecycle.class);
		Mockito.when(source.getLauncher()).thenReturn(launcher);
		Mockito.when(source.getPID()).thenReturn(Optional.ofNullable(pid));
	}

	@Test
	void testGetFilter() {
		assertTrue(capture.getFilter().isEmpty());
	}

	@Test
	void testSetFilter() {
		final Predicate<LineEntry> filter = l -> true;
		capture.setFilter(filter);
		assertEquals(filter, capture.getFilter().get());
	}

	@Test
	void testOnFilteredText() {
		capture.setFilter(l -> l.isStdErr() == false);
		capture.onText(new LineEntry(System.currentTimeMillis(), "content", true, source));
		assertEquals(0, outStreamContent.size());
		assertEquals(0, errStreamContent.size());
	}

	@Test
	void testOnProcessCloseStreamExecOk() {
		Mockito.when(source.isCorrectlyDone()).thenReturn(true);
		Mockito.when(source.getEndStatus()).thenReturn(EndStatus.CORRECTLY_DONE);
		Mockito.when(source.getExitCode()).thenReturn(0);
		Mockito.when(source.getCPUDuration(null)).thenReturn(1L);
		Mockito.when(source.getUptime(null)).thenReturn(1L);

		assertEquals(0, outStreamContent.size());
		assertEquals(0, errStreamContent.size());
		assertEquals(0, errStreamContent.size());
	}

}
