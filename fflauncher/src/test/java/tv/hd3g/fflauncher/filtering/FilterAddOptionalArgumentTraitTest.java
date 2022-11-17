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
 * Copyright (C) hdsdi3g for hd3g.tv 2022
 *
 */
package tv.hd3g.fflauncher.filtering;

import static net.datafaker.Faker.instance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import net.datafaker.Faker;

class FilterAddOptionalArgumentTraitTest {
	static Faker faker = instance();

	class Impl implements FilterAddOptionalArgumentTrait {

		@Override
		public List<FilterArgument> getArguments() {
			return args;
		}

	}

	Impl impl;

	String key;
	String valueString;
	int valueInt;
	long valueLong;
	Duration duration;
	float valueFloat;
	double valueDouble;

	@Mock
	Enum<?> valueEnum;
	@Mock
	List<FilterArgument> args;
	@Captor
	ArgumentCaptor<FilterArgument> filterArgCaptor;

	@BeforeEach
	void init() throws Exception {
		openMocks(this).close();
		key = faker.numerify("key###");
		valueString = faker.numerify("value###");
		valueInt = faker.random().nextInt(10, 1_000_000);
		valueLong = faker.random().nextLong(10, 1_000_000);
		duration = Duration.ofMillis(faker.random().nextInt(2_000, 1_000_000));
		valueFloat = Math.abs(faker.random().nextFloat());
		valueDouble = Math.abs(faker.random().nextDouble());
		impl = new Impl();
	}

	@AfterEach
	void end() {
		verifyNoMoreInteractions(args);
	}

	private void assertArgumentValue(final Object value) {
		verify(args, times(1)).add(filterArgCaptor.capture());
		assertNotNull(filterArgCaptor.getValue());
		final var arg = filterArgCaptor.getValue();
		assertEquals(key, arg.getKey());
		assertEquals(String.valueOf(value), arg.getValue());
	}

	@Test
	void testAddOptionalArgumentStringString() {
		impl.addOptionalArgument(key, (String) null);
		impl.addOptionalArgument((String) null, (String) null);
		impl.addOptionalArgument(key, valueString);
		assertArgumentValue(valueString);
	}

	@Test
	void testAddOptionalArgumentStringNumber() {
		impl.addOptionalArgument(key, (Integer) null);
		impl.addOptionalArgument((String) null, (Integer) null);
		impl.addOptionalArgument(key, valueInt);
		assertArgumentValue(valueInt);
	}

	@Test
	void testAddOptionalArgumentStringEnumOfQ() {
		impl.addOptionalArgument(key, (Enum<?>) null);
		impl.addOptionalArgument((String) null, (Enum<?>) null);
		impl.addOptionalArgument(key, valueEnum);
		assertArgumentValue(valueEnum);
	}

	@Test
	void testAddOptionalArgumentString() {
		impl.addOptionalArgument(null);
		impl.addOptionalArgument(key);

		verify(args, times(1)).add(filterArgCaptor.capture());
		final var arg = filterArgCaptor.getValue();
		assertEquals(key, arg.getKey());
		assertNull(arg.getValue());
	}

	@Test
	void testAddOptionalArgumentStringBoolean() {
		impl.addOptionalArgument(null, true);
		impl.addOptionalArgument(null, false);
		impl.addOptionalArgument(key, true);
		impl.addOptionalArgument(key, false);

		verify(args, times(1)).add(filterArgCaptor.capture());
		final var arg = filterArgCaptor.getValue();
		assertEquals(key, arg.getKey());
		assertNull(arg.getValue());
	}

	@Test
	void testAddOptionalDurationSecArgument() {
		impl.addOptionalDurationSecArgument(key, (Duration) null);
		impl.addOptionalDurationSecArgument((String) null, (Duration) null);
		impl.addOptionalDurationSecArgument(key, duration);
		assertArgumentValue(duration.toSeconds());
	}

	@Test
	void testAddOptionalDurationSecMsArgument_null() {
		impl.addOptionalDurationSecMsArgument(key, (Duration) null);
		impl.addOptionalDurationSecMsArgument((String) null, (Duration) null);
		verifyNoInteractions(args);
	}

	@Test
	void testAddOptionalDurationSecMsArgument_zero() {
		duration = Duration.ZERO;
		impl.addOptionalDurationSecMsArgument(key, duration);
		assertArgumentValue("0");
	}

	@Test
	void testAddOptionalDurationSecMsArgument_secs() {
		duration = Duration.ofSeconds(valueInt);
		impl.addOptionalDurationSecMsArgument(key, duration);
		assertArgumentValue(valueInt);
	}

	@Test
	void testAddArgumentStringOptionalOfT() {
		impl.addArgument(key, Optional.empty());
		impl.addArgument((String) null, Optional.empty());
		impl.addArgument(key, Optional.ofNullable(valueString));
		assertArgumentValue(valueString);
	}

	@Test
	void testAddOptionalArgumentStringBooleanString() {
		impl.addOptionalArgument(key, false, (String) null);
		impl.addOptionalArgument(key, true, (String) null);
		impl.addOptionalArgument((String) null, false, (String) null);
		impl.addOptionalArgument((String) null, true, (String) null);
		impl.addOptionalArgument((String) null, false, valueString);
		impl.addOptionalArgument((String) null, true, valueString);
		impl.addOptionalArgument(key, false, valueString);
		impl.addOptionalArgument(key, true, valueString);
		assertArgumentValue(valueString);
	}

	@Test
	void testAddOptionalNonNegativeArgumentStringInt() {
		impl.addOptionalNonNegativeArgument(key, -valueInt);
		impl.addOptionalNonNegativeArgument((String) null, -valueInt);
		impl.addOptionalNonNegativeArgument((String) null, valueInt);
		impl.addOptionalNonNegativeArgument(key, valueInt);
		assertArgumentValue(valueInt);
	}

	@Test
	void testAddOptionalNonNegativeArgumentStringInt_zero() {
		impl.addOptionalNonNegativeArgument(key, 0);
		assertArgumentValue(0);
	}

	@Test
	void testAddOptionalNonNegativeArgumentStringLong() {
		impl.addOptionalNonNegativeArgument(key, -valueLong);
		impl.addOptionalNonNegativeArgument((String) null, -valueLong);
		impl.addOptionalNonNegativeArgument((String) null, valueLong);
		impl.addOptionalNonNegativeArgument(key, valueLong);
		assertArgumentValue(valueLong);
	}

	@Test
	void testAddOptionalNonNegativeArgumentStringLong_zero() {
		impl.addOptionalNonNegativeArgument(key, 0l);
		assertArgumentValue(0);
	}

	@Test
	void testAddOptionalNonNegativeArgumentStringFloat() {
		impl.addOptionalNonNegativeArgument(key, -valueFloat);
		impl.addOptionalNonNegativeArgument((String) null, -valueFloat);
		impl.addOptionalNonNegativeArgument((String) null, valueFloat);
		impl.addOptionalNonNegativeArgument(key, valueFloat);
		assertArgumentValue(impl.roundWithPrecision(valueFloat));
	}

	@Test
	void testAddOptionalNonNegativeArgumentStringFloat_zero() {
		impl.addOptionalNonNegativeArgument(key, 0f);
		assertArgumentValue("0");
	}

	@Test
	void testAddOptionalNonNegativeArgumentStringDouble() {
		impl.addOptionalNonNegativeArgument(key, -valueDouble);
		impl.addOptionalNonNegativeArgument((String) null, -valueDouble);
		impl.addOptionalNonNegativeArgument((String) null, valueDouble);
		impl.addOptionalNonNegativeArgument(key, valueDouble);
		assertArgumentValue(impl.roundWithPrecision(valueDouble));
	}

	@Test
	void testAddOptionalNonNegativeArgumentStringDouble_zero() {
		impl.addOptionalNonNegativeArgument(key, 0d);
		assertArgumentValue("0");
	}

}
