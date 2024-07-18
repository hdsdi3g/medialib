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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.atLeastOnce;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import tv.hd3g.commons.testtools.Fake;
import tv.hd3g.commons.testtools.MockToolsExtendsJunit;
import tv.hd3g.fflauncher.about.FFAbout;
import tv.hd3g.fflauncher.filtering.AudioFilterEbur128;
import tv.hd3g.fflauncher.filtering.FilterChains;
import tv.hd3g.fflauncher.filtering.lavfimtd.LavfiMetadataFilterParser;
import tv.hd3g.processlauncher.LineEntry;
import tv.hd3g.processlauncher.ProcesslauncherLifecycle;
import tv.hd3g.processlauncher.processingtool.CallbackWatcher;

@ExtendWith(MockToolsExtendsJunit.class)
class MediaAnalyserProcessTest {

	class MAP extends MediaAnalyserProcess {

		public MAP(final String execName,
				   final FFAbout about,
				   final CallbackWatcher executorWatcher,
				   final LavfiMetadataFilterParser lavfiMetadataFilterParser) {
			super(execName, about, executorWatcher, lavfiMetadataFilterParser);
		}

	}

	@Mock
	CallbackWatcher executorWatcher;
	@Mock
	LavfiMetadataFilterParser lavfiMetadataFilterParser;
	@Mock
	FFAbout about;
	@Mock
	ProcesslauncherLifecycle lifeCycle;
	@Mock
	MediaAnalyserProcessSetup setup;
	@Captor
	ArgumentCaptor<Consumer<LineEntry>> stdOutErrConsumerCaptor;

	@Fake
	String execName;
	@Fake
	String stdOutLine;
	@Fake
	String stdErrLine;
	@Fake
	String fullCommandLine;
	@Fake(min = -8000, max = 100)
	int target;

	MAP map;

	@BeforeEach
	void init() {
		when(lifeCycle.getFullCommandLine()).thenReturn(fullCommandLine);
		when(lavfiMetadataFilterParser.close()).thenReturn(lavfiMetadataFilterParser);

		map = new MAP(execName, about, executorWatcher, lavfiMetadataFilterParser);
		verify(executorWatcher, times(1)).setStdOutErrConsumer(stdOutErrConsumerCaptor.capture());

		new MediaAnalyserProcess(execName, about);
	}

	@Test
	void testCompute() {
		when(setup.oLavfiLinesToMerge()).thenReturn(Optional.empty());

		stdOutErrConsumerCaptor.getValue().accept(new LineEntry(0, stdOutLine, false, lifeCycle));
		stdOutErrConsumerCaptor.getValue().accept(new LineEntry(0, stdErrLine, true, lifeCycle));

		final var result = map.compute(setup, lifeCycle);
		assertEquals(lavfiMetadataFilterParser, result.lavfiMetadatas());
		assertThat(result.filters()).isEmpty();
		assertThat(result.r128Target()).isEmpty();
		assertEquals(fullCommandLine, result.ffmpegCommandLine());

		verify(lifeCycle, atLeastOnce()).getFullCommandLine();
		verify(lavfiMetadataFilterParser, times(1)).close();
		verify(lavfiMetadataFilterParser, times(1)).addLavfiRawLine(stdOutLine);
		verify(setup, times(1)).oLavfiLinesToMerge();
	}

	@Test
	void testCompute_withFilters() {
		when(about.isFilterIsAvaliable(any())).thenReturn(true);
		when(setup.oLavfiLinesToMerge()).thenReturn(Optional.ofNullable(() -> Stream.of(stdOutLine)));

		final var aFilter = new AudioFilterEbur128();
		aFilter.setTarget(target);

		final var fc = new FilterChains();
		fc.addFilterInLastChain(aFilter.toFilter(), true);
		fc.pushFilterChainTo("-af", map.getFfmpeg());

		map.addFilterEbur128(f -> {
			f.setTarget(target);
		});

		final var result = map.compute(setup, lifeCycle);
		assertEquals(lavfiMetadataFilterParser, result.lavfiMetadatas());
		assertThat(result.filters())
				.hasSize(1)
				.allMatch(f -> f.name().equals(aFilter.getFilterName()));

		assertThat(result.r128Target()).contains(target);
		assertEquals(fullCommandLine, result.ffmpegCommandLine());

		verify(lavfiMetadataFilterParser, times(1)).addLavfiRawLine(stdOutLine);
		verify(lavfiMetadataFilterParser, times(1)).close();
		verify(about, times(1)).isFilterIsAvaliable(any());
		verify(lifeCycle, atLeastOnce()).getFullCommandLine();
		verify(setup, times(1)).oLavfiLinesToMerge();
	}

}
