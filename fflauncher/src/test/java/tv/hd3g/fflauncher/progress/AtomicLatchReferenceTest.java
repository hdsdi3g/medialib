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
package tv.hd3g.fflauncher.progress;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.MockitoAnnotations.openMocks;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class AtomicLatchReferenceTest {

	@Mock
	Object object;
	AtomicLatchReference<Object> alr;

	@BeforeEach
	void init() throws Exception {
		openMocks(this).close();
		alr = new AtomicLatchReference<>();
	}

	@AfterEach
	void ends() {
		verifyNoMoreInteractions(object);
	}

	@Nested
	class Simple {

		@Test
		void testGetEmpty() {
			assertThrows(IllegalStateException.class, () -> alr.get(1, MILLISECONDS));
		}

		@Test
		void testGetEmpty_interrupted() throws InterruptedException {
			final var checked = new AtomicBoolean();
			final var t = new Thread(() -> {
				alr.get(1, SECONDS);
				checked.set(true);
			});
			t.start();
			t.interrupt();
			assertFalse(checked.get());
		}

		@Test
		void testGet_direct() {
			alr.set(object);
			assertEquals(object, alr.get(1, MILLISECONDS));
		}

		@Test
		void testGet_parallel() throws InterruptedException {
			final var checked = new AtomicReference<>();
			final var t = new Thread(() -> {
				checked.set(alr.get(1, SECONDS));
			});
			t.start();
			alr.set(object);
			while (t.isAlive()) {
				Thread.onSpinWait();
			}
			assertEquals(object, checked.get());
		}
	}

	@Nested
	class Callbackmode {

		@Test
		void testGetEmpty() {
			final var checked = new AtomicReference<>();
			alr.get(1, MILLISECONDS, checked::set);
			assertNull(checked.get());
		}

		@Test
		void testGetDirect() {
			final var checked = new AtomicReference<>();
			alr.set(object);
			alr.get(1, MILLISECONDS, checked::set);
			assertEquals(object, checked.get());
		}

		@Test
		void testGetEmpty_interrupted() throws InterruptedException {
			final var checked = new AtomicBoolean();
			final var t = new Thread(() -> {
				alr.get(1, SECONDS, v -> checked.set(true));
			});
			t.start();
			t.interrupt();
			assertFalse(checked.get());
		}

		@Test
		void testGet_parallel() throws InterruptedException {
			final var checked = new AtomicReference<>();
			final var t = new Thread(() -> {
				alr.get(1, SECONDS, checked::set);
			});
			t.start();
			alr.set(object);
			while (t.isAlive()) {
				Thread.onSpinWait();
			}
			assertEquals(object, checked.get());
		}

	}

}
