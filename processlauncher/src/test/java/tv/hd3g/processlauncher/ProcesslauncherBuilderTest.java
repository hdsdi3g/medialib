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
package tv.hd3g.processlauncher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import tv.hd3g.processlauncher.cmdline.ExecutableFinder;

class ProcesslauncherBuilderTest {

	private ProcesslauncherBuilder pb;
	private final File execFile;

	ProcesslauncherBuilderTest() throws FileNotFoundException {
		Tool.patchTestExec();
		execFile = new ExecutableFinder().get("test-exec");
	}

	@BeforeEach
	void setUp() throws Exception {
		pb = new ProcesslauncherBuilder(execFile, Arrays.asList("p"));
	}

	@Test
	void testGetSetEnvironmentVar() {
		pb.setEnvironmentVar("foo", "bar");
		assertEquals("bar", pb.getEnvironmentVar("foo"));
	}

	@Test
	void testGetSetEnvironmentVarWinPath() {
		if (System.getProperty("os.name").toLowerCase().indexOf("win") > -1) {
			pb.setEnvironmentVar("path", "foo");
			assertEquals("foo", pb.getEnvironmentVar("Path"));
			assertEquals("foo", pb.getEnvironmentVar("PATH"));
		}
	}

	@Test
	void testSetEnvironmentVarIfNotFound() {
		pb.setEnvironmentVarIfNotFound("foo", "bar");
		pb.setEnvironmentVarIfNotFound("foo", "tot");
		assertEquals("bar", pb.getEnvironmentVar("foo"));
	}

	@Test
	void testForEachEnvironmentVar() {
		pb.setEnvironmentVar("foo1", "bar1");
		pb.setEnvironmentVar("foo2", "bar2");

		final var val = new HashMap<String, String>();
		pb.forEachEnvironmentVar((k, v) -> {
			val.put(k, v);
		});
		assertEquals("bar1", val.get("foo1"));
		assertEquals("bar2", val.get("foo2"));
	}

	@Test
	void testGetSetWorkingDirectory() throws IOException {
		assertNull(pb.getWorkingDirectory());
		pb.setWorkingDirectory(new File("."));
		assertEquals(new File("."), pb.getWorkingDirectory());

		try {
			pb.setWorkingDirectory(new File("./DontExists"));
			Assertions.fail();
		} catch (final IOException e) {
		}
		try {
			pb.setWorkingDirectory(execFile);
			Assertions.fail();
		} catch (final IOException e) {
		}
	}

	@Test
	void testSetIsExecCodeMustBeZero() {
		assertTrue(pb.isExecCodeMustBeZero());
		pb.setExecCodeMustBeZero(false);
		assertFalse(pb.isExecCodeMustBeZero());
	}

	@Test
	void testGetExecutionCallbackers() {
		assertEquals(0, pb.getExecutionCallbackers().size());
	}

	@Test
	void testAddExecutionCallbacker() {
		final var executionCallbacker0 = Mockito.mock(ExecutionCallbacker.class);
		final var executionCallbacker1 = Mockito.mock(ExecutionCallbacker.class);

		pb.addExecutionCallbacker(executionCallbacker0);
		pb.addExecutionCallbacker(executionCallbacker1);

		assertEquals(executionCallbacker0, pb.getExecutionCallbackers().get(0));
		assertEquals(executionCallbacker1, pb.getExecutionCallbackers().get(1));
	}

	@Test
	void testRemoveExecutionCallbacker() {
		final var executionCallbacker = Mockito.mock(ExecutionCallbacker.class);
		pb.addExecutionCallbacker(executionCallbacker);
		pb.removeExecutionCallbacker(executionCallbacker);
		assertEquals(0, pb.getExecutionCallbackers().size());
	}

	@Test
	void testGetSetExecutionTimeLimiter() {
		assertFalse(pb.getExecutionTimeLimiter().isPresent());

		final var executionTimeLimiter = Mockito.mock(ExecutionTimeLimiter.class);
		pb.setExecutionTimeLimiter(executionTimeLimiter);

		assertEquals(executionTimeLimiter, pb.getExecutionTimeLimiter().get());
	}

	@Test
	void testGetSetExternalProcessStartup() {
		assertFalse(pb.getExternalProcessStartup().isPresent());

		final var externalProcessStartup = Mockito.mock(ExternalProcessStartup.class);
		pb.setExternalProcessStartup(externalProcessStartup);

		assertEquals(externalProcessStartup, pb.getExternalProcessStartup().get());
	}

	@Test
	void testSetGetCaptureStandardOutput() {
		assertFalse(pb.getCaptureStandardOutput().isPresent());

		final var captureStandardOutput = Mockito.mock(CaptureStandardOutput.class);
		pb.setCaptureStandardOutput(captureStandardOutput);

		assertEquals(captureStandardOutput, pb.getCaptureStandardOutput().get());
	}

	@Test
	void testMakeProcessBuilder() {
		final var processb = pb.makeProcessBuilder();
		assertEquals(pb.getFullCommandLine(), processb.command().stream().collect(Collectors.joining(" ")));
	}

	@Test
	void testGetFullCommandLine() {
		assertEquals(ProcesslauncherBuilder.addQuotesIfSpaces.apply(execFile.getAbsolutePath()) + " p", pb
		        .getFullCommandLine());
	}

	@Test
	void testToString() {
		assertEquals(pb.getFullCommandLine(), pb.toString());
	}

	@Test
	void testGetExecutableName() {
		assertEquals(execFile.getName(), pb.getExecutableName());
	}

}
