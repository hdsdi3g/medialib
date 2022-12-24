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
package tv.hd3g.fflauncher.filtering;

import static net.datafaker.Faker.instance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static tv.hd3g.fflauncher.filtering.lavfimtd.Utility.getFramesFromString;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.datafaker.Faker;
import tv.hd3g.fflauncher.filtering.VideoFilterCropdetect.LavfiMtdCropdetect;
import tv.hd3g.fflauncher.filtering.VideoFilterCropdetect.Mode;
import tv.hd3g.fflauncher.filtering.lavfimtd.LavfiMtdPosition;

class VideoFilterCropdetectTest {
	static Faker faker = instance();

	private static final String RAW_LINES = """
			frame:1801 pts:60041 pts_time:60.041
			lavfi.cropdetect.x1=0
			lavfi.cropdetect.x2=479
			lavfi.cropdetect.y1=0
			lavfi.cropdetect.y2=479
			lavfi.cropdetect.w=480
			lavfi.cropdetect.h=480
			lavfi.cropdetect.x=0
			lavfi.cropdetect.y=0
			frame:7616 pts:317340 pts_time:317.34
			lavfi.cropdetect.x1=0
			lavfi.cropdetect.x2=1919
			lavfi.cropdetect.y1=0
			lavfi.cropdetect.y2=1079
			lavfi.cropdetect.w=1920
			lavfi.cropdetect.h=1072
			lavfi.cropdetect.x=0
			lavfi.cropdetect.y=4
			""";

	VideoFilterCropdetect f;
	Mode mode;

	@BeforeEach
	void init() throws Exception {
		mode = faker.options().option(Mode.class);
		f = new VideoFilterCropdetect(mode);
	}

	@Test
	void testToFilter() {
		assertEquals("cropdetect=mode=" + mode.toString(), f.toFilter().toString());
	}

	@Test
	void testGetMetadatas() {
		final var frames = f.getMetadatas(getFramesFromString(RAW_LINES));
		assertNotNull(frames);
		assertEquals(Map.of(
				new LavfiMtdPosition(1801, 60041, 60.041f),
				new LavfiMtdCropdetect(0, 479, 0, 479, 480, 480, 0, 0),
				new LavfiMtdPosition(7616, 317340, 317.34f),
				new LavfiMtdCropdetect(0, 1919, 0, 1079, 1920, 1072, 0, 4)),
				frames.getFrames());
	}

}
