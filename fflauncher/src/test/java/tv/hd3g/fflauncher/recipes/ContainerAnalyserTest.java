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
package tv.hd3g.fflauncher.recipes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.MockitoAnnotations.openMocks;
import static tv.hd3g.fflauncher.recipes.ContainerAnalyser.emptyWatcher;

import java.io.File;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import net.datafaker.Faker;
import tv.hd3g.fflauncher.FFprobe;
import tv.hd3g.processlauncher.cmdline.ExecutableFinder;
import tv.hd3g.processlauncher.cmdline.Parameters;

class ContainerAnalyserTest {
	static Faker faker = net.datafaker.Faker.instance();

	ContainerAnalyser ca;
	ContainerAnalyserSession cas;

	@Mock
	ExecutableFinder executableFinder;
	String execName;
	String fileName;

	@BeforeEach
	void init() throws Exception {
		openMocks(this).close();
		execName = faker.numerify("execName###");
		fileName = faker.numerify("fileName###");
		ca = new ContainerAnalyser(execName, executableFinder);
	}

	@AfterEach
	void end() {
		verifyNoMoreInteractions(executableFinder);
	}

	@Test
	void testCreateSessionFile() {
		cas = ca.createSession(new File(fileName), emptyWatcher);
		assertNotNull(cas);
		assertNull(cas.getSource());
		assertEquals(new File(fileName), cas.getSourceFile());
		assertEquals(ca, cas.getContainerAnalyser());
	}

	@Test
	void testCreateSessionString() {
		cas = ca.createSession(fileName, emptyWatcher);
		assertNotNull(cas);
		assertEquals(fileName, cas.getSource());
		assertNull(cas.getSourceFile());
		assertEquals(ca, cas.getContainerAnalyser());
	}

	@Test
	void testCreateFFprobe() {
		assertEquals(new FFprobe(execName, new Parameters()).getReadyToRunParameters(),
				ca.createFFprobe().getReadyToRunParameters());
	}

	@Test
	void testGetExecutableFinder() {
		assertEquals(executableFinder, ca.getExecutableFinder());
	}

}
