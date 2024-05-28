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
/**
 *
 */

package tv.hd3g.fflauncher.progress;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.lang.Thread.State;
import java.time.Duration;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import net.datafaker.Faker;
import tv.hd3g.fflauncher.progress.FFprobeXMLProgressWatcher.FFprobeXMLProgressEvent;
import tv.hd3g.fflauncher.recipes.ContainerAnalyserSession;

class FFprobeXMLProgressWatcherTest {
	static Faker faker = net.datafaker.Faker.instance();

	String xml = """
			<?xml version="1.0" encoding="UTF-8"?>
			<ffprobe>
			    <packets_and_frames>
			        <packet pts_time="0.000000" />
			        <frame pts_time="0.000000" />
			        <packet pts_time="1.000000" />
			        <frame pts_time="1.000000" />
			        <packet pts_time="60.000000" />
			        <frame pts_time="60.000000" />
			    </packets_and_frames>
			</ffprobe>
			""";

	@Mock
	ThreadFactory threadFactory;
	@Mock
	Consumer<ContainerAnalyserSession> onStartCallback;
	@Mock
	Consumer<FFprobeXMLProgressEvent> progressCallback;
	@Mock
	Consumer<ContainerAnalyserSession> onEndCallback;
	@Mock
	ContainerAnalyserSession session;
	@Mock
	Thread thread;
	@Captor
	ArgumentCaptor<Runnable> runnableCaptor;
	@Captor
	ArgumentCaptor<FFprobeXMLProgressEvent> eventCaptor;

	FFprobeXMLProgressWatcher w;
	Duration programDuration;

	@BeforeEach
	void init() throws Exception {
		MockitoAnnotations.openMocks(this).close();
		programDuration = Duration.ofMinutes(1);
		w = new FFprobeXMLProgressWatcher(
				programDuration,
				threadFactory,
				onStartCallback,
				progressCallback,
				onEndCallback);
	}

	@AfterEach
	void ends() {
		verifyNoMoreInteractions(
				threadFactory,
				onStartCallback,
				progressCallback,
				onEndCallback,
				session,
				thread);
	}

	@Test
	void testCreateProgress() {
		when(threadFactory.newThread(any())).thenReturn(thread);
		when(thread.getState()).thenReturn(State.NEW, State.RUNNABLE);

		final var progress = w.createProgress(session);
		assertThat(progress).isNotNull();

		progress.accept("");

		verify(thread, times(1)).start();
		verify(thread, times(1)).getState();
		verify(threadFactory, times(1)).newThread(any());
	}

	@Test
	void testRunProgress() {

		when(threadFactory.newThread(any())).thenReturn(thread);
		when(thread.getState()).thenReturn(State.NEW, State.RUNNABLE);

		final var progress = w.createProgress(session);
		assertThat(progress).isNotNull();
		xml.lines().forEach(progress::accept);

		verify(thread, times(1)).start();
		verify(thread, atLeastOnce()).getState();
		verify(threadFactory, times(1)).newThread(runnableCaptor.capture());
		final var r = runnableCaptor.getValue();
		assertNotNull(r);

		r.run();

		verify(onStartCallback, times(1)).accept(session);
		verify(onEndCallback, times(1)).accept(session);
		verify(progressCallback, atLeastOnce()).accept(eventCaptor.capture());
		final var events = eventCaptor.getAllValues();
		assertEquals(new FFprobeXMLProgressEvent(0d, 1f, session), events.get(0));
	}

	@Test
	void testRunProgress_noProgramDuration() {
		programDuration = Duration.ZERO;
		w = new FFprobeXMLProgressWatcher(
				programDuration,
				threadFactory,
				onStartCallback,
				progressCallback,
				onEndCallback);

		final var progress = w.createProgress(session);
		assertThat(progress).isNotNull();
		xml.lines().forEach(progress::accept);
	}

}
