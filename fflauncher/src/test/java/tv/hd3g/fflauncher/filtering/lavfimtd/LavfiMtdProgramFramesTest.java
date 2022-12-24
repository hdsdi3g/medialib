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
package tv.hd3g.fflauncher.filtering.lavfimtd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static tv.hd3g.fflauncher.filtering.lavfimtd.Utility.getFramesFromString;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import net.datafaker.Faker;

class LavfiMtdProgramFramesTest {
	static Faker faker = net.datafaker.Faker.instance();

	final static String RAW_LINES_OK = """
			frame:116 pts:4841 pts_time:4.841
			lavfi.black_end=4.841
			frame:1022 pts:981168 pts_time:20.441
			lavfi.aphasemeter.phase=1.000000
			frame:1299 pts:1247088 pts_time:25.981
			lavfi.aphasemeter.phase=0.992454
			frame:3306 pts:3173808 pts_time:66.121
			lavfi.silence_start.1=65.1366
			""";

	LavfiMtdProgramFrames<Map<String, String>> f;

	@Test
	void testGetFrames() {
		f = new LavfiMtdProgramFrames<>(
				getFramesFromString(RAW_LINES_OK),
				"aphasemeter",
				Optional::ofNullable);
		assertEquals(Map.of(
				new LavfiMtdPosition(1022, 981168, 20.441f), Map.of("phase", "1.000000"),
				new LavfiMtdPosition(1299, 1247088, 25.981f), Map.of("phase", "0.992454")), f.getFrames());
	}

	@Nested
	class Transformer {

		LavfiMtdProgramFrames<Object> f;

		String keyName;
		List<LavfiRawMtdFrame> frameList;

		@Mock
		Object object;
		@Mock
		LavfiRawMtdFrame frame;
		@Mock
		Function<Map<String, String>, Optional<Object>> transformer;
		@Captor
		ArgumentCaptor<Map<String, String>> transformerCaptor;
		@Mock
		LavfiMtdPosition position;
		Map<String, Map<String, String>> values;

		@BeforeEach
		void init() throws Exception {
			openMocks(this).close();
			keyName = faker.numerify("keyName###");
			frameList = List.of(frame);
			values = Map.of(
					keyName, Map.of("sub0", faker.numerify("sub0###")),
					faker.numerify("nopeKey###"), Map.of("sub0", faker.numerify("sub0###")));

			when(frame.getLavfiMtdPosition()).thenReturn(position);
			when(frame.getValuesByFilterKeysByFilterName()).thenReturn(values);
			when(transformer.apply(any())).thenReturn(Optional.ofNullable(object));
		}

		@AfterEach
		void end() {
			verifyNoMoreInteractions(frame, transformer, object, position);
		}

		@Test
		void testGetFrames() {
			f = new LavfiMtdProgramFrames<>(
					frameList,
					keyName,
					transformer);

			verify(position, atLeastOnce()).compareTo(any());
			verify(frame, times(1)).getLavfiMtdPosition();
			verify(frame, times(1)).getValuesByFilterKeysByFilterName();
			verify(transformer, times(1)).apply(transformerCaptor.capture());

			assertEquals(values.get(keyName), transformerCaptor.getValue());

			verifyNoMoreInteractions(frame, transformer, object, position);

			assertEquals(Map.of(position, object), f.getFrames());
			verify(position, atLeastOnce()).compareTo(any());
		}

	}

}
