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
 * Copyright (C) hdsdi3g for hd3g.tv 2018
 *
 */
package tv.hd3g.processlauncher.cmdline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SimpleParametersTest {

	private static final String TEST_CHAOTIC_LINE = "-aa 1  -single --cc 3 -U  \"dsfdsf sdf s  -e foo\" -g 2 42 -f=f -h=i;j,k:l -m Ah! -l \"u \" m ";

	@Nested
	class Simple {

		SimpleParameters pu;

		@Test
		void testParams() {
			pu = new SimpleParameters(TEST_CHAOTIC_LINE);

			assertEquals("-", pu.getParametersKeysStartsWith());
			assertFalse(pu.getParameters().isEmpty());

			final var actual = pu.toString();
			pu = new SimpleParameters();
			pu.addBulkParameters(TEST_CHAOTIC_LINE);
			assertEquals(actual, pu.toString());

			final var compare = Arrays.asList("-aa", "1", "-single", "--cc", "3", "-U", "dsfdsf sdf s  -e foo",
			        "-g", "2", "42", "-f=f", "-h=i;j,k:l", "-m", "Ah!", "-l", "u ", "m");

			assertTrue(Arrays.equals(compare.toArray(), pu.getParameters().toArray()));
			assertEquals(1, pu.getValues("-l").size());
			assertEquals("u ", pu.getValues("-l").get(0));

			assertEquals(1, pu.getValues("--cc").size());
			assertEquals("3", pu.getValues("--cc").get(0));

			assertEquals(0, pu.getValues("-single").size());
			assertEquals(0, pu.getValues("-h=i;j,k:l").size());
			assertNull(pu.getValues("-NOPE"));

			assertTrue(Arrays.equals(Arrays.asList("-a", "1", "-a", "2", "-a", "3").toArray(), new SimpleParameters(
			        "-a 1 -a 2 -a 3").getParameters().toArray()));

			assertTrue(Arrays.equals(Arrays.asList("1", "2", "3").toArray(), new SimpleParameters("-a 1 -a 2 -a 3")
			        .getValues("-a").toArray()));
			assertTrue(new SimpleParameters("-a 1 -a 2 -b -a 3").getValues("-b").isEmpty());
		}

		@Test
		void remove0() {
			pu = new SimpleParameters("-a 1 -c 4 -a 2 -a 3 -b");
			assertTrue(pu.removeParameter("-a", 0));
			assertTrue(Arrays.equals(Arrays.asList("-c", "4", "-a", "2", "-a", "3", "-b").toArray(), pu.getParameters()
			        .toArray()));
		}

		@Test
		void remove1() {
			pu = new SimpleParameters("-a 1 -c 4 -a 2 -a 3 -b");
			assertTrue(pu.removeParameter("-a", 1));
			assertTrue(Arrays.equals(Arrays.asList("-a", "1", "-c", "4", "-a", "3", "-b").toArray(), pu.getParameters()
			        .toArray()));
			assertFalse(pu.removeParameter("-a", 2));
			assertFalse(pu.removeParameter("-N", 0));
			assertTrue(Arrays.equals(Arrays.asList("-a", "1", "-c", "4", "-a", "3", "-b").toArray(), pu.getParameters()
			        .toArray()));
			assertTrue(pu.removeParameter("-a", 0));
			assertTrue(pu.removeParameter("-a", 0));
			assertTrue(Arrays.equals(Arrays.asList("-c", "4", "-b").toArray(), pu.getParameters().toArray()));
			assertTrue(pu.removeParameter("-b", 0));
			assertTrue(Arrays.equals(Arrays.asList("-c", "4").toArray(), pu.getParameters().toArray()));
		}

		@Test
		void remove2() {

			pu = new SimpleParameters("-a -b -c -d");
			assertTrue(pu.removeParameter("-a", 0));
			assertTrue(Arrays.equals(Arrays.asList("-b", "-c", "-d").toArray(), pu.getParameters().toArray()));
			assertTrue(pu.removeParameter("-c", 0));
			assertTrue(Arrays.equals(Arrays.asList("-b", "-d").toArray(), pu.getParameters().toArray()));
		}

		@Test
		void alter0() {

			pu = new SimpleParameters("-a 1 -c 4 -a 2 -a 3 -b");
			assertTrue(pu.alterParameter("-a", "Z2", 1));
			assertTrue(Arrays.equals(Arrays.asList("-a", "1", "-c", "4", "-a", "Z2", "-a", "3", "-b").toArray(), pu
			        .getParameters().toArray()));
			assertFalse(pu.alterParameter("-a", "Z2", 3));
			assertFalse(pu.alterParameter("-N", "Z2", 0));
			assertTrue(Arrays.equals(Arrays.asList("-a", "1", "-c", "4", "-a", "Z2", "-a", "3", "-b").toArray(), pu
			        .getParameters().toArray()));
		}

		@Test
		void alter1() {

			pu = new SimpleParameters("-a -b");
			assertTrue(pu.alterParameter("-a", "1", 0));
			assertTrue(Arrays.equals(Arrays.asList("-a", "1", "-b").toArray(), pu.getParameters().toArray()));
			assertTrue(pu.alterParameter("-b", "2", 0));
			assertTrue(Arrays.equals(Arrays.asList("-a", "1", "-b", "2").toArray(), pu.getParameters().toArray()));
		}

		@Test
		void alter2() {
			pu = new SimpleParameters("-a -a -a");
			assertTrue(pu.alterParameter("-a", "1", 0));
			assertTrue(pu.alterParameter("-a", "2", 1));
			assertTrue(pu.alterParameter("-a", "3", 2));
			assertTrue(Arrays.equals(Arrays.asList("-a", "1", "-a", "2", "-a", "3").toArray(), pu.getParameters()
			        .toArray()));

			pu.clear();
			assertTrue(pu.getParameters().isEmpty());

			pu.addParameters("a", "b", null, "c");
			pu.addParameters("d");
			assertEquals("abcd", pu.getParameters().stream().collect(Collectors.joining()));

			pu.clear().addParameters(Arrays.asList("a", "b", null, "c"));
			assertEquals("abc", pu.getParameters().stream().collect(Collectors.joining()));

			pu.clear().addParameters("a", "b", "c", "d", null, "e");
			assertEquals("abcde", pu.getParameters().stream().collect(Collectors.joining()));

			pu.clear().addParameters("a b  c d", "e", null, "f");
			assertEquals(3, pu.getParameters().size());
			assertEquals("a b  c def", pu.getParameters().stream().collect(Collectors.joining()));

			pu.addParameters("ggg h i ", " e", null, "fff", " ");
			assertEquals(3 + 4, pu.getParameters().size());
			assertEquals("a b  c defggg h i  efff ", pu.getParameters().stream().collect(Collectors.joining()));
		}
	}

	@Test
	void testParamStyleChange() {
		final var pu = new SimpleParameters("-a 1 /b 2").setParametersKeysStartsWith("/");
		assertEquals("/", pu.getParametersKeysStartsWith());

		assertTrue(Arrays.equals(Arrays.asList("-a", "1", "/b", "2").toArray(), pu.getParameters().toArray()));
		assertNull(pu.getValues("-a"));
		assertNotNull(pu.getValues("/b"));
		assertEquals(1, pu.getValues("/b").size());
		assertEquals("2", pu.getValues("/b").get(0));
		assertTrue(pu.alterParameter("/b", "Z", 0));
		assertEquals("Z", pu.getValues("/b").get(0));

	}

	@Test
	void testTransfert() {
		final var pu1 = new SimpleParameters("!ok1").setParametersKeysStartsWith("!");
		final var pu2 = new SimpleParameters("-ok2");

		pu1.transfertThisConfigurationTo(pu2);
		assertEquals("!", pu2.getParametersKeysStartsWith());
		assertNotEquals(pu1.toString(), pu2.toString());

		pu1.setParametersKeysStartsWith("$");
		assertEquals("!", pu2.getParametersKeysStartsWith());

		pu2.importParametersFrom(pu1);
		assertEquals(pu1.toString(), pu2.toString());
		assertEquals("$", pu2.getParametersKeysStartsWith());
	}

	@Test
	void testPrepend() {
		final var pu = new SimpleParameters("-3 -4");
		pu.prependBulkParameters("-1 -2");

		assertEquals(4, pu.getParameters().size());
		assertEquals("-1 -2 -3 -4", pu.toString());

		pu.clear();
		pu.prependParameters("-3", "-4");
		pu.prependParameters("-1", "-2");
		assertEquals(4, pu.getParameters().size());
		assertEquals("-1 -2 -3 -4", pu.toString());

		pu.clear();
		pu.prependParameters(Arrays.asList("-3", "-4"));
		pu.prependParameters(Arrays.asList("-1", "-2"));
		assertEquals(4, pu.getParameters().size());
		assertEquals("-1 -2 -3 -4", pu.toString());
	}

	@Test
	void testHasParameters() {
		final var pu = new SimpleParameters("-a -b");
		assertTrue(pu.hasParameters("-a", "-b", "-z"));
		assertTrue(pu.hasParameters("-a"));
		assertTrue(pu.hasParameters("a"));
		assertFalse(pu.hasParameters("-z"));
		assertTrue(pu.hasParameters("-a", "-b"));
	}

	@Test
	void testIfHasNotParameter() {
		final var pu = new SimpleParameters("-a -b");
		final var count = new AtomicInteger(0);
		pu.ifHasNotParameter(() -> {
			count.getAndIncrement();
		}, "-z");
		assertEquals(1, count.get());
	}

	@Test
	void count() {
		assertEquals(0, new SimpleParameters("").count());
		assertEquals(1, new SimpleParameters("-a").count());
		assertEquals(2, new SimpleParameters("-a b").count());
	}

	@Test
	void isEmpty() {
		assertTrue(new SimpleParameters("").isEmpty());
		assertFalse(new SimpleParameters("-a").isEmpty());
		assertFalse(new SimpleParameters("b").isEmpty());
	}

	@Test
	void getAllArgKeyValues() {
		final var result = new SimpleParameters("0 -a 1 -b 2 0 -c 3 -c 4 0 -d -e")
		        .getAllArgKeyValues();

		assertNotNull(result);
		assertEquals(5, result.size());
		assertEquals(List.of("1"), result.get("-a"));
		assertEquals(List.of("2"), result.get("-b"));
		assertEquals(List.of("3", "4"), result.get("-c"));
		assertEquals(List.of(), result.get("-d"));
		assertEquals(List.of(), result.get("-e"));
	}

	@Nested
	class CompareAndAlter {

		@Test
		void base() {
			final var params = "-spcfiActl -cmnSimple -cmnArg 0 -cmnArgs 1 -cmnArgs 2 -cmnArgs 3 -spcfiActlArg 4 -spcfiActlArgs 5p 6";
			final var compareParams = "-specificCompare -cmnSimple -cmnArg 7 -cmnArgs 8 -cmnArgs 9 -cmnArgs 10 -spcfiCompareArg 11 -spcfiCompareArgs 12p 13";
			final var toCompare = new SimpleParameters(compareParams);

			final var p = new SimpleParameters(params);
			p.compareAndAlter(toCompare, (ak, a, c) -> List.of("é", "è"), false, false);
			assertEquals(
			        "-spcfiActl -cmnSimple é -cmnSimple è -cmnArg é -cmnArg è -cmnArgs é -cmnArgs è -spcfiActlArg 4 -spcfiActlArgs 5p 6",
			        p.toString());

			assertEquals(compareParams, toCompare.toString());
		}

		@Test
		void emptyChooser() {
			final var params = "-specificActual 0 -common 1 -common 11";
			final var compareParams = "-specificCompared 2 -common 3 -common 33";
			final var toCompare = new SimpleParameters(compareParams);

			final var p = new SimpleParameters(params);
			p.compareAndAlter(toCompare, (ak, a, c) -> List.of(), false, false);
			assertEquals("-specificActual 0 -common", p.toString());
			assertEquals(compareParams, toCompare.toString());
		}

		@Test
		void nullChooser() {
			final var params = "-specificActual 0 -common 1 -common 11";
			final var compareParams = "-specificCompared 2 -common 3 -common 33";
			final var toCompare = new SimpleParameters(compareParams);

			final var p = new SimpleParameters(params);
			p.compareAndAlter(toCompare, (ak, a, c) -> null, false, false);
			assertEquals("-specificActual 0", p.toString());
			assertEquals(compareParams, toCompare.toString());
		}

		@Test
		void removeActualMissing() {
			final var params = "-specificActual 0 -common 1 -common 11";
			final var compareParams = "-specificCompared 2 -common 3 -common 33";
			final var toCompare = new SimpleParameters(compareParams);

			final var p = new SimpleParameters(params);
			p.compareAndAlter(toCompare, (argKey, actualValues, comparedValues) -> actualValues, true, false);
			assertEquals("-common 1 -common 11", p.toString());
			assertEquals(compareParams, toCompare.toString());
		}

		@Test
		void addComparedMissing() {
			final var params = "-specificActual 0 -common 1 -common 11";
			final var compareParams = "-specificCompared 2 -common 3 -common 33 -specificCompared 4";
			final var toCompare = new SimpleParameters(compareParams);

			final var p = new SimpleParameters(params);
			p.compareAndAlter(toCompare, (argKey, actualValues, comparedValues) -> actualValues, false, true);
			assertEquals("-specificActual 0 -common 1 -common 11 -specificCompared 2 -specificCompared 4", p
			        .toString());
			assertEquals(compareParams, toCompare.toString());
		}
	}

	@Test
	void testExportToExternalCommandLine() {
		final var p0 = new SimpleParameters("-a b -c d");
		assertEquals(4, p0.count());
		assertEquals("exec -a b -c d", p0.exportToExternalCommandLine("exec"));

		final var p1 = new SimpleParameters("-a [b] -c d");
		assertEquals(4, p1.count());
		assertEquals("exec -a \"[b]\" -c d", p1.exportToExternalCommandLine("exec"));

		final var p2 = new SimpleParameters("-a [b] -c $d");
		assertEquals(4, p2.count());
		assertEquals("exec -a \"[b]\" -c \\$d", p2.exportToExternalCommandLine("exec"));

		final var p3 = new SimpleParameters(List.of("a", "b \\ c", "d"));
		assertEquals(3, p3.count());
		assertEquals("exec a \"b \\\\ c\" d", p3.exportToExternalCommandLine("exec"));
	}

}
