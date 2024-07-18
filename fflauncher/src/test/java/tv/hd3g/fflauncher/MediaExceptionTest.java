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
package tv.hd3g.fflauncher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.MockitoAnnotations.openMocks;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class MediaExceptionTest {
	String message;
	@Mock
	Throwable cause;

	MediaException e;

	@BeforeEach
	void init() throws Exception {
		openMocks(this).close();
	}

	@AfterEach
	void ends() {
		verifyNoMoreInteractions(cause);
	}

	@Test
	void testMediaExceptionString() {
		e = new MediaException(message);
		assertEquals(message, e.getMessage());
		assertNull(e.getCause());
	}

	@Test
	void testMediaExceptionThrowable() {
		e = new MediaException(cause);
		assertNull(message);
		assertEquals(cause, e.getCause());
	}

	@Test
	void testMediaExceptionStringThrowable() {
		e = new MediaException(message, cause);
		assertEquals(message, e.getMessage());
		assertEquals(cause, e.getCause());
	}

}
