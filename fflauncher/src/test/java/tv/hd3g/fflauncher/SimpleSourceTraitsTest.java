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
package tv.hd3g.fflauncher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.time.Duration;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import tv.hd3g.commons.testtools.Fake;
import tv.hd3g.commons.testtools.MockToolsExtendsJunit;
import tv.hd3g.processlauncher.cmdline.Parameters;

@ExtendWith(MockToolsExtendsJunit.class)
class SimpleSourceTraitsTest {

	class SST implements SimpleSourceTraits {

		@Override
		public ConversionTool addInputSource(final String source,
											 final String varNameInParameters,
											 final Collection<String> parametersBeforeInputSource) {
			throw new IllegalCallerException();
		}

		@Override
		public ConversionTool addInputSource(final File source,
											 final String varNameInParameters,
											 final Collection<String> parametersBeforeInputSource) {
			throw new IllegalCallerException();
		}

		@Override
		public Parameters getInternalParameters() {
			throw new IllegalCallerException();
		}

		@Override
		public List<ConversionToolParameterReference> getInputSources() {
			throw new IllegalCallerException();
		}

		String sourceName;
		List<String> sourceOptions;
		File file;

		@Override
		public void addSimpleInputSource(final String sourceName, final List<String> sourceOptions) {
			this.sourceName = sourceName;
			this.sourceOptions = sourceOptions;
		}

		@Override
		public void addSimpleInputSource(final File file, final List<String> sourceOptions) {
			this.file = file;
			this.sourceOptions = sourceOptions;
		}
	}

	@Fake
	String sourceName;
	@Fake
	String sourceOption0;
	@Fake
	String sourceOption1;
	@Fake
	File file;

	@Fake
	long durationLong;
	Duration duration;
	String durationStr;
	SST sst;

	@BeforeEach
	void init() {
		sst = new SST();
		duration = Duration.ofMillis(durationLong);
		durationStr = TemporalProcessTraits.positionToFFmpegPosition(duration);
	}

	@Test
	void testAddSimpleInputSourceStringStringArray() {
		sst.addSimpleInputSource(sourceName, sourceOption0, sourceOption1);
		assertEquals(sourceName, sst.sourceName);
		assertThat(sst.sourceOptions).containsExactly(sourceOption0, sourceOption1);

		sst = new SST();
		sst.addSimpleInputSource(sourceName);
		assertEquals(sourceName, sst.sourceName);
		assertThat(sst.sourceOptions).isEmpty();

		sst = new SST();
		sst.addSimpleInputSource(sourceName, (String[]) null);
		assertEquals(sourceName, sst.sourceName);
		assertThat(sst.sourceOptions).isEmpty();
	}

	@Test
	void testAddSimpleInputSourceStringDurationStringArray() {
		sst.addSimpleInputSource(sourceName, duration, sourceOption0, sourceOption1);
		assertEquals(sourceName, sst.sourceName);
		assertThat(sst.sourceOptions).containsExactly("-ss", durationStr, sourceOption0, sourceOption1);

		sst = new SST();
		sst.addSimpleInputSource(sourceName, duration);
		assertEquals(sourceName, sst.sourceName);
		assertThat(sst.sourceOptions).containsExactly("-ss", durationStr);

		sst = new SST();
		sst.addSimpleInputSource(sourceName, duration, (String[]) null);
		assertEquals(sourceName, sst.sourceName);
		assertThat(sst.sourceOptions).containsExactly("-ss", durationStr);

	}

	@Test
	void testAddSimpleInputSourceFileStringArray() {
		sst.addSimpleInputSource(file, sourceOption0, sourceOption1);
		assertEquals(file, sst.file);
		assertThat(sst.sourceOptions).containsExactly(sourceOption0, sourceOption1);

		sst = new SST();
		sst.addSimpleInputSource(file);
		assertEquals(file, sst.file);
		assertThat(sst.sourceOptions).isEmpty();

		sst = new SST();
		sst.addSimpleInputSource(file, (String[]) null);
		assertEquals(file, sst.file);
		assertThat(sst.sourceOptions).isEmpty();
	}

	@Test
	void testAddSimpleInputSourceFileDurationStringArray() {
		sst.addSimpleInputSource(file, duration, sourceOption0, sourceOption1);
		assertEquals(file, sst.file);
		assertThat(sst.sourceOptions).containsExactly("-ss", durationStr, sourceOption0, sourceOption1);

		sst = new SST();
		sst.addSimpleInputSource(file, duration);
		assertEquals(file, sst.file);
		assertThat(sst.sourceOptions).containsExactly("-ss", durationStr);

		sst = new SST();
		sst.addSimpleInputSource(file, duration, (String[]) null);
		assertEquals(file, sst.file);
		assertThat(sst.sourceOptions).containsExactly("-ss", durationStr);
	}

	@Test
	void testAddSimpleInputSourceStringListOfString() {
		sst.addSimpleInputSource(sourceName, List.of(sourceOption0, sourceOption1));
		assertEquals(sourceName, sst.sourceName);
		assertThat(sst.sourceOptions).containsExactly(sourceOption0, sourceOption1);

		sst = new SST();
		sst.addSimpleInputSource(sourceName, List.of());
		assertEquals(sourceName, sst.sourceName);
		assertThat(sst.sourceOptions).isEmpty();
	}

	@Test
	void testAddSimpleInputSourceFileListOfString() {
		sst.addSimpleInputSource(file, List.of(sourceOption0, sourceOption1));
		assertEquals(file, sst.file);
		assertThat(sst.sourceOptions).containsExactly(sourceOption0, sourceOption1);

		sst = new SST();
		sst.addSimpleInputSource(file, List.of());
		assertEquals(file, sst.file);
		assertThat(sst.sourceOptions).isEmpty();
	}

}
