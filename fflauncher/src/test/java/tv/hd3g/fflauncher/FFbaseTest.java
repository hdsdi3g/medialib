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

import tv.hd3g.fflauncher.enums.FFLogLevel;
import tv.hd3g.processlauncher.cmdline.ExecutableFinder;
import tv.hd3g.processlauncher.cmdline.Parameters;

class FFbaseTest {

	final ExecutableFinder executableFinder;

	FFbaseTest() {
		executableFinder = new ExecutableFinder();
	}

	private static class FFbaseImpl extends FFbase {
		FFbaseImpl(final Parameters parameters) {
			super("ffmpeg", parameters);
		}
	}

	@Test
	void testParams() {
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
	void testAddVarInParametersIfNotExists_withParams() {
		final var b = new FFbaseImpl(Parameters.bulk("-param0 -param1 param2"));
		b.addSimpleInputSource("s0");
		assertEquals("<%IN_AUTOMATIC_0%> -param0 -param1 param2",
				b.getInternalParameters().toString());
		b.addSimpleInputSource("s1");
		assertEquals("<%IN_AUTOMATIC_0%> <%IN_AUTOMATIC_1%> -param0 -param1 param2",
				b.getInternalParameters().toString());
	}

	@Test
	void testAddVarInParametersIfNotExists_withoutParams() {
		final var b = new FFbaseImpl(new Parameters());
		b.addSimpleInputSource("s0");
		assertEquals("<%IN_AUTOMATIC_0%>",
				b.getInternalParameters().toString());
		b.addSimpleInputSource("s1");
		assertEquals("<%IN_AUTOMATIC_0%> <%IN_AUTOMATIC_1%>",
				b.getInternalParameters().toString());
	}

	@Test
	void testAddVarInParametersIfNotExists_varExists() {
		final var b = new FFbaseImpl(Parameters.of("<%IN_AUTOMATIC_0%>"));
		b.addSimpleInputSource("s0");
		assertEquals("<%IN_AUTOMATIC_0%>",
				b.getInternalParameters().toString());
		b.addSimpleInputSource("s1");
		assertEquals("<%IN_AUTOMATIC_0%> <%IN_AUTOMATIC_1%>",
				b.getInternalParameters().toString());
	}

}
