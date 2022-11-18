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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

class LineEntryTest {

	@Mock
	ProcesslauncherLifecycle source;
	@Mock
	Processlauncher launcher;

	long date;
	String line;
	boolean stdErr;
	LineEntry lineEntry;
	String execName;

	@BeforeEach
	void init() throws Exception {
		MockitoAnnotations.openMocks(this).close();

		line = "This is a test";
		stdErr = true;
		date = System.currentTimeMillis();
		execName = "" + System.nanoTime();

		lineEntry = new LineEntry(date, line, stdErr, source);

		when(source.getStartDate()).thenReturn(date - 10000L);
		when(source.getLauncher()).thenReturn(launcher);
		when(launcher.getExecutableName()).thenReturn(execName);
	}

	@AfterEach
	void end() {
		verifyNoMoreInteractions(source, launcher);
	}

	@Test
	void testGetTimeAgo() {
		assertEquals(10000L, lineEntry.getTimeAgo());
		Mockito.verify(source, Mockito.times(1)).getStartDate();
	}

	@Test
	void testGetDate() {
		assertEquals(date, lineEntry.getDate());
	}

	@Test
	void testGetLine() {
		assertEquals(line, lineEntry.getLine());
	}

	@Test
	void testGetSource() {
		assertEquals(source, lineEntry.getSource());
	}

	@Test
	void testIsStdErr() {
		assertEquals(stdErr, lineEntry.isStdErr());
	}

	@Test
	void testCanUseThis() {
		assertFalse(lineEntry.canUseThis(CapturedStreams.ONLY_STDOUT));
		assertTrue(lineEntry.canUseThis(CapturedStreams.ONLY_STDERR));
		assertTrue(lineEntry.canUseThis(CapturedStreams.BOTH_STDOUT_STDERR));

		lineEntry = new LineEntry(date, line, false, source);

		assertTrue(lineEntry.canUseThis(CapturedStreams.ONLY_STDOUT));
		assertFalse(lineEntry.canUseThis(CapturedStreams.ONLY_STDERR));
		assertTrue(lineEntry.canUseThis(CapturedStreams.BOTH_STDOUT_STDERR));
	}

	@Test
	void testToString() {
		var toString = lineEntry.toString();
		assertNotNull(toString);
		assertTrue(toString.startsWith(execName));
		assertTrue(toString.endsWith(line));

		verify(source, Mockito.times(1)).getLauncher();
		verify(launcher, Mockito.times(1)).getExecutableName();

		lineEntry = new LineEntry(date, line, false, source);
		toString = lineEntry.toString();
		assertNotNull(toString);
		assertTrue(toString.startsWith(execName));
		assertTrue(toString.endsWith(line));

		verify(source, Mockito.times(2)).getLauncher();
		verify(launcher, Mockito.times(2)).getExecutableName();
	}

	@Test
	void testMakeStdOut() {
		lineEntry = LineEntry.makeStdOut(line, source);
		assertTrue(lineEntry.getDate() > 0);
		assertTrue(lineEntry.getDate() < System.currentTimeMillis() + 100);
		assertEquals(line, lineEntry.getLine());
		assertEquals(source, lineEntry.getSource());
		assertFalse(lineEntry.isStdErr());
	}

	@Test
	void testMakeStdErr() {
		lineEntry = LineEntry.makeStdErr(line, source);
		assertTrue(lineEntry.getDate() > 0);
		assertTrue(lineEntry.getDate() < System.currentTimeMillis() + 100);
		assertEquals(line, lineEntry.getLine());
		assertEquals(source, lineEntry.getSource());
		assertTrue(lineEntry.isStdErr());
	}

}
