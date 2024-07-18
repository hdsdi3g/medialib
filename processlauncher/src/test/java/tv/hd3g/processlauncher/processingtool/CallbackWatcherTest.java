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
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.util.function.Consumer;
import java.util.function.Predicate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import net.datafaker.Faker;
import tv.hd3g.processlauncher.CaptureStandardOutputText;
import tv.hd3g.processlauncher.CapturedStdOutErrText;
import tv.hd3g.processlauncher.InvalidExecution;
import tv.hd3g.processlauncher.LineEntry;
import tv.hd3g.processlauncher.ProcesslauncherBuilder;
import tv.hd3g.processlauncher.ProcesslauncherLifecycle;

class CallbackWatcherTest {

	static Faker faker = net.datafaker.Faker.instance();

	@Mock
	Predicate<String> filterOutErrorLines;
	@Mock
	Consumer<LineEntry> stdOutErrConsumer;
	@Mock
	ProcesslauncherBuilder builder;
	@Mock
	ProcesslauncherLifecycle lifeCycle;
	@Mock
	CaptureStandardOutputText captureStandardOutputText;
	@Mock
	LineEntry lineEntry;
	@Mock
	InvalidExecution invalidExecution;
	@Captor
	ArgumentCaptor<CapturedStdOutErrText> observer;

	String line;
	CallbackWatcher callbackWatcher;

	@BeforeEach
	void init() throws Exception {
		openMocks(this).close();
		line = faker.numerify("line###");
		callbackWatcher = new CallbackWatcher();
		callbackWatcher.setFilterOutErrorLines(filterOutErrorLines);
		callbackWatcher.setStdOutErrConsumer(stdOutErrConsumer);

		when(builder.getSetCaptureStandardOutputAsOutputText()).thenReturn(captureStandardOutputText);
		when(lineEntry.line()).thenReturn(line);
		when(lineEntry.source()).thenReturn(lifeCycle);
	}

	@AfterEach
	void end() {
		verifyNoMoreInteractions(
				filterOutErrorLines,
				stdOutErrConsumer,
				builder,
				lifeCycle,
				captureStandardOutputText,
				lineEntry,
				invalidExecution);
	}

	@Test
	void testSetupWatcherRun() {
		callbackWatcher.setupWatcherRun(builder);

		verify(builder, times(1)).getSetCaptureStandardOutputAsOutputText();
		verify(captureStandardOutputText, times(1)).addObserver(observer.capture());
		final var o = observer.getValue();

		when(lineEntry.stdErr()).thenReturn(false, true);
		when(lineEntry.isEmpty()).thenReturn(true);
		o.onText(lineEntry);
		verify(stdOutErrConsumer, times(1)).accept(lineEntry);
		o.onText(lineEntry);
		verify(stdOutErrConsumer, times(2)).accept(lineEntry);

		when(lineEntry.stdErr()).thenReturn(true);
		when(lineEntry.isEmpty()).thenReturn(false);
		o.onText(lineEntry);
		verify(stdOutErrConsumer, times(3)).accept(lineEntry);
		verify(lineEntry, atLeast(1)).stdErr();
		verify(lineEntry, atLeast(1)).isEmpty();
		verify(lineEntry, times(1)).line();
		verify(lineEntry, times(3)).source();
	}

	@Test
	void testAfterStartProcess_noError() {
		callbackWatcher.afterStartProcess(lifeCycle);
		verify(lifeCycle, times(1)).checkExecution();
	}

	@Test
	void testAfterStartProcess_withError() {
		when(lifeCycle.checkExecution()).thenThrow(invalidExecution);
		when(lineEntry.stdErr()).thenReturn(true);
		when(lineEntry.isEmpty()).thenReturn(false);
		when(filterOutErrorLines.test(line)).thenReturn(true);
		when(invalidExecution.injectStdErr(line)).thenReturn(invalidExecution);

		callbackWatcher.setupWatcherRun(builder);
		verify(captureStandardOutputText, times(1)).addObserver(observer.capture());
		final var o = observer.getValue();
		o.onText(lineEntry);

		assertThrows(InvalidExecution.class, () -> callbackWatcher.afterStartProcess(lifeCycle));

		verify(builder, times(1)).getSetCaptureStandardOutputAsOutputText();
		verify(lifeCycle, times(1)).checkExecution();
		verify(lineEntry, atLeast(1)).stdErr();
		verify(lineEntry, atLeast(1)).isEmpty();
		verify(lineEntry, times(1)).line();
		verify(lineEntry, times(1)).source();
		verify(stdOutErrConsumer, times(1)).accept(lineEntry);
		verify(filterOutErrorLines, times(1)).test(line);
		verify(invalidExecution, times(1)).injectStdErr(line);
	}

}
