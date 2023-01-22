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
 * Copyright (C) hdsdi3g for hd3g.tv 2023
 *
 */
package tv.hd3g.fflauncher.recipes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.MockitoAnnotations.openMocks;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import net.datafaker.Faker;
import tv.hd3g.fflauncher.ffprobecontainer.FFprobeVideoFrame;

class GOPStatItemTest {
	static Faker faker = net.datafaker.Faker.instance();

	int gopFrameCount;
	long gopDataSize;
	int bFramesCount;
	long iFrameDataSize;
	long bFramesDataSize;

	GOPStatItem g;

	@Mock
	List<FFprobeVideoFrame> videoFrames;

	@BeforeEach
	void init() throws Exception {
		openMocks(this).close();

		gopFrameCount = faker.random().nextInt();
		gopDataSize = faker.random().nextLong();
		bFramesCount = faker.random().nextInt();
		iFrameDataSize = faker.random().nextLong();
		bFramesDataSize = faker.random().nextLong();

		g = new GOPStatItem(gopFrameCount, gopDataSize, bFramesCount, iFrameDataSize, bFramesDataSize, videoFrames);
	}

	@AfterEach
	void ends() {
		verifyNoMoreInteractions(videoFrames);
	}

	@Test
	void testPFramesCount() {
		assertEquals(gopFrameCount - bFramesCount - 1, g.pFramesCount());
	}

	@Test
	void testPFramesDataSize() {
		assertEquals(gopDataSize - (iFrameDataSize + bFramesDataSize), g.pFramesDataSize());
	}
}
