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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.io.File;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import tv.hd3g.commons.testtools.Fake;
import tv.hd3g.commons.testtools.MockToolsExtendsJunit;
import tv.hd3g.fflauncher.ConversionTool.ConversionHooks;
import tv.hd3g.fflauncher.FFprobe;
import tv.hd3g.fflauncher.SimpleSourceTraits;
import tv.hd3g.processlauncher.ProcesslauncherLifecycle;
import tv.hd3g.processlauncher.processingtool.ExecutorWatcher;
import tv.hd3g.processlauncher.processingtool.ProcessingToolResult;

@ExtendWith(MockToolsExtendsJunit.class)
class FFprobeToolBuilderTest {

	class FTB extends FFprobeToolBuilder<Object, ExecutorWatcher> {

		protected FTB(final FFprobe ffprobe, final ExecutorWatcher watcher) {
			super(ffprobe, watcher);
			sourceOrigin = null;
		}

		@Override
		public ProcessingToolResult<FFSourceDefinition, FFprobe, Object, ExecutorWatcher> process(final FFSourceDefinition so) {
			sourceOrigin = so;
			return result;
		}

		@Override
		protected Object compute(final FFSourceDefinition sourceOrigin, final ProcesslauncherLifecycle lifeCycle) {
			throw new UnsupportedOperationException();
		}

	}

	@Mock
	FFprobe ffprobe;
	@Mock
	ExecutorWatcher watcher;
	@Mock
	ProcessingToolResult<FFSourceDefinition, FFprobe, Object, ExecutorWatcher> result;
	@Mock
	SimpleSourceTraits simpleSourceTraits;
	@Mock
	ConversionHooks conversionHooks;

	@Fake
	String executableName;
	@Fake
	String source;
	@Fake
	File sourceFile;

	FTB ftb;
	FFSourceDefinition sourceOrigin;

	@BeforeEach
	void init() {
		when(ffprobe.getExecutableName()).thenReturn(executableName);
		when(ffprobe.makeConversionHooks()).thenReturn(conversionHooks);
		ftb = new FTB(ffprobe, watcher);
	}

	@AfterEach
	void end() {
		reset(conversionHooks);
		verify(ffprobe, atLeastOnce()).getExecutableName();
		verify(ffprobe, times(1)).makeConversionHooks();
	}

	@Test
	void testProcessString() {
		final var r = ftb.process(source);
		assertEquals(result, r);
		assertNotNull(sourceOrigin);
		sourceOrigin.applySourceToFF(simpleSourceTraits);

		verify(simpleSourceTraits, times(1)).addSimpleInputSource(source);
	}

	@Test
	void testProcess() {
		final var r = ftb.process(sourceFile);
		assertEquals(result, r);
		assertNotNull(sourceOrigin);
		sourceOrigin.applySourceToFF(simpleSourceTraits);

		verify(simpleSourceTraits, times(1)).addSimpleInputSource(sourceFile);
	}

	@Test
	void testGetFfprobe() {
		assertEquals(ffprobe, ftb.getFfprobe());
	}

}
