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

import static java.lang.Float.NEGATIVE_INFINITY;
import static net.datafaker.Faker.instance;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.datafaker.Faker;

class Ebur128StrErrFilterEventTest {

	static Faker faker = instance();

	Ebur128StrErrFilterEvent fe;

	float t;
	float target;
	float m;
	float s;
	float i;
	float lra;
	Stereo<Float> spk;
	Stereo<Float> ftpk;
	Stereo<Float> tpk;

	@BeforeEach
	void init() throws Exception {
		t = faker.random().nextFloat();
		target = faker.random().nextFloat();
		m = faker.random().nextFloat();
		s = faker.random().nextFloat();
		i = faker.random().nextFloat();
		lra = faker.random().nextFloat();
		spk = new Stereo<>(faker.random().nextFloat(), faker.random().nextFloat());
		ftpk = new Stereo<>(faker.random().nextFloat(), faker.random().nextFloat());
		tpk = new Stereo<>(faker.random().nextFloat(), faker.random().nextFloat());

		fe = new Ebur128StrErrFilterEvent(
				"t: " + t + "    TARGET:" + target
										  + " LUFS    M: " + m + " S:" + s
										  + "     I: " + i + " LUFS       LRA:   " + lra
										  + " LU  SPK:  " + spk.left() + "  " + spk.right()
										  + " dBFS  FTPK: " + ftpk.left() + " " + ftpk.right()
										  + " dBFS  TPK:  " + tpk.left() + "  " + tpk.right() + " dBFS");

	}

	@Test
	void testCheckNull() {
		fe = new Ebur128StrErrFilterEvent("");
		assertEquals(NEGATIVE_INFINITY, fe.getT());
		assertEquals(NEGATIVE_INFINITY, fe.getTarget());
		assertEquals(NEGATIVE_INFINITY, fe.getM());
		assertEquals(NEGATIVE_INFINITY, fe.getS());
		assertEquals(NEGATIVE_INFINITY, fe.getI());
		assertEquals(NEGATIVE_INFINITY, fe.getLra());
		assertEquals(new Stereo<>(NEGATIVE_INFINITY, NEGATIVE_INFINITY), fe.getSpk());
		assertEquals(new Stereo<>(NEGATIVE_INFINITY, NEGATIVE_INFINITY), fe.getFtpk());
		assertEquals(new Stereo<>(NEGATIVE_INFINITY, NEGATIVE_INFINITY), fe.getTpk());
	}

	@Test
	void testGetT() {
		assertEquals(t, fe.getT());
	}

	@Test
	void testGetTarget() {
		assertEquals(target, fe.getTarget());
	}

	@Test
	void testGetM() {
		assertEquals(m, fe.getM());
	}

	@Test
	void testGetS() {
		assertEquals(s, fe.getS());
	}

	@Test
	void testGetI() {
		assertEquals(i, fe.getI());
	}

	@Test
	void testGetLra() {
		assertEquals(lra, fe.getLra());
	}

	@Test
	void testGetSpk() {
		assertEquals(spk, fe.getSpk());
	}

	@Test
	void testGetFtpk() {
		assertEquals(ftpk, fe.getFtpk());
	}

	@Test
	void testGetTpk() {
		assertEquals(tpk, fe.getTpk());
	}

}
