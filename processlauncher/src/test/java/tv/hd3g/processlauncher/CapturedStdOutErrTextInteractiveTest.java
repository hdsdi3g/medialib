/*
 * This file is part of processlauncher.
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
 * Copyright (C) hdsdi3g for hd3g.tv 2019
 *
 */
package tv.hd3g.processlauncher;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class CapturedStdOutErrTextInteractiveTest {

	@Test
	void test() {
		final var source = Mockito.mock(ProcesslauncherLifecycle.class);
		Mockito.when(source.isRunning()).thenReturn(true);

		final var baos = new ByteArrayOutputStream();
		final var stdInInject = new StdInInjection(baos);

		Mockito.when(source.getStdInInjection()).thenReturn(stdInInject);

		final List<LineEntry> capturedLe = new ArrayList<>();
		final Function<LineEntry, String> interactive = le -> {
			if (le.source().equals(source) == false) {
				throw new IllegalStateException("Invalid source");
			}
			capturedLe.add(le);
			return le.line().toUpperCase();
		};

		final var csoeti = new CapturedStdOutErrTextInteractive(interactive);
		final var added = new LineEntry(0, "My text", true, source);
		csoeti.onText(added);

		assertEquals(1, capturedLe.size());
		assertEquals(added, capturedLe.get(0));
		assertEquals("My text".toUpperCase() + System.lineSeparator(), new String(baos.toByteArray()));
	}
}
