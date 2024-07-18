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
package tv.hd3g.fflauncher.processingtool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static tv.hd3g.fflauncher.processingtool.FFmpegToolBuilder.statsPeriod;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import tv.hd3g.commons.testtools.Fake;
import tv.hd3g.commons.testtools.MockToolsExtendsJunit;
import tv.hd3g.fflauncher.ConversionTool.ConversionHooks;
import tv.hd3g.fflauncher.FFmpeg;
import tv.hd3g.fflauncher.progress.ProgressCallback;
import tv.hd3g.fflauncher.progress.ProgressListener;
import tv.hd3g.fflauncher.progress.ProgressListenerSession;
import tv.hd3g.processlauncher.ExecutionCallbacker;
import tv.hd3g.processlauncher.ProcesslauncherBuilder;
import tv.hd3g.processlauncher.ProcesslauncherLifecycle;
import tv.hd3g.processlauncher.cmdline.Parameters;
import tv.hd3g.processlauncher.processingtool.ExecutorWatcher;
import tv.hd3g.processlauncher.processingtool.ProcessingToolResult;

@ExtendWith(MockToolsExtendsJunit.class)
class FFmpegToolBuilderTest {

	class FFTB extends FFmpegToolBuilder<Object, Object, ExecutorWatcher> {

		protected FFTB(final FFmpeg ffmpeg, final ExecutorWatcher watcher) {
			super(ffmpeg, watcher);
		}

		@Override
		protected FFmpeg getParametersProvider(final Object so) {
			throw new UnsupportedOperationException();
		}

		@Override
		protected Object compute(final Object sourceOrigin, final ProcesslauncherLifecycle lifeCycle) {
			throw new UnsupportedOperationException();
		}

	}

	@Mock
	ProcessingToolResult<Object, FFmpeg, Object, ExecutorWatcher> result;
	@Mock
	FFmpeg ffmpeg;
	@Mock
	Object sourceOrigin;
	@Mock
	ExecutorWatcher executorWatcher;
	@Mock
	ProgressListener progressListener;
	@Mock
	ProgressCallback progressCallback;
	@Mock
	Parameters parameters;
	@Mock
	ProcesslauncherBuilder pBuilder;
	@Mock
	ProcesslauncherLifecycle processlauncherLifecycle;
	@Mock
	ConversionHooks conversionHooks;
	@Mock
	ProgressListenerSession progressListenerSession;

	@Captor
	ArgumentCaptor<Runnable> toDoIfMissingCaptor;
	@Captor
	ArgumentCaptor<String[]> inParametersCaptor;
	@Captor
	ArgumentCaptor<ExecutionCallbacker> executionCallbackerCaptor;

	@Fake
	String executableName;
	@Fake(min = 1, max = 10000)
	int port;

	FFTB fftb;

	@BeforeEach
	void init() {
		when(ffmpeg.getExecutableName()).thenReturn(executableName);
		when(ffmpeg.makeConversionHooks()).thenReturn(conversionHooks);
		fftb = new FFTB(ffmpeg, executorWatcher);
	}

	@AfterEach
	void end() {
		reset(conversionHooks);
		verify(ffmpeg, atLeastOnce()).getExecutableName();
		verify(ffmpeg, times(1)).makeConversionHooks();
	}

	@Test
	void testResetProgressListener() {// NOSONAR S2699
		fftb.setProgressListener(progressListener, progressCallback);
		fftb.resetProgressListener();
		fftb.dryRunCallbacks(parameters, pBuilder, processlauncherLifecycle);
	}

	@Test
	void testSetProgressListenerNull() {
		assertThrows(NullPointerException.class, () -> fftb.setProgressListener(null, progressCallback));
		assertThrows(NullPointerException.class, () -> fftb.setProgressListener(progressListener, null));
	}

	@Test
	void testSetProgressListener() {
		fftb.setProgressListener(progressListener, progressCallback);

		when(parameters.hasParameters("-progress")).thenReturn(false);
		when(progressListener.createSession(progressCallback, statsPeriod)).thenReturn(
				progressListenerSession);
		when(progressListenerSession.start()).thenReturn(port);

		fftb.dryRunCallbacks(parameters, pBuilder, processlauncherLifecycle);

		verify(parameters, atLeast(1)).hasParameters("-progress");
		verify(parameters, times(1)).ifHasNotParameter(toDoIfMissingCaptor.capture(), inParametersCaptor.capture());

		toDoIfMissingCaptor.getValue().run();

		verify(parameters, times(1))
				.prependParameters("-stats_period", String.valueOf(statsPeriod.toSeconds()));
		verify(parameters, times(1))
				.prependParameters("-progress", "tcp://127.0.0.1:" + port);

		assertThat(inParametersCaptor.getValue())
				.hasSize(1)
				.contains("-stats_period");
		verify(progressListener, times(1)).createSession(progressCallback, statsPeriod);
		verify(progressListenerSession, times(1)).start();

		verify(pBuilder, times(1)).addExecutionCallbacker(executionCallbackerCaptor.capture());
		executionCallbackerCaptor.getValue().onEndExecution(processlauncherLifecycle);
		verify(progressListenerSession, times(1)).manualClose();
	}

	@Test
	void testMakeCallback_progressError() {
		fftb.setProgressListener(progressListener, progressCallback);
		when(parameters.hasParameters("-progress")).thenReturn(true);

		assertThrows(IllegalArgumentException.class,
				() -> fftb.dryRunCallbacks(parameters, pBuilder, processlauncherLifecycle));
		verify(parameters, atLeast(1)).hasParameters("-progress");
	}

}
