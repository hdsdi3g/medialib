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

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.Attributes;

public interface SAXAttributeParserTraits {
	Logger log = LogManager.getLogger();

	default Optional<String> getAttrValue(final Attributes attributes, final String keyName) {
		return Optional.ofNullable(attributes.getValue(keyName));
	}

	default String getAttrValue(final Attributes attributes, final String keyName, final String orDefault) {
		return getAttrValue(attributes, keyName).orElse(orDefault);
	}

	default Optional<Boolean> getAttrBooleanValue(final Attributes attributes, final String keyName) {
		return getAttrValue(attributes, keyName).map(v -> v.equals("1"));
	}

	default boolean getAttrBooleanValue(final Attributes attributes, final String keyName, final boolean orDefault) {
		return getAttrBooleanValue(attributes, keyName).orElse(orDefault);
	}

	default Optional<Integer> getAttrIntValue(final Attributes attributes, final String keyName) {
		return getAttrValue(attributes, keyName).flatMap(v -> {
			try {
				return Optional.ofNullable(Integer.valueOf(v));
			} catch (final NumberFormatException e) {
				log.warn("Can't parse number: {}", v);
				return Optional.empty();
			}
		});
	}

	default int getAttrIntValue(final Attributes attributes, final String keyName, final int orDefault) {
		return getAttrIntValue(attributes, keyName).orElse(orDefault);
	}

	default Optional<Long> getAttrLongValue(final Attributes attributes, final String keyName) {
		return getAttrValue(attributes, keyName).flatMap(v -> {
			try {
				return Optional.ofNullable(Long.valueOf(v));
			} catch (final NumberFormatException e) {
				log.warn("Can't parse number: {}", v);
				return Optional.empty();
			}
		});
	}

	default long getAttrLongValue(final Attributes attributes, final String keyName, final long orDefault) {
		return getAttrLongValue(attributes, keyName).orElse(orDefault);
	}

	default Optional<Float> getAttrFloatValue(final Attributes attributes, final String keyName) {
		return getAttrValue(attributes, keyName).flatMap(v -> {
			try {
				return Optional.ofNullable(Float.valueOf(v));
			} catch (final NumberFormatException e) {
				log.warn("Can't parse number: {}", v);
				return Optional.empty();
			}
		});
	}

	default float getAttrFloatValue(final Attributes attributes, final String keyName, final float orDefault) {
		return getAttrFloatValue(attributes, keyName).orElse(orDefault);
	}

}
