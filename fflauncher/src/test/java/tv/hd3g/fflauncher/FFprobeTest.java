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
 * Copyright (C) hdsdi3g for hd3g.tv 2018
 *
 */
package tv.hd3g.fflauncher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import tv.hd3g.fflauncher.FFprobe.FFPrintFormat;
import tv.hd3g.processlauncher.cmdline.Parameters;

class FFprobeTest {

	@Test
	void test() {
		final var parameters = new Parameters();
		final var p = new FFprobe("ffprobe", parameters);

		final var skip_base_cmdline = parameters.toString().length();

		assertFalse(p.isPretty());
		p.setPretty();
		assertEquals("-pretty", parameters.toString().substring(skip_base_cmdline));
		assertTrue(p.isPretty());

		parameters.clear();

		assertFalse(p.hasPrintFormat());
		p.setPrintFormat(FFPrintFormat.XML);
		assertEquals("-print_format xml", parameters.toString().substring(skip_base_cmdline));
		assertTrue(p.hasPrintFormat());

		parameters.clear();

		assertFalse(p.isShowFormat());
		p.setShowFormat();
		assertEquals("-show_format", parameters.toString().substring(skip_base_cmdline));
		assertTrue(p.isShowFormat());

		parameters.clear();

		assertFalse(p.isShowData());
		p.setShowData();
		assertEquals("-show_data", parameters.toString().substring(skip_base_cmdline));
		assertTrue(p.isShowData());

		parameters.clear();

		assertFalse(p.isShowError());
		p.setShowError();
		assertEquals("-show_error", parameters.toString().substring(skip_base_cmdline));
		assertTrue(p.isShowError());

		parameters.clear();

		assertFalse(p.isShowFrames());
		p.setShowFrames();
		assertEquals("-show_frames", parameters.toString().substring(skip_base_cmdline));
		assertTrue(p.isShowFrames());

		parameters.clear();

		assertFalse(p.isShowLog());
		p.setShowLog();
		assertEquals("-show_log", parameters.toString().substring(skip_base_cmdline));
		assertTrue(p.isShowLog());

		parameters.clear();

		assertFalse(p.isShowPackets());
		p.setShowPackets();
		assertEquals("-show_packets", parameters.toString().substring(skip_base_cmdline));
		assertTrue(p.isShowPackets());

		parameters.clear();

		assertFalse(p.isShowPrograms());
		p.setShowPrograms();
		assertEquals("-show_programs", parameters.toString().substring(skip_base_cmdline));
		assertTrue(p.isShowPrograms());

		parameters.clear();

		assertFalse(p.isShowStreams());
		p.setShowStreams();
		assertEquals("-show_streams", parameters.toString().substring(skip_base_cmdline));
		assertTrue(p.isShowStreams());

		parameters.clear();

		assertFalse(p.isShowChapters());
		p.setShowChapters();
		assertEquals("-show_chapters", parameters.toString().substring(skip_base_cmdline));
		assertTrue(p.isShowChapters());

		parameters.clear();
	}

}
