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
package tv.hd3g.fflauncher.ffprobecontainer;

import static tv.hd3g.fflauncher.ffprobecontainer.FFprobeCodecType.AUDIO;
import static tv.hd3g.fflauncher.ffprobecontainer.FFprobeCodecType.VIDEO;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import tv.hd3g.fflauncher.enums.ChannelLayout;
import tv.hd3g.fflauncher.recipes.ContainerAnalyserResult;
import tv.hd3g.fflauncher.recipes.ContainerAnalyserSession;
import tv.hd3g.processlauncher.InputStreamConsumer;
import tv.hd3g.processlauncher.ProcesslauncherLifecycle;

public class FFprobeResultSAX extends DefaultHandler implements ErrorHandler, InputStreamConsumer {
	private static final String CAN_T_EXTRACT_NUMBER_FROM = "Can't extract number from {}=\"{}\"";
	private static final Logger log = LogManager.getLogger();
	private static final SAXParserFactory factory;

	static {
		try {
			factory = SAXParserFactory.newInstance();
			factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
			factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		} catch (SAXNotRecognizedException | SAXNotSupportedException | ParserConfigurationException e) {
			throw new InternalError("Can't load SAX parser", e);
		}
	}

	private String processSource;
	private final List<FFprobePacket> packets;
	private final List<FFprobeAudioFrame> audioFrames;
	private final List<FFprobeVideoFrame> videoFrames;

	private final List<FFprobeVideoFrameConst> olderVideoConsts;
	private final List<FFprobeAudioFrameConst> olderAudioConsts;
	private FFprobeVideoFrameConst videoConst;
	private FFprobeAudioFrameConst audioConst;

	public FFprobeResultSAX() {
		packets = new ArrayList<>();
		audioFrames = new ArrayList<>();
		videoFrames = new ArrayList<>();
		olderVideoConsts = new ArrayList<>();
		olderAudioConsts = new ArrayList<>();
	}

	@Override
	public void onProcessStart(final InputStream processInputStream, final ProcesslauncherLifecycle source) {
		processSource = Optional.ofNullable(source)
				.map(ProcesslauncherLifecycle::toString)
				.orElse("");
		try {
			factory.newSAXParser().parse(new InputSource(processInputStream), this);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			log.error("Can't load XML in SAX parser for {}", processSource, e);
			Optional.ofNullable(source)
					.ifPresent(ProcesslauncherLifecycle::kill);
		}
	}

	@Override
	public void startElement(final String uri,
							 final String localName,
							 final String qName,
							 final Attributes attributes) throws SAXException {
		if (qName.equals("packet")) {
			onPacket(attributes);
		} else if (qName.equals("frame")) {
			onFrame(attributes);
		}
	}

	private static Map<String, String> getAttributes(final Attributes attributes) {
		final var result = new LinkedHashMap<String, String>(attributes.getLength());
		for (var pos = 0; pos < attributes.getLength(); pos++) {
			result.put(attributes.getQName(pos), attributes.getValue(pos));
		}
		return result;
	}

	/**
	 * codec_type="audio" stream_index="1" pts="2555904" pts_time="57.957007" dts="2555904" dts_time="57.957007" duration="1024" duration_time="0.023220" size="442" pos="84622585" flags="K_"/>
	 * <packet codec_type="video" stream_index="0" pts="1738737" pts_time="57.957900" dts="1738737" dts_time="57.957900" duration="1001" duration_time="0.033367" size="75214" pos="84623027" flags=
	 * "__"/>
	 */
	private void onPacket(final Attributes attributes) {
		if (attributes.getLength() != 11) {
			log.error("Invalid <packet>, expect 11 attributes, get {}: {}",
					attributes.getLength(), getAttributes(attributes));
			return;
		}

		packets.add(new FFprobePacket(
				FFprobeCodecType.fromString(getAttrValue(attributes, "codec_type")),
				getAttrIntValue(attributes, "stream_index"),
				getAttrLongValue(attributes, "pts"),
				getAttrFloatValue(attributes, "pts_time"),
				getAttrLongValue(attributes, "dts"),
				getAttrFloatValue(attributes, "dts_time"),
				getAttrIntValue(attributes, "duration"),
				getAttrFloatValue(attributes, "duration_time"),
				getAttrIntValue(attributes, "size"),
				getAttrLongValue(attributes, "pos"),
				getAttrValue(attributes, "flags")));
	}

	/**
	 * <frame media_type="audio"
	 * stream_index="1"
	 * key_frame="1"
	 * pts="2555904"
	 * pts_time="57.957007"
	 * pkt_dts="2555904"
	 * pkt_dts_time="57.957007"
	 * best_effort_timestamp="2555904"
	 * best_effort_timestamp_time="57.957007"
	 * pkt_duration="1024"
	 * pkt_duration_time="0.023220"
	 * pkt_pos="84622585"
	 * pkt_size="442"
	 * --
	 * sample_fmt="fltp" nb_samples="1024" channels="2" channel_layout="stereo"/>
	 * --
	 * <frame media_type="video" key_frame="0" [...]
	 * width="2160"
	 * height="3840"
	 * pix_fmt="yuv420p"
	 * sample_aspect_ratio="1:1"
	 * pict_type="P"
	 * coded_picture_number="0"
	 * display_picture_number="0"
	 * interlaced_frame="0"
	 * top_field_first="0"
	 * repeat_pict="0"
	 * color_range="tv"
	 * color_space="bt709"
	 * color_primaries="bt709"
	 * color_transfer= * "bt709"/>
	 */
	private void onFrame(final Attributes attributes) {
		if (attributes.getLength() < 13) {
			log.error("Invalid <frame>, expect at least 13 attributes, get {}: {}",
					attributes.getLength(), getAttributes(attributes));
			return;
		}

		final var mediaType = FFprobeCodecType.fromString(getAttrValue(attributes, "media_type"));
		final var baseFrame = new FFprobeBaseFrame(
				mediaType,
				getAttrIntValue(attributes, "stream_index"),
				getAttrBooleanValue(attributes, "key_frame"),
				getAttrLongValue(attributes, "pts"),
				getAttrFloatValue(attributes, "pts_time"),
				getAttrLongValue(attributes, "pkt_dts"),
				getAttrFloatValue(attributes, "pkt_dts_time"),
				getAttrLongValue(attributes, "best_effort_timestamp"),
				getAttrFloatValue(attributes, "best_effort_timestamp_time"),
				getAttrIntValue(attributes, "pkt_duration"),
				getAttrFloatValue(attributes, "pkt_duration_time"),
				getAttrLongValue(attributes, "pkt_pos"),
				getAttrIntValue(attributes, "pkt_size"));

		if (mediaType == VIDEO) {
			onFrameVideo(attributes, baseFrame);
		} else if (mediaType == AUDIO) {
			onFrameAudio(attributes, baseFrame);
		} else {
			log.warn("Can't manage this frame type: {}, {}", mediaType, getAttributes(attributes));
		}
	}

	/**
	 * <frame media_type="video" [...] width="2160" height="3840" pix_fmt="yuv420p" sample_aspect_ratio="1:1"
	 * pict_type="P"
	 * coded_picture_number="0" display_picture_number="0"
	 * interlaced_frame="0" top_field_first="0" repeat_pict="0"
	 * color_range="tv" color_space="bt709" color_primaries="bt709" color_transfer="bt709"/>
	 */
	private void onFrameVideo(final Attributes attributes, final FFprobeBaseFrame baseFrame) {
		if (attributes.getLength() != 27) {
			log.error("Invalid <frame media_type=\"video\">, expect 27 attributes, get {}: {}",
					attributes.getLength(), getAttributes(attributes));
			return;
		}

		final var pictType = Optional.ofNullable(FFprobePictType.valueOf(attributes.getValue("pict_type")))
				.orElse(FFprobePictType.UNKNOWN);

		final var frame = new FFprobeVideoFrame(
				baseFrame,
				pictType,
				"1".equals(attributes.getValue("repeat_pict")) ? true : false);
		videoFrames.add(frame);

		final var currentVideoConst = new FFprobeVideoFrameConst(
				frame,
				getAttrIntValue(attributes, "width"),
				getAttrIntValue(attributes, "height"),
				getAttrValue(attributes, "pix_fmt"),
				getAttrValue(attributes, "sample_aspect_ratio"),
				getAttrIntValue(attributes, "coded_picture_number"),
				getAttrIntValue(attributes, "display_picture_number"),
				getAttrBooleanValue(attributes, "interlaced_frame"),
				getAttrBooleanValue(attributes, "top_field_first"),
				getAttrValue(attributes, "color_range"),
				getAttrValue(attributes, "color_space"),
				getAttrValue(attributes, "color_primaries"),
				getAttrValue(attributes, "color_transfer"));

		if (videoConst == null) {
			videoConst = currentVideoConst;
		} else if (videoConst.valuesEquals(currentVideoConst) == false) {
			olderVideoConsts.add(videoConst);
			videoConst = currentVideoConst;
		}
	}

	/**
	 * <frame media_type="audio" [...] sample_fmt="fltp" nb_samples="1024" channels="2" channel_layout="stereo"/>
	 */
	private void onFrameAudio(final Attributes attributes, final FFprobeBaseFrame baseFrame) {
		if (attributes.getLength() != 17) {
			log.error("Invalid <frame media_type=\"audio\">, expect 17 attributes, get {}: {}",
					attributes.getLength(), getAttributes(attributes));
			return;
		}

		final var frame = new FFprobeAudioFrame(
				baseFrame,
				getAttrIntValue(attributes, "nb_samples"));
		audioFrames.add(frame);

		final var currentAudioConst = new FFprobeAudioFrameConst(
				frame,
				getAttrValue(attributes, "sample_fmt"),
				getAttrIntValue(attributes, "channels"),
				ChannelLayout.parse(getAttrValue(attributes, "channel_layout")));

		if (audioConst == null) {
			audioConst = currentAudioConst;
		} else if (audioConst.valuesEquals(currentAudioConst) == false) {
			olderAudioConsts.add(audioConst);
			audioConst = currentAudioConst;
		}
	}

	public ContainerAnalyserResult getResult(final ContainerAnalyserSession session) {
		return new ContainerAnalyserResult(
				session,
				Collections.unmodifiableList(packets),
				Collections.unmodifiableList(audioFrames),
				Collections.unmodifiableList(videoFrames),
				videoConst,
				audioConst,
				Collections.unmodifiableList(olderVideoConsts),
				Collections.unmodifiableList(olderAudioConsts));
	}

	private static String getAttrValue(final Attributes attributes, final String keyName) {
		final var value = attributes.getValue(keyName);
		if (value == null) {
			log.error("Can't get attribute {}", keyName);
			return "";
		}
		return value;
	}

	private static boolean getAttrBooleanValue(final Attributes attributes, final String keyName) {
		return getAttrValue(attributes, keyName).equals("1");
	}

	private static int getAttrIntValue(final Attributes attributes, final String keyName) {
		final var value = getAttrValue(attributes, keyName);
		try {
			return Integer.valueOf(value);
		} catch (final NumberFormatException e) {
			log.error(CAN_T_EXTRACT_NUMBER_FROM, keyName, value);
			return -1;
		}
	}

	private static long getAttrLongValue(final Attributes attributes, final String keyName) {
		final var value = getAttrValue(attributes, keyName);
		try {
			return Long.valueOf(value);
		} catch (final NumberFormatException e) {
			log.error(CAN_T_EXTRACT_NUMBER_FROM, keyName, value);
			return -1;
		}
	}

	private static float getAttrFloatValue(final Attributes attributes, final String keyName) {
		final var value = getAttrValue(attributes, keyName);
		try {
			return Float.valueOf(value);
		} catch (final NumberFormatException e) {
			log.error(CAN_T_EXTRACT_NUMBER_FROM, keyName, value);
			return -1;
		}
	}

	@Override
	public void error(final SAXParseException e) throws SAXException {
		log.warn("SAX error (during {})", processSource, e);
	}

	@Override
	public void fatalError(final SAXParseException e) throws SAXException {
		log.warn("SAX error (during {})", processSource, e);
	}

	@Override
	public void warning(final SAXParseException e) throws SAXException {
		log.warn("SAX warning (during {})", processSource, e);
	}

}
