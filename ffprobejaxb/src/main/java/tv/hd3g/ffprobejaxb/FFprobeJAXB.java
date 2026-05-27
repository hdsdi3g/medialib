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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.w3c.dom.Node;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.ValidationEvent;
import tv.hd3g.ffprobejaxb.data.FFProbeStream;

public abstract class FFprobeJAXB implements FFprobeReference {
    private static final Logger log = LoggerFactory.getLogger(FFprobeJAXB.class);

    private static final String LOADED_XML = "Loaded XML: {}";
    private final String xmlContent;

    protected static final Predicate<FFProbeStream> filterOutNoCodecType = s -> s.codecType() != null;

    protected FFprobeJAXB(final String xmlContent) {
        this.xmlContent = xmlContent;
    }

    @Override
    public String getXmlContent() {
        return xmlContent;
    }

    protected abstract void setJAXB(final Object rawJAXB);

    public static FFprobeJAXB load(final String xmlContent) {
        Node document;
        try {
            document = UnmarshallerTools.parseXMLDocument(xmlContent, new ErrorHandler() {

                @Override
                public void warning(final SAXParseException exception) throws SAXException {
                    log.debug(LOADED_XML, xmlContent);
                    log.warn("XML parser warning", exception);
                }

                @Override
                public void fatalError(final SAXParseException exception) throws SAXException {
                    log.debug(LOADED_XML, xmlContent);
                    log.error("XML parser fatal error", exception);
                }

                @Override
                public void error(final SAXParseException exception) throws SAXException {
                    log.debug(LOADED_XML, xmlContent);
                    log.error("XML parser error", exception);
                }
            });
        } catch (ParserConfigurationException | SAXException e) {
            throw new UncheckedIOException(new IOException("Can't load XML content", e));
        }

        final Map<FFprobeXSDVersion, List<ValidationEvent>> eventsByXSDVersion = new EnumMap<>(FFprobeXSDVersion.class);
        JAXBException lastJAXBException = null;
        for (final var xsdVersion : FFprobeXSDVersion.values()) {
            try {
                log.debug("Try to load JAXB {}", xsdVersion.name());
                final var events = new ConcurrentLinkedQueue<ValidationEvent>();
                final var ffRef = UnmarshallerTools.unmarshal(
                        xsdVersion.createInstance(),
                        document,
                        events::add,
                        xsdVersion.getClassJAXB());

                if (events.isEmpty()) {
                    return xsdVersion.make(xmlContent, ffRef);
                }

                eventsByXSDVersion.put(xsdVersion, events.stream().toList());
            } catch (final JAXBException e) {
                log.debug("Can't load JAXB", e);
                lastJAXBException = e;
            }
        }

        if (lastJAXBException != null) {
            throw new UncheckedIOException(new IOException(lastJAXBException));
        }

        eventsByXSDVersion.forEach((version, events) -> events.forEach(e -> {
            final var locator = e.getLocator();
            log.error(
                    "JAXB {} says: {} [s{}] at line {}, column {} offset {} node: {}, object {}",
                    version,
                    e.getMessage(),
                    e.getSeverity(),
                    locator.getLineNumber(),
                    locator.getColumnNumber(),
                    locator.getOffset(),
                    locator.getNode(),
                    locator.getObject(),
                    e.getLinkedException());
        }));

        throw new IllegalArgumentException(
                "Can't properly load ffprobe JAXB. You should update ffprobe.xsd ref and/or check XML document");
    }

    @SuppressWarnings("unchecked")
    protected static <T> Stream<T> getSubList(final Object jaxbSubListClass, final Class<T> outputFormat) {
        if (jaxbSubListClass == null) {
            return Stream.empty();
        }

        final var getterMethod = Stream.of(jaxbSubListClass.getClass().getMethods())
                .filter(m -> m.getName().startsWith("get"))
                .filter(m -> m.getReturnType().isAssignableFrom(List.class))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Can't found a List getter"));

        try {
            final var raw = getterMethod.invoke(jaxbSubListClass);
            if (raw == null) {
                return Stream.empty();
            }
            return ((List<T>) raw).stream()
                    .filter(Objects::nonNull)
                    .map(outputFormat::cast);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public MediaSummary getMediaSummary() {
        return MediaSummary.create(this);
    }

    @Override
    public String toString() {
        return getMediaSummary().toString();
    }

    static String getCodecTagString(final String value) {
        if (value == null || value.equals("[0][0][0][0]")) {
            return null;
        }
        return value;
    }

    static int getLevelTag(final int value) {
        if (value < 0) {
            return 0;
        }
        return value;
    }

    static boolean getNonNull(final Boolean value) {
        if (value == null) {
            return false;
        }
        return value;
    }

    static int getNonNull(final Integer value) {
        if (value == null) {
            return 0;
        }
        return value;
    }

    static long getNonNull(final Long value) {
        if (value == null) {
            return 0;
        }
        return value;
    }

    static float getNonNull(final Float value) {
        if (value == null) {
            return 0;
        }
        return value;
    }

    static String removeUnknown(final String value) {
        if ("unknown".equalsIgnoreCase(value)) {
            return null;
        }
        return value;
    }

}
