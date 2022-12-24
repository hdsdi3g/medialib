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
package tv.hd3g.fflauncher.resultparser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import tv.hd3g.fflauncher.filtering.lavfimtd.LavfiMtdProgramFrames;
import tv.hd3g.fflauncher.filtering.lavfimtd.LavfiMtdProgramFramesExtractor;
import tv.hd3g.fflauncher.filtering.lavfimtd.LavfiRawMtdFrame;

class MetadataFilterFrameParserTest {

	private List<LavfiRawMtdFrame> process(final String lines) {
		final var m = new MetadataFilterFrameParser();
		lines.lines().forEach(m::onLine);
		final var result = m.close();
		assertNotNull(result);
		return result;
	}

	@Test
	void testOneEmptyFrame() {
		final var r = process("frame:1022 pts:981168  pts_time:20.441");
		assertTrue(r.isEmpty());
	}

	@Test
	void testOneFrame() {
		final var r = process("""
				frame:1022 pts:981168  pts_time:20.441
				lavfi.aphasemeter.phase=1.000000
				lavfi.aphasemeter.mono_start=18.461
				lavfi.astats.1.Entropy=0.788192
				lavfi.astats.2.DC_offset=0.000002
				lavfi.silence_end.1=66.6474
				lavfi.idet.repeated.current_frame=neither
				lavfi.cropdetect.h=480
				""");
		assertEquals(1, r.size());
		final var f = r.get(0);
		assertEquals(1022, f.getFrame());
		assertEquals(981168, f.getPts());
		assertEquals(20.44099998474121, f.getPtsTime());

		assertEquals(
				Map.of(
						"aphasemeter", Map.of("mono_start", "18.461", "phase", "1.000000"),
						"cropdetect", Map.of("h", "480"),
						"silence_end", Map.of("1", "66.6474"),
						"idet", Map.of("repeated.current_frame", "neither"),
						"astats", Map.of("2.DC_offset", "0.000002",
								"1.Entropy", "0.788192")),
				f.getValuesByFilterKeysByFilterName());

	}

	@Test
	void testMultipleFrames() {
		final var r = process("""
				frame:1022 pts:981168  pts_time:20.441
				lavfi.aphasemeter.phase=1.000000
				lavfi.aphasemeter.mono_start=11.461
				frame:1801 pts:60041   pts_time:60.041
				lavfi.aphasemeter.phase=1.000000
				lavfi.aphasemeter.mono_start=18.461
				lavfi.astats.1.Entropy=0.788192
				lavfi.astats.2.DC_offset=0.000002
				frame:7616 pts:317340  pts_time:317.34
				lavfi.silence_end.1=66.6474
				lavfi.idet.repeated.current_frame=neither
				frame:66   pts:2757    pts_time:2.757
				frame:114  pts:4757    pts_time:4.757
				lavfi.cropdetect.h=480
				frame:119  pts:4966    pts_time:4.966
				""");
		assertEquals(4, r.size());
		var f = r.get(0);
		assertEquals(1022, f.getFrame());
		assertEquals(981168, f.getPts());
		assertEquals(20.44099998474121, f.getPtsTime());
		assertEquals("{aphasemeter={phase=1.000000, mono_start=11.461}}",
				f.getValuesByFilterKeysByFilterName().toString());

		f = r.get(1);
		assertEquals(1801, f.getFrame());
		assertEquals(60041, f.getPts());
		assertEquals(60.04100036621094, f.getPtsTime());
		assertEquals(
				"{aphasemeter={phase=1.000000, mono_start=18.461}, astats={2.DC_offset=0.000002, 1.Entropy=0.788192}}",
				f.getValuesByFilterKeysByFilterName().toString());

		f = r.get(2);
		assertEquals(7616, f.getFrame());
		assertEquals(317340, f.getPts());
		assertEquals(317.3399963378906, f.getPtsTime());
		assertEquals("{silence_end={1=66.6474}, idet={repeated.current_frame=neither}}",
				f.getValuesByFilterKeysByFilterName().toString());

		f = r.get(3);
		assertEquals(114, f.getFrame());
		assertEquals(4757, f.getPts());
		assertEquals(4.756999969482422, f.getPtsTime());
		assertEquals("{cropdetect={h=480}}",
				f.getValuesByFilterKeysByFilterName().toString());
	}

	@Test
	void testNoFrame() {
		assertThrows(IllegalArgumentException.class, () -> process("""
				lavfi.aphasemeter.phase=1.000000
				lavfi.aphasemeter.mono_start=11.461
				"""));
	}

	@Test
	void testMissingFrames() {
		assertThrows(IllegalArgumentException.class, () -> process("""
				lavfi.aphasemeter.phase=1.000000
				frame:1801 pts:60041   pts_time:60.041
				lavfi.aphasemeter.mono_start=11.461
				"""));
	}

	@Test
	void testNoLavfi() {
		assertThrows(IllegalArgumentException.class, () -> process("""
				frame:1022 pts:981168  pts_time:20.441
				aaaa.aphasemeter.phase=1.000000
				bbbb.aphasemeter.mono_start=11.461
				"""));
	}

	@Mock
	LavfiMtdProgramFramesExtractor<Object> metadataExtractor;
	@Mock
	LavfiMtdProgramFrames<Object> frames;

	@Test
	void testGetMetadatasForFilter() throws Exception {
		openMocks(this).close();
		final var m = new MetadataFilterFrameParser();
		"""
				frame:1022 pts:981168  pts_time:20.441
				lavfi.aphasemeter.phase=1.000000
				frame:1801 pts:60041   pts_time:60.041
				lavfi.aphasemeter.phase=0.998000
				""".lines().forEach(m::onLine);

		assertThrows(IllegalStateException.class, () -> m.getMetadatasForFilter(metadataExtractor));
		verifyNoMoreInteractions(metadataExtractor);
		final var extractedRawMtdFrames = m.close();

		when(metadataExtractor.getMetadatas(extractedRawMtdFrames)).thenReturn(frames);
		final var result = m.getMetadatasForFilter(metadataExtractor);
		assertNotNull(result);
		assertEquals(frames, result);
		verify(metadataExtractor, times(1)).getMetadatas(extractedRawMtdFrames);
		verifyNoMoreInteractions(metadataExtractor, frames);
	}

}
