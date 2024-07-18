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
 * Copyright (C) hdsdi3g for hd3g.tv 2018
 *
 */
package tv.hd3g.fflauncher;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tv.hd3g.fflauncher.ConversionTool.APPEND_PARAM_AT_END;
import static tv.hd3g.fflauncher.ConversionTool.PREPEND_PARAM_AT_START;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import tv.hd3g.fflauncher.enums.OutputFilePresencePolicy;
import tv.hd3g.processlauncher.cmdline.Parameters;

class ConversionToolTest {

	@Test
	void test() throws IOException {
		final var ct = new ConversionTool("java");
		ct.getInternalParameters().addParameters("-firstparam");

		assertFalse(ct.isRemoveParamsIfNoVarToInject());
		ct.setRemoveParamsIfNoVarToInject(true);
		assertTrue(ct.isRemoveParamsIfNoVarToInject());
		ct.setRemoveParamsIfNoVarToInject(false);
		assertFalse(ct.isRemoveParamsIfNoVarToInject());

		assertNotNull(ct.getExecutableName());

		final var p = ct.getInternalParameters();
		assertNotNull(p);
		assertNotNull(ct.getReadyToRunParameters());

		assertEquals("-firstparam", p.getParameters().stream().findFirst().get());

		p.addParameters("<%varsource1%>");
		ct.addInputSource("source1", "varsource1", "-pre1-source1", "-pre2-source1");

		assertEquals(2, p.getParameters().size());

		p.addParameters("<%varsource2%>");
		ct.addInputSource("source2", "varsource2", Arrays.asList("-pre1-source2", "-pre2-source2"));

		p.addParameters("<%vardest1%>");
		ct.addOutputDestination("dest1", "vardest1", "-pre1-dest1", "-pre2-dest1");

		p.addParameters("<%vardest2%>");
		ct.addOutputDestination("dest2", "vardest2", Arrays.asList("-pre1-dest2", "-pre2-dest2"));

		assertEquals(5, p.getParameters().size());

		assertEquals("source1 source2", ct.getDeclaredSources().stream().collect(Collectors.joining(" ")));

		ct.addSimpleOutputDestination("dest-simple");
		assertEquals("dest1 dest2 dest-simple", ct.getDeclaredDestinations().stream().collect(joining(" ")));

		final var processed_cmdline = ct.getReadyToRunParameters().getParameters().stream().collect(joining(" "));

		final var expected = "-firstparam -pre1-source1 -pre2-source1 source1 -pre1-source2 -pre2-source2 source2 -pre1-dest1 -pre2-dest1 dest1 -pre1-dest2 -pre2-dest2 dest2 dest-simple";
		assertEquals(expected, processed_cmdline);

		assertEquals("source1", ct.getDeclaredSourceByVarName("<%varsource1%>").orElse("nope"));
		assertEquals("dest2", ct.getDeclaredDestinationByVarName("<%vardest2%>").orElse("nope"));
	}

	@Test
	void testCatchMissingOutVar() {

		class CT extends ConversionTool {

			CT() {
				super("java");
			}

			@Override
			protected void onMissingInputOutputVar(final String var_name, final String ressource) {
				throw new IllegalCallerException();
			}
		}

		final var ct = new CT();
		final var p = ct.getInternalParameters();
		p.addParameters("-1", "<%not_found_var%>", "-2", "<%found_var%>");
		ct.addInputSource("source", "found_var");
		assertEquals("-1 -2 source", ct.getReadyToRunParameters().toString());

		ct.addSimpleOutputDestination("dest-simple");
		assertEquals("-1 -2 source dest-simple", ct.getReadyToRunParameters().toString());

		ct.setRemoveParamsIfNoVarToInject(true);
		assertEquals("-2 source dest-simple", ct.getReadyToRunParameters().toString());

		p.clear();
		assertEquals("source dest-simple", ct.getReadyToRunParameters().toString());
	}

	@Test
	void testManageOutFiles() throws IOException {
		final var f1 = File.createTempFile("test", ".txt");
		final var d1 = new File(f1.getParent() + File.separator + "sub1-" + f1.getName() + File.separator + "sub2");
		assertTrue(d1.mkdirs());
		final var f2 = File.createTempFile("test", ".txt", d1.getParentFile());
		final var f3 = File.createTempFile("test", ".txt", d1);

		assertTrue(f1.exists());
		assertTrue(f2.exists());
		assertTrue(f3.exists());
		assertTrue(d1.exists());
		assertTrue(d1.listFiles().length > 0);
		assertTrue(d1.getParentFile().exists());

		final var ct = new ConversionTool("java");
		ct.getInternalParameters().addParameters("-firstparam");
		ct.addSimpleOutputDestination("nothing");
		ct.addSimpleOutputDestination(f1.getAbsolutePath());
		ct.addSimpleOutputDestination(f2.toURI().toURL().toString());
		ct.addSimpleOutputDestination(f3.getAbsolutePath());
		ct.addSimpleOutputDestination(d1.getAbsolutePath());
		ct.addSimpleOutputDestination("http://not.this/");

		var founded = ct.getOutputFiles(OutputFilePresencePolicy.ALL, null);

		assertEquals(5, founded.size());
		assertEquals("nothing", founded.get(0).getPath());
		assertEquals(f1, founded.get(1));
		assertEquals(f2, founded.get(2));
		assertEquals(f3, founded.get(3));
		assertEquals(d1, founded.get(4));

		founded = ct.getOutputFiles(OutputFilePresencePolicy.MUST_EXISTS, null);
		assertEquals(4, founded.size());
		assertEquals(f1, founded.get(0));
		assertEquals(f2, founded.get(1));
		assertEquals(f3, founded.get(2));
		assertEquals(d1, founded.get(3));

		founded = ct.getOutputFiles(OutputFilePresencePolicy.MUST_BE_A_REGULAR_FILE, null);
		assertEquals(3, founded.size());
		assertEquals(f1, founded.get(0));
		assertEquals(f2, founded.get(1));
		assertEquals(f3, founded.get(2));

		founded = ct.getOutputFiles(OutputFilePresencePolicy.NOT_EMPTY, null);
		assertEquals(1, founded.size());
		assertEquals(d1, founded.get(0));

		ct.cleanUpOutputFiles(false, true, null);

		assertFalse(f1.exists());
		assertFalse(f2.exists());
		assertFalse(f3.exists());
		// Flacky on windows
		/*assertFalse(d1.exists());
		assertTrue(d1.getParentFile().exists());
		assertTrue(d1.getParentFile().delete());*/
	}

	@Nested
	class FixIOParametredVars {

		@Test
		void noIORef_noCollision() {
			final var ct = new ConversionTool("java");
			final var firstParams = "-pre -before <%myvar%> -after -post value";
			ct.parameters.addBulkParameters(firstParams);
			ct.addInputSource("src0", "in0", List.of("-bef00", "-bef01"));
			ct.addInputSource("src1", "in1", List.of("-bef10", "-bef11"));
			ct.addOutputDestination("dst0", "out0", List.of("-bef20", "-bef21"));
			ct.addOutputDestination("dst1", "out1", List.of("-bef30", "-bef31"));

			ct.setFixIOParametredVars(
					(p, k) -> p.addParameters("<i", k, "i>"),
					(p, k) -> p.addParameters("<o", k, "o>"));
			assertEquals(firstParams, ct.parameters.toString());
			assertEquals(
					"-pre -before -after -post value <i -bef00 -bef01 src0 i> <i -bef10 -bef11 src1 i> <o -bef20 -bef21 dst0 o> <o -bef30 -bef31 dst1 o>",
					ct.getReadyToRunParameters().toString());
		}

		@Test
		void ioRef_noCollision() {
			final var ct = new ConversionTool("java");
			ct.parameters.addBulkParameters("-pre -i <%in%> -o <%out%> -post");
			ct.addInputSource("src", "in", "-i");
			ct.addOutputDestination("dst", "out", "-o");

			ct.setFixIOParametredVars(neverTriggMe, neverTriggMe);
			assertEquals("-pre -i <%in%> -o <%out%> -post", ct.parameters.toString());
			assertEquals("-pre -i src -o dst -post", ct.getReadyToRunParameters().toString());
		}

		@Test
		void ioRef_Collision() {
			final var ct = new ConversionTool("java");
			ct.parameters.addBulkParameters("-pre -i <%in%> -o <%out%> -post");
			ct.addInputSource("src", "in", "-i", "beforeI");
			ct.addOutputDestination("dst", "out", "-o", "beforeO");

			ct.setFixIOParametredVars(neverTriggMe, neverTriggMe);
			assertEquals("-pre -i <%in%> -o <%out%> -post", ct.parameters.toString());
			assertEquals("-pre -i beforeI src -o beforeO dst -post", ct.getReadyToRunParameters().toString());
		}

		@Test
		void ioRef_MultipleNotCollisions() {
			final var ct = new ConversionTool("java");
			ct.parameters.addBulkParameters("-pre -i keepme <%in%> -o <%out%> -post");
			ct.addInputSource("src", "in", "-i", "beforeI");
			ct.addOutputDestination("dst", "out", "-o", "beforeO");

			ct.setFixIOParametredVars(neverTriggMe, neverTriggMe);
			assertEquals("-pre -i keepme <%in%> -o <%out%> -post", ct.parameters.toString());
			assertEquals("-pre -i keepme -i beforeI src -o beforeO dst -post", ct.getReadyToRunParameters().toString());
		}

		@Test
		void defaultIoRef_noCollision() {
			final var ct = new ConversionTool("java");
			ct.parameters.addBulkParameters("-pre -i <%in%> -o <%out%> -post");
			ct.addInputSource("src", "in", "-i");
			ct.addOutputDestination("dst", "out", "-o");

			ct.setFixIOParametredVars(PREPEND_PARAM_AT_START, APPEND_PARAM_AT_END);
			assertEquals("-pre -i <%in%> -o <%out%> -post", ct.parameters.toString());
			assertEquals("-pre -i src -o dst -post", ct.getReadyToRunParameters().toString());
		}

	}

	private static final BiConsumer<Parameters, String> neverTriggMe = (p, k) -> {
		throw new IllegalStateException("You never should manage here missing keys: " + k);
	};

	@Test
	void testSpacesInInputOutputFileNames() {
		final var parameters = new Parameters();
		IntStream.range(0, 4).forEach(i -> parameters.addParameters("<%IN" + i + "%>"));
		IntStream.range(4, 8).forEach(i -> parameters.addParameters("<%OUT" + i + "%>"));

		final var ct = new ConversionTool("java", parameters);

		final var files = IntStream.range(0, 10)
				.mapToObj(i -> {
					try {
						return File.createTempFile("temp FF name [" + i + "]", ".ext");
					} catch (final IOException e) {
						throw new UncheckedIOException(e);
					}
				})
				.collect(toUnmodifiableList());

		ct.addInputSource(files.get(0), "IN0");
		ct.addInputSource(files.get(1), "IN1", List.of());
		ct.addInputSource(files.get(2).getPath(), "IN2");
		ct.addInputSource(files.get(3).getPath(), "IN3", List.of());
		ct.addOutputDestination(files.get(4), "OUT4");
		ct.addOutputDestination(files.get(5), "OUT5", List.of());
		ct.addOutputDestination(files.get(6).getPath(), "OUT6");
		ct.addOutputDestination(files.get(7).getPath(), "OUT7", List.of());
		ct.addSimpleOutputDestination(files.get(8));
		ct.addSimpleOutputDestination(files.get(9).getPath());
		ct.setFixIOParametredVars(PREPEND_PARAM_AT_START, APPEND_PARAM_AT_END);

		final var p = ct.getReadyToRunParameters();
		final var filesNames = files.stream().map(File::getPath).collect(Collectors.toUnmodifiableList());

		final var params = new ArrayList<>();
		for (var pos = 0; pos < 10; pos++) {
			params.add(filesNames.get(pos));
		}

		assertEquals(params, p.getParameters());
	}
}
