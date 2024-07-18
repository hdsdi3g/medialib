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
package tv.hd3g.fflauncher.recipes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import java.awt.Point;
import java.io.File;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;

import tv.hd3g.fflauncher.enums.OutputFilePresencePolicy;
import tv.hd3g.processlauncher.cmdline.ExecutableFinder;

class ProbeMediaTest {

	final ExecutableFinder executableFinder;

	ProbeMediaTest() {
		executableFinder = new ExecutableFinder();
	}

	@Test
	void test() {
		final var gvf = new GenerateVideoFile(executableFinder);

		final var tDir = System.getProperty("java.io.tmpdir");
		final var testFileToCreate = new File(tDir + File.separator + "smptebars-" + System.nanoTime() + ".mkv");
		final var ffmpeg = gvf.generateBarsAnd1k(testFileToCreate, 1, new Point(768, 432));

		ffmpeg.checkDestinations();
		final var outputFiles = ffmpeg.getOutputFiles(OutputFilePresencePolicy.ALL, null);
		assertEquals(testFileToCreate, outputFiles.get(0));
		assertEquals(1, outputFiles.size());
		assertNotSame(0, testFileToCreate.length());

		final var probe = new ProbeMedia(Executors.newSingleThreadScheduledExecutor());
		probe.setExecutableFinder(executableFinder);
		final var result = probe.process(testFileToCreate).getResult();

		assertEquals(1, Math.round(result.getFormat().get().duration()));
		assertEquals(432, result.getVideoStreams().findFirst().get().height());

		ffmpeg.cleanUpOutputFiles(true, false, null);
	}

}
