package tv.hd3g.fflauncher.filtering;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static tv.hd3g.fflauncher.enums.ChannelLayout.CH3_0_BACK;
import static tv.hd3g.fflauncher.enums.ChannelLayout.CH5_0_SIDE;
import static tv.hd3g.fflauncher.enums.ChannelLayout.DOWNMIX;
import static tv.hd3g.fflauncher.enums.ChannelLayout.MONO;
import static tv.hd3g.fflauncher.enums.ChannelLayout.QUAD;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FilterArgumentTest {

	String key;
	String value;

	FilterArgument filterArgument;

	@BeforeEach
	void init() {
		key = String.valueOf(System.nanoTime());
		value = String.valueOf(System.nanoTime());
		filterArgument = new FilterArgument(key, value);
	}

	@Test
	void testFilterArgument() {
		filterArgument = new FilterArgument(key);
		assertEquals(key, filterArgument.getKey());
		assertNull(filterArgument.getValue());
	}

	@Test
	void testFilterArgument_number() {
		filterArgument = new FilterArgument(key, 42);
		assertEquals(key, filterArgument.getKey());
		assertEquals(String.valueOf(42), filterArgument.getValue());
	}

	@Test
	void testFilterArgument_enum() {
		filterArgument = new FilterArgument(key, CH3_0_BACK);
		assertEquals(key, filterArgument.getKey());
		assertEquals(CH3_0_BACK.toString(), filterArgument.getValue());
	}

	@Test
	void testFilterArgument_collection_single() {
		final var list = List.of(MONO);
		filterArgument = new FilterArgument(key, list, "*");
		assertEquals(key, filterArgument.getKey());
		assertEquals(MONO.toString(), filterArgument.getValue());
	}

	@Test
	void testFilterArgument_collection() {
		final var list = List.of(MONO, QUAD);
		filterArgument = new FilterArgument(key, list, "*");
		assertEquals(key, filterArgument.getKey());
		assertEquals(MONO.toString() + "*" + QUAD.toString(), filterArgument.getValue());
	}

	@Test
	void testFilterArgument_stream() {
		final var list = List.of(CH5_0_SIDE, DOWNMIX);
		filterArgument = new FilterArgument(key, list.stream(), "~");
		assertEquals(key, filterArgument.getKey());
		assertEquals(CH5_0_SIDE.toString() + "~" + DOWNMIX.toString(), filterArgument.getValue());
	}

	@Test
	void testGetKey() {
		assertEquals(key, filterArgument.getKey());
	}

	@Test
	void testGetValue() {
		assertEquals(value, filterArgument.getValue());
	}

	@Test
	void testSetValue() {
		value = String.valueOf(System.nanoTime());
		filterArgument.setValue(value);
		assertEquals(value, filterArgument.getValue());
	}

	@Test
	void testToString() {
		assertEquals(key + "=" + value, filterArgument.toString());
		assertEquals(key, new FilterArgument(key).toString());
	}

	@Test
	void testHashCode() {
		assertEquals(filterArgument.hashCode(), new FilterArgument(key, value).hashCode());
	}

	@Test
	void testEqualsObject() {
		assertEquals(filterArgument, new FilterArgument(key, value));
	}
}
