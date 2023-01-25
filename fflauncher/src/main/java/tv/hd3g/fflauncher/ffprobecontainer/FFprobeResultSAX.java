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
import static tv.hd3g.fflauncher.ffprobecontainer.FFprobeCodecType.OTHER;
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

public class FFprobeResultSAX extends DefaultHandler implements
							  ErrorHandler,
							  InputStreamConsumer,
							  SAXAttributeParserTraits {
	private static final String STREAM_INDEX = "stream_index";
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
		if (getAttrIntValue(attributes, STREAM_INDEX, -1) == -1) {
			return;
		}

		packets.add(new FFprobePacket(
				FFprobeCodecType.fromString(getAttrValue(attributes, "codec_type", OTHER.toString())),
				getAttrIntValue(attributes, STREAM_INDEX, -1),
				getAttrLongValue(attributes, "pts", -1),
				getAttrFloatValue(attributes, "pts_time", -1),
				getAttrLongValue(attributes, "dts", -1),
				getAttrFloatValue(attributes, "dts_time", -1),
				getAttrIntValue(attributes, "duration", -1),
				getAttrFloatValue(attributes, "duration_time", -1),
				getAttrIntValue(attributes, "size", -1),
				getAttrLongValue(attributes, "pos", -1),
				getAttrValue(attributes, "flags", null)));
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
		if (getAttrIntValue(attributes, STREAM_INDEX, -1) == -1) {
			return;
		}

		final var mediaType = FFprobeCodecType.fromString(getAttrValue(attributes, "media_type", OTHER.toString()));
		final var baseFrame = new FFprobeBaseFrame(
				mediaType,
				getAttrIntValue(attributes, STREAM_INDEX, -1),
				getAttrBooleanValue(attributes, "key_frame", true),
				getAttrLongValue(attributes, "pts", -1),
				getAttrFloatValue(attributes, "pts_time", -1),
				getAttrLongValue(attributes, "pkt_dts", -1),
				getAttrFloatValue(attributes, "pkt_dts_time", -1),
				getAttrLongValue(attributes, "best_effort_timestamp", -1),
				getAttrFloatValue(attributes, "best_effort_timestamp_time", -1),
				getAttrIntValue(attributes, "pkt_duration", -1),
				getAttrFloatValue(attributes, "pkt_duration_time", -1),
				getAttrLongValue(attributes, "pkt_pos", -1),
				getAttrIntValue(attributes, "pkt_size", -1));

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
		final var frame = new FFprobeVideoFrame(
				baseFrame,
				FFprobePictType.valueOf(getAttrValue(attributes, "pict_type", "UNKNOWN")),
				"1".equals(attributes.getValue("repeat_pict")) ? true : false);
		videoFrames.add(frame);

		final var currentVideoConst = new FFprobeVideoFrameConst(
				frame,
				getAttrIntValue(attributes, "width", 0),
				getAttrIntValue(attributes, "height", 0),
				getAttrValue(attributes, "pix_fmt", null),
				getAttrValue(attributes, "sample_aspect_ratio", null),
				getAttrIntValue(attributes, "coded_picture_number", 0),
				getAttrIntValue(attributes, "display_picture_number", 0),
				getAttrBooleanValue(attributes, "interlaced_frame", false),
				getAttrBooleanValue(attributes, "top_field_first", false),
				getAttrValue(attributes, "color_range", null),
				getAttrValue(attributes, "color_space", null),
				getAttrValue(attributes, "color_primaries", null),
				getAttrValue(attributes, "color_transfer", null));

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
		final var frame = new FFprobeAudioFrame(
				baseFrame,
				getAttrIntValue(attributes, "nb_samples", -1));
		audioFrames.add(frame);

		final var currentAudioConst = new FFprobeAudioFrameConst(
				frame,
				getAttrValue(attributes, "sample_fmt", null),
				getAttrIntValue(attributes, "channels", 0),
				ChannelLayout.parse(getAttrValue(attributes, "channel_layout", "")));

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
