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
 * Copyright (C) hdsdi3g for hd3g.tv 2019
 *
 */
package tv.hd3g.fflauncher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import tv.hd3g.processlauncher.cmdline.Parameters;

class ConversionToolParameterReferenceTest {

	final File tempFile;

	ConversionToolParameterReferenceTest() throws IOException {
		tempFile = File.createTempFile("bintest", ".txt");
	}

	ConversionToolParameterReference ctprS;
	ConversionToolParameterReference ctprF;

	@BeforeEach
	void setUp() throws Exception {
		ctprS = new ConversionToolParameterReference("reference", "<%var%>", Arrays.asList("before1", "before2"));
		ctprF = new ConversionToolParameterReference(tempFile, "<%var%>", Arrays.asList("before1", "before2"));
	}

	@Test
	void testNullConstructor() {
		final var ctprS = new ConversionToolParameterReference("reference", "<%var%>",
		        null);
		final var ctprF = new ConversionToolParameterReference(tempFile, "<%var%>", null);

		assertEquals(Collections.emptyList(), ctprS.getParametersListBeforeRef());
		assertEquals(Collections.emptyList(), ctprF.getParametersListBeforeRef());
	}

	@Test
	void testGetRessource() {
		assertEquals("reference", ctprS.getRessource());
		assertEquals(tempFile.getPath(), ctprF.getRessource());
	}

	@Test
	void testGetParametersListBeforeRef() {
		assertEquals(Arrays.asList("before1", "before2"), ctprS.getParametersListBeforeRef());
	}

	@Test
	void testGetVarNameInParameters() {
		assertEquals("<%var%>", ctprS.getVarNameInParameters());
	}

	@Test
	void testIsVarNameInParametersEquals() {
		assertTrue(ctprS.isVarNameInParametersEquals("<%var%>"));
	}

	@Test
	void testToString() {
		assertEquals(ctprS.getRessource(), ctprS.toString());
		assertEquals(ctprF.getRessource(), ctprF.toString());
	}

	@Test
	void testCheckOpenRessourceAsFile() throws IOException, InterruptedException {
		ctprS.checkOpenRessourceAsFile();
		ctprF.checkOpenRessourceAsFile();
		assertThrows(IOException.class, () -> {
			new ConversionToolParameterReference(new File("nope"), "<%var%>", null).checkOpenRessourceAsFile();
		}, "Expected exception from here: file not exists");

	}

	@Nested
	class ManageCollisionsParameters {

		@Test
		void noCollision() {
			final var ctpr = new ConversionToolParameterReference("ref", "<%var%>", List.of("b1", "b2"));
			final var actual = Parameters.bulk("-beforeall <%var%> -afterall");
			ctpr.manageCollisionsParameters(actual);

			assertEquals("-beforeall <%var%> -afterall", actual.toString());
			assertEquals("b1 b2", ctpr.getParametersBeforeRef().toString());
		}

		@Test
		void noCollision_NoAddedParams() {
			final var ctpr = new ConversionToolParameterReference("ref", "<%var%>", List.of());
			final var actual = Parameters.bulk("-beforeall <%var%> -afterall");
			ctpr.manageCollisionsParameters(actual);

			assertEquals("-beforeall <%var%> -afterall", actual.toString());
			assertEquals("", ctpr.getParametersBeforeRef().toString());
		}

		@Test
		void noCollision_ShuffledParam_2() {
			final var ctpr = new ConversionToolParameterReference("ref", "<%var%>", List.of("s1", "s2"));
			final var actual = Parameters.bulk("-beforeall s2 s1 <%var%> -afterall");
			ctpr.manageCollisionsParameters(actual);

			assertEquals("-beforeall s2 s1 <%var%> -afterall", actual.toString());
			assertEquals("s2", ctpr.getParametersBeforeRef().toString());
		}

		@Test
		void noCollision_ShuffledParam_3() {
			final var ctpr = new ConversionToolParameterReference("ref", "<%var%>", List.of("s1", "s2", "s3"));
			final var actual = Parameters.bulk("-beforeall s3 s2 s1 <%var%> -afterall");
			ctpr.manageCollisionsParameters(actual);

			assertEquals("-beforeall s3 s2 s1 <%var%> -afterall", actual.toString());
			assertEquals("s2 s3", ctpr.getParametersBeforeRef().toString());
		}

		@Test
		void simpleEqualsCollision() {
			final var ctpr = new ConversionToolParameterReference("ref", "<%var%>", List.of("eq1", "eq2"));
			final var actual = Parameters.bulk("-beforeall eq1 eq2 <%var%> -afterall");
			ctpr.manageCollisionsParameters(actual);

			assertEquals("-beforeall eq1 eq2 <%var%> -afterall", actual.toString());
			assertEquals("", ctpr.getParametersBeforeRef().toString());
		}

		@Test
		void simpleCollisionMoreThan() {
			final var ctpr = new ConversionToolParameterReference("ref", "<%var%>", List.of("smT1", "smT2", "smT3"));
			final var actual = Parameters.bulk("-beforeall smT1 smT2 <%var%> -afterall");
			ctpr.manageCollisionsParameters(actual);

			assertEquals("-beforeall smT1 smT2 <%var%> -afterall", actual.toString());
			assertEquals("smT3", ctpr.getParametersBeforeRef().toString());
		}

		@Test
		void simpleCollisionLessThan() {
			final var ctpr = new ConversionToolParameterReference("ref", "<%var%>", List.of("b1"));
			final var actual = Parameters.bulk("-beforeall b1 b2 <%var%> -afterall");
			ctpr.manageCollisionsParameters(actual);

			assertEquals("-beforeall b1 b2 <%var%> -afterall", actual.toString());
			assertEquals("b1", ctpr.getParametersBeforeRef().toString());
		}

		@Test
		void paramValueCollision() {
			final var ctpr = new ConversionToolParameterReference("ref", "<%var%>", List.of("-b", "before"));
			final var actual = Parameters.bulk("-beforeall -b <%var%> -afterall");
			ctpr.manageCollisionsParameters(actual);

			assertEquals("-beforeall -b <%var%> -afterall", actual.toString());
			assertEquals("before", ctpr.getParametersBeforeRef().toString());
		}

		@Test
		void dangerousNotACollision() {
			final var ctpr = new ConversionToolParameterReference("ref", "<%var%>", List.of("-i"));
			final var actual = Parameters.bulk("-i letMe <%var%> -afterall");
			ctpr.manageCollisionsParameters(actual);

			assertEquals("-i letMe <%var%> -afterall", actual.toString());
			assertEquals("-i", ctpr.getParametersBeforeRef().toString());
		}

		@Test
		void dangerousCollision() {
			final var ctpr = new ConversionToolParameterReference("ref", "<%var%>", List.of("-i"));
			final var actual = Parameters.bulk("-i letMe -i <%var%> -afterall");
			ctpr.manageCollisionsParameters(actual);

			assertEquals("-i letMe -i <%var%> -afterall", actual.toString());
			assertEquals("", ctpr.getParametersBeforeRef().toString());
		}

	}

}
