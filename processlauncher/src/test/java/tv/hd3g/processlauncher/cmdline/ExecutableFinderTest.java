/*
 * This file is part of processlauncher.
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
package tv.hd3g.processlauncher.cmdline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import tv.hd3g.processlauncher.Tool;

class ExecutableFinderTest {

	ExecutableFinderTest() {
		Tool.patchTestExec();
	}

	@Test
	void testPreCheck() {
		assertEquals("\\", "/".replaceAll("/", "\\\\"));
		assertEquals("/", "\\".replaceAll("\\\\", "/"));
	}

	@Test
	void test() throws IOException {
		final var ef = new ExecutableFinder();

		assertTrue(ef.getFullPath().contains(new File(System.getProperty("user.dir"))));

		final var exec = ef.get("test-exec");
		if (File.separator.equals("/")) {
			assertEquals("test-exec", exec.getName());
		} else {
			assertEquals("test-exec.bat", exec.getName());
		}
	}

	@Test
	void testRegisterExecutable() throws IOException {
		var ef = new ExecutableFinder();

		final var element = ef.get("test-exec");

		ef = new ExecutableFinder();
		ef.registerExecutable("other-test", element);

		assertEquals(element.getPath(), ef.get("other-test").getPath());

		ef.get("java");
		ef.registerExecutable("java", element);
		assertEquals(element.getPath(), ef.get("java").getPath());
	}
}
