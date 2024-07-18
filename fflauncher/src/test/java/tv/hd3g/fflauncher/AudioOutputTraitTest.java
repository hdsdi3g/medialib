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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import tv.hd3g.commons.testtools.Fake;
import tv.hd3g.commons.testtools.MockToolsExtendsJunit;
import tv.hd3g.processlauncher.cmdline.Parameters;

@ExtendWith(MockToolsExtendsJunit.class)
class AudioOutputTraitTest {

	@Mock
	Parameters parameters;
	@Mock
	List<ConversionToolParameterReference> refs;

	class AOT implements AudioOutputTrait {

		@Override
		public Parameters getInternalParameters() {
			return parameters;
		}

		@Override
		public List<ConversionToolParameterReference> getInputSources() {
			return refs;
		}

	}

	@Fake
	String codecName;
	@Fake(min = 1, max = 10000)
	int outputAudioStreamIndex;
	@Fake(min = 1, max = 10000)
	int channel;
	@Fake(min = 1, max = 10000)
	int sr;

	AOT aot;

	@BeforeEach
	void init() {
		aot = new AOT();
	}

	@Test
	void testAddAudioCodecName() {
		aot.addAudioCodecName(codecName, outputAudioStreamIndex);
		verify(parameters, times(1)).addParameters("-c:a:" + outputAudioStreamIndex, codecName);
		aot.addAudioCodecName(codecName, -1);
		verify(parameters, times(1)).addParameters("-c:a", codecName);
	}

	@Test
	void testAddAudioChannelCount() {
		aot.addAudioChannelCount(channel, outputAudioStreamIndex);

		verify(parameters, times(1)).addParameters("-ac:" + outputAudioStreamIndex, String.valueOf(channel));
		aot.addAudioChannelCount(channel, -1);
		verify(parameters, times(1)).addParameters("-ac", String.valueOf(channel));

	}

	@Test
	void testAddAudioSamplingRate() {
		aot.addAudioSamplingRate(sr, outputAudioStreamIndex);
		verify(parameters, times(1)).addParameters("-ar:" + outputAudioStreamIndex, String.valueOf(sr));
		aot.addAudioSamplingRate(sr, -1);
		verify(parameters, times(1)).addParameters("-ar", String.valueOf(sr));
	}

}
