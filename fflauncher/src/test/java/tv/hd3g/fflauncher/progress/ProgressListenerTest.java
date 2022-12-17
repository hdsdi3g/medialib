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
 * Copyright (C) hdsdi3g for hd3g.tv 2022
 *
 */
package tv.hd3g.fflauncher.progress;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.time.Duration;
import java.util.concurrent.ThreadFactory;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import net.datafaker.Faker;

class ProgressListenerTest {
	static Faker faker = net.datafaker.Faker.instance();

	ProgressListener pl;

	@Mock
	ThreadFactory threadFactory;
	@Mock
	ProgressCallback progressCallback;
	@Mock
	Duration statsPeriod;
	@Captor
	ArgumentCaptor<Runnable> runnableCaptor;

	@BeforeEach
	void init() throws Exception {
		openMocks(this).close();
		pl = new ProgressListener(threadFactory);
	}

	@AfterEach
	void end() {
		verifyNoMoreInteractions(threadFactory);
	}

	@Test
	void testCreateSession() {
		final var session = pl.createSession(progressCallback, statsPeriod);
		assertNotNull(session);
		verify(threadFactory, times(1)).newThread(runnableCaptor.capture());
		assertNotNull(runnableCaptor.getValue());
	}

	@Test
	void testCreateSession_defaultConstr() {
		final var session = new ProgressListener().createSession(progressCallback, statsPeriod);
		assertNotNull(session);
	}

}
