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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

class StdInInjectionTest {

	@Test
	void testInject() throws IOException {
		final var baos = new ByteArrayOutputStream();

		final var sii = new StdInInjection(baos);

		final var test = new byte[] { 1, 2, 3 };
		sii.write(test);
		sii.close();
		var result = baos.toByteArray();

		assertTrue(Arrays.equals(test, result));

		baos.reset();
		sii.println("test !", StandardCharsets.UTF_8);

		result = baos.toByteArray();

		assertTrue(Arrays.equals(("test !" + StdInInjection.LINESEPARATOR).getBytes(StandardCharsets.UTF_8), result));
	}

}
