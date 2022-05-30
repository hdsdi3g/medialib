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
 * Copyright (C) hdsdi3g for hd3g.tv 2019
 *
 */
package tv.hd3g.processlauncher.cmdline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import tv.hd3g.processlauncher.Tool;

class CommandLineTest {

	private final ExecutableFinder ef;
	private final CommandLine cmd;
	private final Parameters parametersSource;

	CommandLineTest() throws IOException {
		parametersSource = Parameters.of("-a");
		Tool.patchTestExec();

		ef = new ExecutableFinder();
		cmd = new CommandLine("test-exec", parametersSource, ef);
		parametersSource.addParameters("-b");
	}

	@Test
	void testGetExecutableFinder() {
		assertEquals(ef, cmd.getExecutableFinder().get());
	}

	@Test
	void testGetExecutable() throws FileNotFoundException {
		assertEquals(ef.get("test-exec"), cmd.getExecutable());
	}

	@Test
	void testGetParameters() {
		assertNotSame(parametersSource, cmd.getParameters());
		assertNotEquals(parametersSource.toString(), cmd.getParameters().toString());
	}

}
