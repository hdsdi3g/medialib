/*
 * This file is part of ffprobejaxb.
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
package tv.hd3g.ffprobejaxb.data;

import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.slf4j.LoggerFactory.getLogger;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.Logger;

/**
 * StreamDispositionType
 */
public record FFProbeStreamDisposition(boolean asDefault,
									   boolean dub,
									   boolean original,
									   boolean comment,
									   boolean lyrics,
									   boolean karaoke,
									   boolean forced,
									   boolean hearingImpaired,
									   boolean visualImpaired,
									   boolean cleanEffects,
									   boolean attachedPic,
									   boolean timedThumbnails,
									   boolean nonDiegetic,
									   boolean captions,
									   boolean descriptions,
									   boolean metadata,
									   boolean dependent,
									   boolean stillImage) {

	private static final Logger log = getLogger(FFProbeStreamDisposition.class);

	public Stream<String> resumeDispositions() {
		final var thisClass = this;
		/**
		 * @return only booleans, please add non-boolean values after the Stream
		 */
		return Stream.of(this.getClass().getDeclaredFields())
				.filter(f -> {
					try {
						return f.getBoolean(thisClass);
					} catch (final IllegalArgumentException iae) {
						return false;
					} catch (final IllegalAccessException e) {
						throw new IllegalCallerException("Can't extract values!", e);
					}
				})
				.map(Field::getName)
				.map(f -> f.equals("asDefault") ? "default" : f)
				/**
				 * https://www.geeksforgeeks.org/convert-camel-case-string-to-snake-case-in-java/
				 */
				.map(n -> n.replaceAll("([a-z])([A-Z]+)", "$1 $2").toLowerCase());
	}

	public static FFProbeStreamDisposition getByNames(final String... names) {
		return Stream.of(FFProbeStreamDisposition.class.getConstructors())
				.findFirst()
				.map(constructor -> {
					final var paramNames = Stream.of(names)
							.filter(Objects::nonNull)
							.map(String::toLowerCase)
							.map(f -> f.replace("_", ""))
							.map(f -> f.equals("default") ? "asdefault" : f)
							.distinct()
							.collect(toUnmodifiableSet());

					final var newInstanceParams = Stream.of(constructor.getParameters())
							.map(param -> {
								if (param.getType().isAssignableFrom(boolean.class) == false) {
									return null;
								}
								return paramNames.contains(param.getName().toLowerCase());
							})
							.toList()
							.toArray();

					try {
						return (FFProbeStreamDisposition) constructor.newInstance(newInstanceParams);
					} catch (InstantiationException
							 | IllegalAccessException
							 | IllegalArgumentException
							 | InvocationTargetException e) {
						log.error("Can't create StreamDisposition", e);
					}
					return null;
				})
				.flatMap(Optional::ofNullable)
				.orElseGet(() -> new FFProbeStreamDisposition(
						false, false, false, false, false,
						false, false, false, false, false,
						false, false, false, false, false,
						false, false, false));
	}

}
