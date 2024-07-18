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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static tv.hd3g.processlauncher.CapturedStreams.BOTH_STDOUT_STDERR;
import static tv.hd3g.processlauncher.EndStatus.KILLED;

import java.util.function.Function;
import java.util.function.Predicate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.event.Level;
import org.slf4j.spi.LoggingEventBuilder;

import net.datafaker.Faker;
import tv.hd3g.processlauncher.CaptureStandardOutputText;
import tv.hd3g.processlauncher.CapturedStdOutErrText;
import tv.hd3g.processlauncher.InvalidExecution;
import tv.hd3g.processlauncher.LineEntry;
import tv.hd3g.processlauncher.ProcesslauncherBuilder;
import tv.hd3g.processlauncher.ProcesslauncherLifecycle;

class KeepStdoutAndErrToLogWatcherTest {

	static Faker faker = net.datafaker.Faker.instance();

	@Mock
	Logger log;
	@Mock
	Function<LineEntry, Level> levelMapper;
	@Mock
	Predicate<String> filterOutErrorLines;
	@Mock
	ProcesslauncherBuilder builder;
	@Mock
	ProcesslauncherLifecycle lifeCycle;
	@Mock
	CaptureStandardOutputText captureStandardOutputText;
	@Mock
	LineEntry lineEntry;
	@Mock
	LoggingEventBuilder loggingEventBuilder;

	@Captor
	ArgumentCaptor<CapturedStdOutErrText> observer;

	KeepStdoutAndErrToLogWatcher w;
	Level level;
	InvalidExecution invalidExecution;

	@BeforeEach
	void init() throws Exception {
		openMocks(this).close();
		w = new KeepStdoutAndErrToLogWatcher(log, levelMapper);
		w.setFilterOutErrorLines(filterOutErrorLines);
		level = faker.options().option(Level.class);

		when(builder.getSetCaptureStandardOutputAsOutputText()).thenReturn(captureStandardOutputText);
		when(lineEntry.source()).thenReturn(lifeCycle);

		when(lifeCycle.getFullCommandLine()).thenReturn("");
		when(lifeCycle.getEndStatus()).thenReturn(KILLED);
		when(lifeCycle.getExitCode()).thenReturn(0);
		invalidExecution = new InvalidExecution(lifeCycle);
	}

	@AfterEach
	void end() {
		verify(lifeCycle, atLeast(1)).getFullCommandLine();
		verify(lifeCycle, atLeast(1)).getEndStatus();
		verify(lifeCycle, atLeast(1)).getExitCode();

		verifyNoMoreInteractions(
				log,
				levelMapper,
				filterOutErrorLines,
				builder,
				lifeCycle,
				captureStandardOutputText,
				lineEntry,
				loggingEventBuilder);
	}

	@Test
	void testSetupWatcherRun_withLog() {
		when(levelMapper.apply(lineEntry)).thenReturn(level);
		when(lineEntry.canUseThis(any())).thenReturn(true);
		when(log.isEnabledForLevel(level)).thenReturn(true);
		when(log.atLevel(level)).thenReturn(loggingEventBuilder);

		w.setupWatcherRun(builder);

		verify(captureStandardOutputText, times(2)).addObserver(observer.capture());
		observer.getAllValues().stream().findFirst().get().onText(lineEntry);

		verify(lineEntry, atLeast(1)).source();
		verify(levelMapper, times(1)).apply(lineEntry);
		verify(log, times(1)).isEnabledForLevel(level);
		verify(log, times(1)).atLevel(level);
		verify(loggingEventBuilder, times(1)).log(anyString());
		verify(builder, times(1)).getSetCaptureStandardOutputAsOutputText();
		verify(captureStandardOutputText, times(1)).addObserver(w.getTextRetention());
	}

	@Test
	void testSetupWatcherRun_withoutLog() {
		w = new KeepStdoutAndErrToLogWatcher();
		w.setFilterOutErrorLines(filterOutErrorLines);

		when(builder.getSetCaptureStandardOutputAsOutputText(BOTH_STDOUT_STDERR)).thenReturn(captureStandardOutputText);
		w.setupWatcherRun(builder);

		verify(builder, times(1)).getSetCaptureStandardOutputAsOutputText(BOTH_STDOUT_STDERR);
		verify(captureStandardOutputText, times(1)).addObserver(w.getTextRetention());
	}

	@Test
	void testAfterStartProcess() {
		w.afterStartProcess(lifeCycle);
		verify(lifeCycle, times(1)).checkExecution();
	}

	@Test
	void testAfterStartProcess_withError() {
		when(lifeCycle.checkExecution()).thenThrow(invalidExecution);
		assertThrows(InvalidExecution.class, () -> w.afterStartProcess(lifeCycle));
		verify(lifeCycle, times(1)).checkExecution();
	}
}
