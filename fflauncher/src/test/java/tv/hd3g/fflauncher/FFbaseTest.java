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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import tv.hd3g.fflauncher.enums.FFLogLevel;
import tv.hd3g.processlauncher.cmdline.ExecutableFinder;
import tv.hd3g.processlauncher.cmdline.Parameters;

class FFbaseTest {

	final ExecutableFinder executableFinder;

	FFbaseTest() {
		executableFinder = new ExecutableFinder();
	}

	private static class FFbaseImpl extends FFbase {
		private static final String execName = "ffmpeg";

		FFbaseImpl(final Parameters parameters) throws IOException {
			super(execName, parameters);
		}

	}

	@Test
	void testBase() throws Exception {
		final var b = new FFbaseImpl(new Parameters());
		final var about = b.getAbout(executableFinder);

		assertNotNull(about.getVersion(), "version");
		assertFalse(about.getCodecs().isEmpty(), "codecs empty");
		assertFalse(about.getFormats().isEmpty(), "formats empty");
		assertFalse(about.getDevices().isEmpty(), "devices empty");
		assertFalse(about.getBitStreamFilters().isEmpty(), "bitstream empty");
		assertNotNull(about.getProtocols(), "protocols");
		assertFalse(about.getFilters().isEmpty(), "filters empty");
		assertFalse(about.getPixelFormats().isEmpty(), "pixelFormats empty");

		assertTrue(about.isCoderIsAvaliable("ffv1"), "Coder Avaliable");
		assertFalse(about.isCoderIsAvaliable("nonono"), "Coder notAvaliable");
		assertTrue(about.isDecoderIsAvaliable("rl2"), "Decoder Avaliable");
		assertFalse(about.isDecoderIsAvaliable("nonono"), "Decoder notAvaliable");
		assertTrue(about.isFilterIsAvaliable("color"), "Filter Avaliable");
		assertFalse(about.isFilterIsAvaliable("nonono"), "Filter notAvaliable");
		assertTrue(about.isToFormatIsAvaliable("wav"), "Format Avaliable");
		assertFalse(about.isToFormatIsAvaliable("nonono"), "Format notAvaliable");
	}

	@Test
	void testNVPresence() throws Exception {
		final var b = new FFbaseImpl(new Parameters());

		if (System.getProperty("ffmpeg.test.nvidia", "").equals("1")) {
			assertTrue(b.getAbout(executableFinder)
			        .isNVToolkitIsAvaliable(), "Can't found NV lib like cuda, cuvid and nvenc");
		}
		if (System.getProperty("ffmpeg.test.libnpp", "").equals("1")) {
			assertTrue(b.getAbout(executableFinder).isHardwareNVScalerFilterIsAvaliable(), "Can't found libnpp");
		}
	}

	@Test
	void testParams() throws IOException {
		final var b = new FFbaseImpl(new Parameters());
		assertFalse(b.isLogLevelSet());

		final var skip_base_cmdline = b.getInternalParameters().toString().length();

		b.setLogLevel(FFLogLevel.FATAL, true, true);
		assertEquals("-loglevel repeat+level+fatal", b.getInternalParameters().toString().substring(skip_base_cmdline));

		b.setLogLevel(FFLogLevel.DEBUG, false, false);
		assertEquals("-loglevel repeat+level+fatal", b.getInternalParameters().toString().substring(skip_base_cmdline));

		assertTrue(b.isLogLevelSet());

		b.getInternalParameters().clear();

		assertFalse(b.isHidebanner());
		b.setHidebanner();
		assertEquals("-hide_banner", b.getInternalParameters().toString().substring(skip_base_cmdline));
		assertTrue(b.isHidebanner());

		b.getInternalParameters().clear();

		assertFalse(b.isOverwriteOutputFiles());
		b.setOverwriteOutputFiles();
		assertEquals("-y", b.getInternalParameters().toString().substring(skip_base_cmdline));
		assertTrue(b.isOverwriteOutputFiles());

		b.getInternalParameters().clear();

		assertFalse(b.isNeverOverwriteOutputFiles());
		b.setNeverOverwriteOutputFiles();
		assertEquals("-n", b.getInternalParameters().toString().substring(skip_base_cmdline));
		assertTrue(b.isNeverOverwriteOutputFiles());
	}

	@Test
	void testAddVarInParametersIfNotExists_withParams() throws IOException {
		final var b = new FFbaseImpl(Parameters.bulk("-param0 -param1 param2"));
		b.addSimpleInputSource("s0");
		assertEquals("<%IN_AUTOMATIC_0%> -param0 -param1 param2",
		        b.getInternalParameters().toString());
		b.addSimpleInputSource("s1");
		assertEquals("<%IN_AUTOMATIC_0%> <%IN_AUTOMATIC_1%> -param0 -param1 param2",
		        b.getInternalParameters().toString());
	}

	@Test
	void testAddVarInParametersIfNotExists_withoutParams() throws IOException {
		final var b = new FFbaseImpl(new Parameters());
		b.addSimpleInputSource("s0");
		assertEquals("<%IN_AUTOMATIC_0%>",
		        b.getInternalParameters().toString());
		b.addSimpleInputSource("s1");
		assertEquals("<%IN_AUTOMATIC_0%> <%IN_AUTOMATIC_1%>",
		        b.getInternalParameters().toString());
	}

	@Test
	void testAddVarInParametersIfNotExists_varExists() throws IOException {
		final var b = new FFbaseImpl(Parameters.of("<%IN_AUTOMATIC_0%>"));
		b.addSimpleInputSource("s0");
		assertEquals("<%IN_AUTOMATIC_0%>",
		        b.getInternalParameters().toString());
		b.addSimpleInputSource("s1");
		assertEquals("<%IN_AUTOMATIC_0%> <%IN_AUTOMATIC_1%>",
		        b.getInternalParameters().toString());
	}

}
