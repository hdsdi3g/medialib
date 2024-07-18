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
package tv.hd3g.fflauncher.recipes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.atLeastOnce;

import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import tv.hd3g.commons.testtools.Fake;
import tv.hd3g.commons.testtools.MockToolsExtendsJunit;
import tv.hd3g.fflauncher.about.FFAbout;
import tv.hd3g.processlauncher.LineEntry;
import tv.hd3g.processlauncher.ProcesslauncherLifecycle;

@ExtendWith(MockToolsExtendsJunit.class)
class MediaAnalyserExtractTest {

	class MAE extends MediaAnalyserExtract {

		public MAE(final String execName, final FFAbout about) {
			super(execName, about);
			stdOutErrConsumer = executorWatcher.getStdOutErrConsumer();
		}

	}

	@Mock
	FFAbout about;
	@Mock
	ProcesslauncherLifecycle lifeCycle;

	@Fake
	String execName;
	@Fake
	String fullCommandLine;
	@Fake
	String stdOutLine;
	@Fake
	String stdErrLine;

	MAE mae;
	Consumer<LineEntry> stdOutErrConsumer;
	LineEntry entry;

	@BeforeEach
	void init() {
		mae = new MAE(execName, about);
	}

	@Test
	void testCompute() {
		when(lifeCycle.getFullCommandLine()).thenReturn(fullCommandLine);

		stdOutErrConsumer.accept(new LineEntry(0, stdOutLine, false, lifeCycle));
		stdOutErrConsumer.accept(new LineEntry(0, stdErrLine, true, lifeCycle));

		final var result = mae.compute(null, lifeCycle);
		assertNotNull(result);
		assertEquals(fullCommandLine, result.ffmpegCommandLine());
		assertThat(result.filters()).isEmpty();
		assertThat(result.sysOut()).containsExactly(stdOutLine);
		assertThat(result.sysErr()).containsExactly(stdErrLine);

		verify(lifeCycle, atLeastOnce()).getFullCommandLine();
	}

}
