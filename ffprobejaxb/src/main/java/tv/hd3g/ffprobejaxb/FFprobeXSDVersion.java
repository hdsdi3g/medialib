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
 * Copyright (C) hdsdi3g for hd3g.tv 2023
 *
 */
package tv.hd3g.ffprobejaxb;

import java.util.function.Function;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

public enum FFprobeXSDVersion {

	V611("org.ffmpeg.ffprobe611", org.ffmpeg.ffprobe611.FfprobeType.class, FFprobeJAXB611::new),
	V436("org.ffmpeg.ffprobe436", org.ffmpeg.ffprobe436.FfprobeType.class, FFprobeJAXB436::new);

	private final String contextPath;
	private final Function<String, FFprobeJAXB> makeJAXBRef;
	private final Class<?> classJAXB;

	FFprobeXSDVersion(final String contextPath,
					  final Class<?> classJAXB,
					  final Function<String, FFprobeJAXB> makeJAXBRef) {
		this.contextPath = contextPath;
		this.classJAXB = classJAXB;
		this.makeJAXBRef = makeJAXBRef;
	}

	public JAXBContext createInstance() throws JAXBException {
		return JAXBContext.newInstance(contextPath);
	}

	public FFprobeJAXB make(final String xmlContent, final Object rawJAXB) {
		final var result = makeJAXBRef.apply(xmlContent);
		result.setJAXB(rawJAXB);
		return result;
	}

	public Class<?> getClassJAXB() {
		return classJAXB;
	}
}
