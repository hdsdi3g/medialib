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

import static tv.hd3g.fflauncher.recipes.MediaAnalyser.assertAndParse;
import static tv.hd3g.fflauncher.recipes.MediaAnalyser.splitter;

import java.util.ArrayList;
import java.util.List;

import tv.hd3g.fflauncher.filtering.lavfimtd.LavfiMtdProgramFrames;
import tv.hd3g.fflauncher.filtering.lavfimtd.LavfiMtdProgramFramesExtractor;
import tv.hd3g.fflauncher.filtering.lavfimtd.LavfiRawMtdFrame;

public class MetadataFilterFrameParser {
	private final List<LavfiRawMtdFrame> frames;
	private final List<String> bucket;
	private LavfiMetadataFilterFrame currentFrame;

	public MetadataFilterFrameParser() {
		frames = new ArrayList<>();
		bucket = new ArrayList<>();
	}

	/**
	 * @param line only inject lavfi metadata stream, like "frame:1022 pts:981168 pts_time:20.441", "lavfi.aphasemeter.phase=1.000000", "lavfi.aphasemeter.mono_start=18.461"
	 */
	public void onLine(final String line) {
		if (line.startsWith("frame:")) {
			if (bucket.isEmpty() == false) {
				currentFrame.setRawLines(bucket.stream());
				frames.add(currentFrame);
				bucket.clear();
			}
			currentFrame = parseFrameLine(line);
		} else if (currentFrame == null) {
			throw new IllegalArgumentException("Missing frame declaration: " + line);
		} else if (line.startsWith("lavfi.")) {
			bucket.add(line);
		} else {
			throw new IllegalArgumentException("Invalid line content inside frame: " + line);
		}
	}

	public List<LavfiRawMtdFrame> close() {
		if (bucket.isEmpty() == false && currentFrame != null) {
			currentFrame.setRawLines(bucket.stream());
			frames.add(currentFrame);
			bucket.clear();
		}
		return frames;
	}

	public <T, U extends LavfiMtdProgramFramesExtractor<T>> LavfiMtdProgramFrames<T> getMetadatasForFilter(final U metadataExtractor) {
		if (bucket.isEmpty() == false && currentFrame != null) {
			throw new IllegalStateException("You must close() before call this");
		}
		return metadataExtractor.getMetadatas(frames);
	}

	private LavfiMetadataFilterFrame parseFrameLine(final String line) {
		/*
		 * frame:1022 pts:981168  pts_time:20.441
		 * */
		final var items = splitter(line, ' ');
		return new LavfiMetadataFilterFrame(
				assertAndParse(items.get(0), "frame:", Integer::valueOf),
				assertAndParse(items.get(1), "pts:", Long::valueOf),
				assertAndParse(items.get(2), "pts_time:", Float::valueOf));
	}

}
