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
 * Copyright (C) hdsdi3g for hd3g.tv 2023
 *
 */
package tv.hd3g.fflauncher.ffprobecontainer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.xml.sax.Attributes;

import net.datafaker.Faker;

class SAXAttributeParserTraitsTest {
	static Faker faker = net.datafaker.Faker.instance();

	static class SAXAttributeParserTraitsImpl implements SAXAttributeParserTraits {
	}

	@Mock
	Attributes attributes;

	SAXAttributeParserTraits s;
	String keyName;
	String sValue;
	int iValue;
	float fValue;
	long lValue;

	@BeforeEach
	void init() throws Exception {
		openMocks(this).close();
		s = new SAXAttributeParserTraitsImpl();
		keyName = faker.numerify("keyName###");
		sValue = faker.numerify("sValue###");
		iValue = faker.random().nextInt();
		fValue = faker.random().nextFloat();
		lValue = faker.random().nextLong();
	}

	@AfterEach
	void end() {
		verifyNoMoreInteractions(attributes);
	}

	@Test
	void testGetAttrValueAttributesString() {
		when(attributes.getValue(keyName)).thenReturn(sValue);
		assertEquals(Optional.ofNullable(sValue), s.getAttrValue(attributes, keyName));
		verify(attributes, times(1)).getValue(keyName);
	}

	@Test
	void testGetAttrValueAttributesStringString() {
		final var orDefault = faker.numerify("or###");
		assertEquals(orDefault, s.getAttrValue(attributes, keyName, orDefault));

		when(attributes.getValue(keyName)).thenReturn(sValue);
		assertEquals(sValue, s.getAttrValue(attributes, keyName, orDefault));

		verify(attributes, times(2)).getValue(keyName);
	}

	@Test
	void testGetAttrBooleanValueAttributesString() {
		assertFalse(s.getAttrBooleanValue(attributes, keyName).isPresent());

		when(attributes.getValue(keyName)).thenReturn("1");
		assertTrue(s.getAttrBooleanValue(attributes, keyName).get());

		when(attributes.getValue(keyName)).thenReturn("nope");
		assertFalse(s.getAttrBooleanValue(attributes, keyName).get());

		verify(attributes, times(3)).getValue(keyName);
	}

	@Test
	void testGetAttrBooleanValueAttributesStringBoolean() {
		assertTrue(s.getAttrBooleanValue(attributes, keyName, true));
		assertFalse(s.getAttrBooleanValue(attributes, keyName, false));

		when(attributes.getValue(keyName)).thenReturn("1");
		assertTrue(s.getAttrBooleanValue(attributes, keyName, false));

		when(attributes.getValue(keyName)).thenReturn("nope");
		assertFalse(s.getAttrBooleanValue(attributes, keyName, true));

		verify(attributes, times(4)).getValue(keyName);
	}

	@Test
	void testGetAttrIntValueAttributesString() {
		assertFalse(s.getAttrIntValue(attributes, keyName).isPresent());

		when(attributes.getValue(keyName)).thenReturn(String.valueOf(iValue));
		assertEquals(Optional.ofNullable(iValue), s.getAttrIntValue(attributes, keyName));

		when(attributes.getValue(keyName)).thenReturn("nope");
		assertFalse(s.getAttrIntValue(attributes, keyName).isPresent());

		verify(attributes, times(3)).getValue(keyName);
	}

	@Test
	void testGetAttrIntValueAttributesStringInt() {
		final var orDefault = faker.random().nextInt();

		assertEquals(orDefault, s.getAttrIntValue(attributes, keyName, orDefault));

		when(attributes.getValue(keyName)).thenReturn(String.valueOf(iValue));
		assertEquals(iValue, s.getAttrIntValue(attributes, keyName, orDefault));

		verify(attributes, times(2)).getValue(keyName);
	}

	@Test
	void testGetAttrLongValueAttributesString() {
		assertFalse(s.getAttrLongValue(attributes, keyName).isPresent());

		when(attributes.getValue(keyName)).thenReturn(String.valueOf(lValue));
		assertEquals(Optional.ofNullable(lValue), s.getAttrLongValue(attributes, keyName));

		when(attributes.getValue(keyName)).thenReturn("nope");
		assertFalse(s.getAttrLongValue(attributes, keyName).isPresent());

		verify(attributes, times(3)).getValue(keyName);
	}

	@Test
	void testGetAttrLongValueAttributesStringLong() {
		final var orDefault = faker.random().nextLong();

		assertEquals(orDefault, s.getAttrLongValue(attributes, keyName, orDefault));

		when(attributes.getValue(keyName)).thenReturn(String.valueOf(lValue));
		assertEquals(lValue, s.getAttrLongValue(attributes, keyName, orDefault));

		verify(attributes, times(2)).getValue(keyName);
	}

	@Test
	void testGetAttrFloatValueAttributesString() {
		assertFalse(s.getAttrFloatValue(attributes, keyName).isPresent());

		when(attributes.getValue(keyName)).thenReturn(String.valueOf(fValue));
		assertEquals(Optional.ofNullable(fValue), s.getAttrFloatValue(attributes, keyName));

		when(attributes.getValue(keyName)).thenReturn("nope");
		assertFalse(s.getAttrFloatValue(attributes, keyName).isPresent());

		verify(attributes, times(3)).getValue(keyName);
	}

	@Test
	void testGetAttrFloatValueAttributesStringFloat() {
		final var orDefault = faker.random().nextFloat();

		assertEquals(orDefault, s.getAttrFloatValue(attributes, keyName, orDefault));

		when(attributes.getValue(keyName)).thenReturn(String.valueOf(fValue));
		assertEquals(fValue, s.getAttrFloatValue(attributes, keyName, orDefault));

		verify(attributes, times(2)).getValue(keyName);
	}

}
