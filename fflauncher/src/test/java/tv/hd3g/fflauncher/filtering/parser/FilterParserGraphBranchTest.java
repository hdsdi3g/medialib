package tv.hd3g.fflauncher.filtering.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;

class FilterParserGraphBranchTest {

	@Test
	void testGetRawChains_2items() {
		final var rfg = new FilterParserGraphBranch(List.of(
		        new FilterParserChars("a"),
		        new FilterParserChars("b"),
		        new FilterParserChars(","),
		        new FilterParserChars("c"),
		        new FilterParserChars("d")));

		final var rc = rfg.getRawChains();
		assertNotNull(rc);
		assertEquals(2, rc.size());
		assertEquals("ab", rc.get(0).toString());
		assertEquals("cd", rc.get(1).toString());
	}

	@Test
	void testGetRawChains_1item() {
		final var rfg = new FilterParserGraphBranch(List.of(
		        new FilterParserChars("a"),
		        new FilterParserChars("b")));

		final var rc = rfg.getRawChains();
		assertNotNull(rc);
		assertEquals(1, rc.size());
		assertEquals("ab", rc.get(0).toString());
	}

	@Test
	void testGetRawChains_3items() {
		final var rfg = new FilterParserGraphBranch(List.of(
		        new FilterParserChars("a"),
		        new FilterParserChars("b"),
		        new FilterParserChars(","),
		        new FilterParserChars("c"),
		        new FilterParserChars("d"),
		        new FilterParserChars(","),
		        new FilterParserChars("e"),
		        new FilterParserChars("f")));

		final var rc = rfg.getRawChains();
		assertNotNull(rc);
		assertEquals(3, rc.size());
		assertEquals("ab", rc.get(0).toString());
		assertEquals("cd", rc.get(1).toString());
		assertEquals("ef", rc.get(2).toString());
	}

	@Test
	void testToString() {
		final var rfg = new FilterParserGraphBranch(List.of(
		        new FilterParserChars("a"),
		        new FilterParserChars("b"),
		        new FilterParserChars(","),
		        new FilterParserChars("c"),
		        new FilterParserChars("d")));
		assertEquals("ab,cd", rfg.toString());
	}
}
