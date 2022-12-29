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
 * Copyright (C) hdsdi3g for hd3g.tv 2022
 *
 */
package tv.hd3g.processlauncher;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class DirectStandardOutputStdErrRetentionTest {

	DirectStandardOutputStdErrRetention d;
	Thread t;

	@Mock
	CapturedStdOutErrText stdErrObserver;
	@Mock
	InputStreamConsumer stdOutConsumer;
	@Mock
	InputStream processInputStream;
	@Mock
	ProcesslauncherLifecycle source;
	@Mock
	Processlauncher launcher;

	@BeforeEach
	void init() throws Exception {
		openMocks(this).close();
		d = new DirectStandardOutputStdErrRetention(stdErrObserver, stdOutConsumer);

		when(source.getLauncher()).thenReturn(launcher);
		when(launcher.getExecutableName()).thenReturn("Test for " + getClass().getSimpleName());
	}

	@AfterEach
	void end() {
		verify(source, times(1)).getLauncher();
		verify(launcher, times(1)).getExecutableName();

		verifyNoMoreInteractions(stdErrObserver, stdOutConsumer, processInputStream, source, launcher);
	}

	@Test
	void testStdOutStreamConsumer() {

		t = d.stdOutStreamConsumer(processInputStream, source);
		assertNotNull(t);

		while (t.isAlive()) {
			Thread.onSpinWait();
		}

		verify(stdOutConsumer, times(1)).onProcessStart(processInputStream, source);
		verify(stdOutConsumer, times(1)).onClose(source);
	}

	@Test
	void testStdErrStreamConsumer() throws IOException {

		t = d.stdErrStreamConsumer(processInputStream, source);
		assertNotNull(t);
		assertTrue(t instanceof StreamParser);
		verify(stdErrObserver, times(1)).setWatchThreadStderr((StreamParser) t);

		while (t.isAlive()) {
			Thread.onSpinWait();
		}

		verify(processInputStream, times(1)).read(any(), anyInt(), anyInt());
		verify(processInputStream, times(1)).close();

	}

}
