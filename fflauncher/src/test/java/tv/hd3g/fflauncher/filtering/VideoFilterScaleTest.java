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
package tv.hd3g.fflauncher.filtering;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VideoFilterScaleTest {

	VideoFilterScale f;

	@BeforeEach
	void init() {
		f = new VideoFilterScale();
	}

	@Test
	void testToFilter() {
		assertEquals("scale", f.toFilter().toString());
		f.setWidth("1");
		f.setHeight("2");
		f.setEval(VideoFilterScale.Eval.FRAME);
		f.setInterl(VideoFilterScale.Interl.ONLY_FLAGGED);
		f.setFlags("flag");
		f.setParam0("p0");
		f.setParam1("p1");
		f.setSize("sz");
		f.setInColorMatrix("inColorMatrix");
		f.setOutColorMatrix("outColorMatrix");
		f.setInRange("inRange");
		f.setOutRange("outRange");
		f.setInChromaLoc(VideoFilterScale.ChromaSampleLocation.BOTTOMLEFT);
		f.setOutChromaLoc(VideoFilterScale.ChromaSampleLocation.CENTER);
		f.setForceOriginalAspectRatio(VideoFilterScale.ForceOriginalAspectRatio.DECREASE);
		f.setForceDivisibleBy("forceDivisibleBy");
		assertEquals(
				"scale=width=1:height=2:eval=frame:interl=-1:flags=flag:param0=p0:param1=p1:size=sz:in_color_matrix=inColorMatrix:out_color_matrix=outColorMatrix:in_range=inRange:out_range=outRange:in_chroma_loc=bottomleft:out_chroma_loc=center:force_original_aspect_ratio=decrease:force_divisible_by=forceDivisibleBy",
				f.toFilter().toString());
	}
}
