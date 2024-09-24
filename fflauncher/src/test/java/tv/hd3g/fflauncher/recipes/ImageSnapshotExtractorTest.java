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
 * Copyright (C) hdsdi3g for hd3g.tv 2024
 *
 */
package tv.hd3g.fflauncher.recipes;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Point;
import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import tv.hd3g.processlauncher.cmdline.ExecutableFinder;

class ImageSnapshotExtractorTest {

	@Test
	void test() throws IOException {
		final var executableFinder = new ExecutableFinder();
		final var gvf = new GenerateVideoFile(executableFinder);
		final var testFile = File.createTempFile("minismptebars", ".mkv");

		final var ffmpeg = gvf.generateBarsAnd1k(testFile, 10, new Point(128, 128));

		final var ise = new ImageSnapshotExtractor(executableFinder);
		assertThat(ise.bestEffortTimedSnapshot(testFile, "00:00:00", 10)).isNotEmpty();
		assertThat(ise.preciseTimedSnapshot(testFile, "00:00:00", 0)).isNotEmpty();
		ffmpeg.cleanUpOutputFiles(true, true, null);
	}

}
