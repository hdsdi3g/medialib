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
package tv.hd3g.fflauncher.processingtool;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import tv.hd3g.commons.testtools.MockToolsExtendsJunit;
import tv.hd3g.fflauncher.SimpleSourceTraits;

@ExtendWith(MockToolsExtendsJunit.class)
class FFSourceDefinitionTest {

	@Mock
	SimpleSourceTraits input;

	FFSourceDefinition ffsd;

	@Test
	void testNoSourceInput() {
		ffsd = FFSourceDefinition.noSourceInput();
		assertNotNull(ffsd);
		ffsd.applySourceToFF(input);
	}

}
