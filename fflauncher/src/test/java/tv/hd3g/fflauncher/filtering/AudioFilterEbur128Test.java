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

import static java.util.Collections.unmodifiableSortedSet;
import static net.datafaker.Faker.instance;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.datafaker.Faker;
import tv.hd3g.fflauncher.filtering.AudioFilterEbur128.Framelog;
import tv.hd3g.fflauncher.filtering.AudioFilterEbur128.Gauge;
import tv.hd3g.fflauncher.filtering.AudioFilterEbur128.Peak;
import tv.hd3g.fflauncher.filtering.AudioFilterEbur128.Scale;

class AudioFilterEbur128Test {
	static Faker faker = instance();

	AudioFilterEbur128 f;
	Framelog framelog;
	SortedSet<Peak> peaks;
	Gauge gauge;
	Scale scale;

	@BeforeEach
	void init() throws Exception {
		framelog = faker.options().option(Framelog.class);
		peaks = unmodifiableSortedSet(new TreeSet<>(Set.of(faker.options().option(Peak.class))));
		gauge = faker.options().option(Gauge.class);
		scale = faker.options().option(Scale.class);
		f = new AudioFilterEbur128();
	}

	@Test
	void testToFilter() {
		assertEquals("ebur128", f.toFilter().toString());
		f.setFramelog(framelog);
		assertEquals("ebur128=framelog=" + framelog.toString(), f.toFilter().toString());

	}

	@Test
	void testToFilter_framelog() {
		f.setFramelog(framelog);
		assertEquals("ebur128=framelog=" + framelog.toString(), f.toFilter().toString());
	}

	@Test
	void testToFilter_gauge() {
		f.setGauge(gauge);
		assertEquals("ebur128=gauge=" + gauge.toString(), f.toFilter().toString());
	}

	@Test
	void testToFilter_scale() {
		f.setScale(scale);
		assertEquals("ebur128=scale=" + scale.toString(), f.toFilter().toString());
	}

	@Test
	void testToFilter_peak() {
		f.setPeakMode(peaks);
		assertEquals("ebur128=peak=" + peaks.stream().findFirst().get(), f.toFilter().toString());
	}

}
