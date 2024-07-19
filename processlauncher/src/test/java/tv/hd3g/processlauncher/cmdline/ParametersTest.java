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
package tv.hd3g.processlauncher.cmdline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;

class ParametersTest {

	@Test
	void test() {
		var clp = new Parameters();

		assertEquals("<%", clp.getStartVarTag());
		assertEquals("%>", clp.getEndVarTag());
		assertTrue(clp.isTaggedParameter("<%ok%>"));
		assertFalse(clp.isTaggedParameter("<%nope"));
		assertFalse(clp.isTaggedParameter("nope%>"));
		assertFalse(clp.isTaggedParameter("<nope>"));
		assertFalse(clp.isTaggedParameter("nope"));
		assertFalse(clp.isTaggedParameter("%>nope<%"));
		assertFalse(clp.isTaggedParameter("<%nope %>"));
		assertEquals("my_var", clp.extractVarNameFromTaggedParameter("<%my_var%>"));

		clp = new Parameters().setVarTags("{", "}");
		assertEquals("{", clp.getStartVarTag());
		assertEquals("}", clp.getEndVarTag());
		assertTrue(clp.isTaggedParameter("{ok}"));
		assertFalse(clp.isTaggedParameter("{ok }"));
		assertFalse(clp.isTaggedParameter("{nope"));
		assertFalse(clp.isTaggedParameter("nope}"));
		assertFalse(clp.isTaggedParameter("nope"));
		assertFalse(clp.isTaggedParameter("}nope{"));
		assertEquals("my_var", clp.extractVarNameFromTaggedParameter("{my_var}"));

		clp = new Parameters();
		assertNull(clp.extractVarNameFromTaggedParameter("<%%>"));
		assertNull(clp.extractVarNameFromTaggedParameter("<%"));
		assertNull(clp.extractVarNameFromTaggedParameter("%>"));
		assertNull(clp.extractVarNameFromTaggedParameter("nope"));
	}

	@Test
	void testInjectVarKeepEmptyParam() {
		final var p = Parameters.bulk("-a <%var1%> <%var2%> <%varNOPE%> -b <%varNOPE%> -c");
		final var vars = new HashMap<String, Parameters>();
		vars.put("<%var1%>", Parameters.bulk("value1 value2"));
		vars.put("<%var2%>", Parameters.bulk("value3"));
		vars.put("<%ignored%>", Parameters.bulk("value4"));
		p.injectVariables(vars, false);

		assertEquals("-a value1 value2 value3 -b -c", p.toString());
	}

	@Test
	void testRemoveVarsKeepEmptyParam() {
		final var p = Parameters.bulk("-a <%var1%> <%var2%> <%varNOPE%> -b <%varNOPE%> -c");
		p.removeVariables(false);

		assertEquals("-a -b -c", p.toString());
	}

	@Test
	void testInjectVarRemoveEmptyParam() {
		final var p = Parameters.bulk("-a <%var1%> <%var2%> <%varNOPE%> -b <%varNOPE%> -c");
		final var vars = new HashMap<String, Parameters>();
		vars.put("<%var1%>", Parameters.bulk("value1 value2"));
		vars.put("<%var2%>", Parameters.bulk("value3"));
		vars.put("<%ignored%>", Parameters.bulk("value4"));
		p.injectVariables(vars, true);

		assertEquals("-a value1 value2 value3 -c", p.toString());
	}

	@Test
	void testRemoveVarsRemoveEmptyParam() {
		final var p = Parameters.bulk("-a <%var1%> <%var2%> <%varNOPE%> -b <%varNOPE%> -c");
		p.removeVariables(true);
		assertEquals("-c", p.toString());
	}

	@Test
	void testInjectParamsAroundVariable() {
		var p = Parameters.bulk("-before <%myvar%> -after");

		p.injectParamsAroundVariable("myvar", Arrays.asList("-addedbefore", "1"), Arrays.asList("-addedafter", "2"));
		assertEquals("-before -addedbefore 1 <%myvar%> -addedafter 2 -after", p.toString());

		p = Parameters.bulk("-before <%myvar%> <%myvar%> -after");
		p.injectParamsAroundVariable("myvar", Arrays.asList("-addedbefore", "1"), Arrays.asList("-addedafter", "2"));
		assertEquals(
				"-before -addedbefore 1 <%myvar%> -addedafter 2 -addedbefore 1 <%myvar%> -addedafter 2 -after", p
						.toString());

		p = Parameters.bulk("-before <%myvar1%> <%myvar2%> -after");
		p.injectParamsAroundVariable("myvar1", Arrays.asList("-addedbefore", "1"), Arrays.asList("-addedafter", "2"));
		p.injectParamsAroundVariable("myvar2", Arrays.asList("-addedbefore", "3"), Arrays.asList("-addedafter", "4"));
		assertEquals(
				"-before -addedbefore 1 <%myvar1%> -addedafter 2 -addedbefore 3 <%myvar2%> -addedafter 4 -after", p
						.toString());
	}

	@Test
	void transfertThisConfigurationTo() {
		final var pu1 = Parameters.bulk("!ok1");
		pu1.setParametersKeysStartsWith("!");
		pu1.setVarTags("{", "}");

		final var pu2 = Parameters.bulk("-ok2");
		pu1.transfertThisConfigurationTo(pu2);

		assertEquals("!", pu2.getParametersKeysStartsWith());
		assertEquals("{", pu2.getStartVarTag());
		assertEquals("}", pu2.getEndVarTag());
		assertNotEquals(pu1.toString(), pu2.toString());
	}

	@Test
	void tagVar() {
		final var pu1 = Parameters.bulk("ok");
		final var tags = pu1.tagVar("myvar");
		assertEquals("<%myvar%>", tags);

		pu1.setVarTags("<", ">");
		final var tags2 = pu1.tagVar("myvar");
		assertEquals("<myvar>", tags2);
	}

	@Test
	void testOfArray() {
		assertEquals(0, Parameters.of().count());
		assertEquals(List.of("a", "b c"),
				Parameters.of("a", "", "b c").getParameters());
	}

	@Test
	void testOfCollection() {
		assertEquals(0, Parameters.of(List.of()).count());
		assertEquals(List.of("a", "b c"),
				Parameters.of(List.of("a", "", "b c")).getParameters());
	}

	@Test
	void testBulkArray() {
		assertEquals(0, Parameters.bulk().count());
		assertEquals(List.of("a", "b", "c", "d e"),
				Parameters.bulk("a", "", "b c", "\"d e\"").getParameters());
	}

	@Test
	void testBulkCollection() {
		assertEquals(0, Parameters.bulk(List.of()).count());
		assertEquals(List.of("a", "b", "c", "d e"),
				Parameters.bulk(List.of("a", "", "b c", "\"d e\"")).getParameters());
	}

}
