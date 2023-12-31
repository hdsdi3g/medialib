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

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD;
import static javax.xml.XMLConstants.ACCESS_EXTERNAL_SCHEMA;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

import org.w3c.dom.Node;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

public interface UnmarshallerTools {

	static <T> T unmarshal(final JAXBContext context,
						   final Node document,
						   final ValidationEventHandler handler,
						   final Class<T> declaredType) throws JAXBException {
		final var unmarshaller = context.createUnmarshaller();
		unmarshaller.setEventHandler(handler);
		return unmarshaller.unmarshal(document, declaredType).getValue();
	}

	static Node parseXMLDocument(final String xmlContent,
								 final ErrorHandler errorHandler) throws ParserConfigurationException, SAXException {
		final var xmlDocumentBuilderFactory = DocumentBuilderFactory.newInstance();// NOSONAR
		xmlDocumentBuilderFactory.setAttribute(ACCESS_EXTERNAL_DTD, "");
		xmlDocumentBuilderFactory.setAttribute(ACCESS_EXTERNAL_SCHEMA, "");
		xmlDocumentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		final var xmlDocumentBuilder = xmlDocumentBuilderFactory.newDocumentBuilder();
		xmlDocumentBuilder.setErrorHandler(errorHandler);

		try {
			return xmlDocumentBuilder.parse(new ByteArrayInputStream(xmlContent.getBytes(UTF_8)));
		} catch (final IOException e) {
			throw new UncheckedIOException(e);
		}
	}

}
