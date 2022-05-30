/*
 * This file is part of ffprobe-jaxb.
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
 * Copyright (C) hdsdi3g for hd3g.tv 2018-2020
 *
 */
package tv.hd3g.ffprobejaxb;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD;
import static javax.xml.XMLConstants.ACCESS_EXTERNAL_SCHEMA;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.ffmpeg.ffprobe.ChapterType;
import org.ffmpeg.ffprobe.ChaptersType;
import org.ffmpeg.ffprobe.ErrorType;
import org.ffmpeg.ffprobe.FfprobeType;
import org.ffmpeg.ffprobe.FormatType;
import org.ffmpeg.ffprobe.FramesType;
import org.ffmpeg.ffprobe.LibraryVersionType;
import org.ffmpeg.ffprobe.LibraryVersionsType;
import org.ffmpeg.ffprobe.PacketType;
import org.ffmpeg.ffprobe.PacketsAndFramesType;
import org.ffmpeg.ffprobe.PacketsType;
import org.ffmpeg.ffprobe.PixelFormatType;
import org.ffmpeg.ffprobe.PixelFormatsType;
import org.ffmpeg.ffprobe.ProgramType;
import org.ffmpeg.ffprobe.ProgramVersionType;
import org.ffmpeg.ffprobe.ProgramsType;
import org.ffmpeg.ffprobe.StreamType;
import org.ffmpeg.ffprobe.StreamsType;
import org.xml.sax.SAXException;

public class FFprobeJAXB {

	public final FfprobeType probeResult;
	private final String xmlContent;

	public FFprobeJAXB(final String xmlContent, final Consumer<String> onWarnLog) {
		this.xmlContent = xmlContent;
		try {
			final var jc = JAXBContext.newInstance("org.ffmpeg.ffprobe");
			final var unmarshaller = jc.createUnmarshaller();

			/**
			 * Prepare an error catcher if trouble are catched during import.
			 */
			unmarshaller.setEventHandler(e -> {
				final var locator = e.getLocator();
				onWarnLog.accept("XML validation: "
				                 + e.getMessage() + " [s"
				                 + e.getSeverity() + "] at line "
				                 + locator.getLineNumber() + ", column "
				                 + locator.getColumnNumber() + " offset "
				                 + locator.getOffset() + " node: "
				                 + locator.getNode() + ", object "
				                 + locator.getObject());
				return true;
			});

			final var xmlDocumentBuilderFactory = DocumentBuilderFactory.newInstance();// NOSONAR
			xmlDocumentBuilderFactory.setAttribute(ACCESS_EXTERNAL_DTD, "");
			xmlDocumentBuilderFactory.setAttribute(ACCESS_EXTERNAL_SCHEMA, "");
			final var xmlDocumentBuilder = xmlDocumentBuilderFactory.newDocumentBuilder();
			xmlDocumentBuilder.setErrorHandler(null);

			final var document = xmlDocumentBuilder.parse(new ByteArrayInputStream(xmlContent.getBytes(UTF_8)));

			probeResult = unmarshaller.unmarshal(document, FfprobeType.class).getValue();
		} catch (JAXBException | SAXException | ParserConfigurationException e) {
			throw new UncheckedIOException(new IOException("Can't load XML content", e));
		} catch (final IOException e1) {
			throw new UncheckedIOException(e1);
		}
	}

	public String getXmlContent() {
		return xmlContent;
	}

	public List<ChapterType> getChapters() {
		return Optional.ofNullable(probeResult.getChapters())
		        .map(ChaptersType::getChapter)
		        .map(Collections::unmodifiableList)
		        .orElse(List.of());
	}

	public List<StreamType> getStreams() {
		return Optional.ofNullable(probeResult.getStreams())
		        .map(StreamsType::getStream)
		        .map(Collections::unmodifiableList)
		        .orElse(List.of());
	}

	public FormatType getFormat() {
		return probeResult.getFormat();
	}

	/**
	 * @return nullable
	 */
	public ErrorType getError() {
		return probeResult.getError();
	}

	/**
	 * @return nullable
	 */
	public ProgramVersionType getProgramVersion() {
		return probeResult.getProgramVersion();
	}

	public List<LibraryVersionType> getLibraryVersions() {
		return Optional.ofNullable(probeResult.getLibraryVersions())
		        .map(LibraryVersionsType::getLibraryVersion)
		        .map(Collections::unmodifiableList)
		        .orElse(List.of());
	}

	public List<PixelFormatType> getPixelFormats() {
		return Optional.ofNullable(probeResult.getPixelFormats())
		        .map(PixelFormatsType::getPixelFormat)
		        .map(Collections::unmodifiableList)
		        .orElse(List.of());
	}

	public List<PacketType> getPackets() {
		return Optional.ofNullable(probeResult.getPackets())
		        .map(PacketsType::getPacket)
		        .map(Collections::unmodifiableList)
		        .orElse(List.of());
	}

	/**
	 * {@link FrameType }
	 * {@link SubtitleType }
	 */
	public List<Object> getFrames() {
		return Optional.ofNullable(probeResult.getFrames())
		        .map(FramesType::getFrameOrSubtitle)
		        .map(Collections::unmodifiableList)
		        .orElse(List.of());
	}

	/**
	 * {@link PacketType }
	 * {@link FrameType }
	 * {@link SubtitleType }
	 */
	public List<Object> getPacketsAndFrames() {
		return Optional.ofNullable(probeResult.getPacketsAndFrames())
		        .map(PacketsAndFramesType::getPacketOrFrameOrSubtitle)
		        .map(Collections::unmodifiableList)
		        .orElse(List.of());
	}

	public List<ProgramType> getPrograms() {
		return Optional.ofNullable(probeResult.getPrograms())
		        .map(ProgramsType::getProgram)
		        .map(Collections::unmodifiableList)
		        .orElse(List.of());
	}

	public static final Predicate<StreamType> filterVideoStream = streamType -> streamType
	        .getCodecType().equals("video");
	public static final Predicate<StreamType> filterAudioStream = streamType -> streamType
	        .getCodecType().equals("audio");
	public static final Predicate<StreamType> filterDataStream = streamType -> streamType
	        .getCodecType().equals("data");

	public Stream<StreamType> getVideoStreams() {
		return getStreams().stream().filter(filterVideoStream);
	}

	public Stream<StreamType> getAudiosStreams() {
		return getStreams().stream().filter(filterAudioStream);
	}

}
