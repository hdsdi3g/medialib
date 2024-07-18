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
 * Copyright (C) hdsdi3g for hd3g.tv 2024
 *
 */
package tv.hd3g.processlauncher.processingtool;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import tv.hd3g.commons.testtools.Fake;
import tv.hd3g.commons.testtools.MockToolsExtendsJunit;

@ExtendWith(MockToolsExtendsJunit.class)
class ProcessingToolResultTest {

	@Mock
	Object processedResult;
	@Mock
	ProcessingToolBuilder<Object, ParametersProvider, Object, ExecutorWatcher> builder;
	@Fake
	String fullCommandLine;

	ProcessingToolResult<Object, ParametersProvider, Object, ExecutorWatcher> p;

	@BeforeEach
	void init() {
		p = new ProcessingToolResult<>(builder, fullCommandLine, processedResult);
	}

	@Test
	void testProcessingToolResult() {
		assertEquals(processedResult, p.getResult());
		assertEquals(builder, p.getBuilder());
		assertEquals(fullCommandLine, p.getFullCommandLine());
	}

}
