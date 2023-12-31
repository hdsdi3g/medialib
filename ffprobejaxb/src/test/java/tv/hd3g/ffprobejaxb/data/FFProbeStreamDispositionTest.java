/*
 * This file is part of ffprobejaxb.
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
package tv.hd3g.ffprobejaxb.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class FFProbeStreamDispositionTest {

	FFProbeStreamDisposition d;

	@Test
	void test() {
		d = FFProbeStreamDisposition.getByNames("default");
		assertNotNull(d);
		assertTrue(d.asDefault());
		assertFalse(d.forced());
		assertThat(d.resumeDispositions().toList()).hasSameElementsAs(List.of("default"));

		d = FFProbeStreamDisposition.getByNames("asdefault");
		assertNotNull(d);
		assertTrue(d.asDefault());
		assertThat(d.resumeDispositions().toList()).hasSameElementsAs(List.of("default"));

		d = FFProbeStreamDisposition.getByNames("asDefault");
		assertNotNull(d);
		assertTrue(d.asDefault());
		assertThat(d.resumeDispositions().toList()).hasSameElementsAs(List.of("default"));

		d = FFProbeStreamDisposition.getByNames("default", "forced");
		assertNotNull(d);
		assertTrue(d.asDefault());
		assertTrue(d.forced());
		assertThat(d.resumeDispositions().toList()).hasSameElementsAs(List.of("default", "forced"));

		d = FFProbeStreamDisposition.getByNames("hearingImpaired");
		assertNotNull(d);
		assertFalse(d.asDefault());
		assertTrue(d.hearingImpaired());
		assertThat(d.resumeDispositions().toList()).hasSameElementsAs(List.of("hearing impaired"));

		d = FFProbeStreamDisposition.getByNames("hearingimpaired");
		assertNotNull(d);
		assertFalse(d.asDefault());
		assertTrue(d.hearingImpaired());
		assertThat(d.resumeDispositions().toList()).hasSameElementsAs(List.of("hearing impaired"));

		d = FFProbeStreamDisposition.getByNames("hearing_impaired");
		assertNotNull(d);
		assertFalse(d.asDefault());
		assertTrue(d.hearingImpaired());
		assertThat(d.resumeDispositions().toList()).hasSameElementsAs(List.of("hearing impaired"));
	}
}
