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
 * Copyright (C) hdsdi3g for hd3g.tv 2020
 *
 */
package tv.hd3g.fflauncher.acm;

import static java.util.stream.Collectors.toUnmodifiableList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tv.hd3g.fflauncher.acm.AudioChannelManipulationSetup.extractFromParentheses;
import static tv.hd3g.fflauncher.enums.ChannelLayout.DOWNMIX;
import static tv.hd3g.fflauncher.enums.ChannelLayout.MONO;
import static tv.hd3g.fflauncher.enums.ChannelLayout.STEREO;
import static tv.hd3g.fflauncher.enums.SourceNotFoundPolicy.ERROR;
import static tv.hd3g.fflauncher.enums.SourceNotFoundPolicy.REMOVE_OUT_STREAM;

import java.util.List;
import java.util.stream.Stream;

import org.ffmpeg.ffprobe.StreamType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import tv.hd3g.fflauncher.acm.OutputAudioStream.OutputAudioChannel;
import tv.hd3g.fflauncher.enums.ChannelLayout;
import tv.hd3g.fflauncher.enums.SourceNotFoundPolicy.SourceNotFoundException;
import tv.hd3g.ffprobejaxb.FFprobeJAXB;

class AudioChannelManipulationSetupTest {

	@Test
	void testRegexExtractFromParentheses() {
		final var matcher0 = extractFromParentheses.matcher("aa(bb)cc");
		assertTrue(matcher0.find());
		assertEquals("(bb)", matcher0.group());

		final var matcher1 = extractFromParentheses.matcher("aabbcc");
		assertFalse(matcher1.find());

		final var matcher2 = extractFromParentheses.matcher("aa(bb)cc(dd)");
		assertTrue(matcher2.find());
		assertEquals(1, matcher2.groupCount());
		assertEquals("(bb)", matcher2.group(0));

		final var matcher3 = extractFromParentheses.matcher("aa(bb)cc");
		assertEquals("aacc", matcher3.replaceAll(""));

		final var matcher4 = extractFromParentheses.matcher("aa()cc");
		assertFalse(matcher4.find());
	}

	AudioChannelManipulationSetup setup;

	FFprobeJAXB ffprobeJAXBfile0;
	FFprobeJAXB ffprobeJAXBfile1;
	List<FFprobeJAXB> sourcesFiles;

	@BeforeEach
	void init() {
		setup = new AudioChannelManipulationSetup();

		ffprobeJAXBfile0 = Mockito.mock(FFprobeJAXB.class);
		final var stream00 = new StreamType();
		stream00.setIndex(1);
		stream00.setChannelLayout("stereo");
		stream00.setChannels(2);

		final var stream01 = new StreamType();
		stream01.setIndex(2);
		stream01.setChannelLayout("stereo");
		stream01.setChannels(2);

		Mockito.when(ffprobeJAXBfile0.getAudiosStreams()).thenReturn(Stream.of(stream00, stream01));

		ffprobeJAXBfile1 = Mockito.mock(FFprobeJAXB.class);
		final var stream1 = new StreamType();
		stream1.setIndex(0);
		stream1.setChannelLayout("");
		stream1.setChannels(1);

		Mockito.when(ffprobeJAXBfile1.getAudiosStreams()).thenReturn(Stream.of(stream1));
		sourcesFiles = List.of(ffprobeJAXBfile0, ffprobeJAXBfile1);
	}

	@Test
	void testGetSetChannelMap() {
		assertNull(setup.getChannelMap());
		final var channelMap = List.of("nothing");
		setup.setChannelMap(channelMap);
		assertEquals(channelMap, setup.getChannelMap());
	}

	@Test
	void testGetSetNotFound() {
		assertNull(setup.getNotFound());
		setup.setNotFound(ERROR);
		assertEquals(ERROR, setup.getNotFound());
		setup.setNotFound(REMOVE_OUT_STREAM);
		assertEquals(REMOVE_OUT_STREAM, setup.getNotFound());
	}

	@Test
	void testGetSetOutputFileIndexes() {
		assertNull(setup.getOutputFileIndexes());
		final var outputFileIndexes = List.of(0, 1);
		setup.setOutputFileIndexes(outputFileIndexes);
		assertEquals(outputFileIndexes, setup.getOutputFileIndexes());
	}

	@Test
	void testGetAllOutputStreamList_noSources() {
		setup.setChannelMap(List.of());
		final var outStreams = setup.getAllOutputStreamList(List.of());
		assertNotNull(outStreams);
		assertTrue(outStreams.isEmpty());
	}

	@Test
	void testGetAllOutputStreamList_noDests() {
		setup.setChannelMap(List.of());
		final var outStreams = setup.getAllOutputStreamList(sourcesFiles);
		assertNotNull(outStreams);
		assertTrue(outStreams.isEmpty());
	}

	@ParameterizedTest
	@ValueSource(strings = { "12+44", "0:5", "0:5:0+0:5:1" })
	void testGetAllOutputStreamList_PointAbsoluteSources_MissingSourceError(final String map) {
		setup.setChannelMap(List.of(map));
		assertThrows(SourceNotFoundException.class, () -> setup.getAllOutputStreamList(sourcesFiles));
	}

	@ParameterizedTest
	@ValueSource(strings = { "12+44", "0:5", "0:5:0+0:5:1" })
	void testGetAllOutputStreamList_PointAbsoluteSources_MissingSourceDiscardStream(final String map) {
		setup.setChannelMap(List.of(map));
		setup.setNotFound(REMOVE_OUT_STREAM);
		final var outStreams = setup.getAllOutputStreamList(sourcesFiles);
		assertNotNull(outStreams);
		assertTrue(outStreams.isEmpty());
	}

	@Test
	void testGetAllOutputStreamList_PointAbsoluteSources_1stream() {
		setup.setChannelMap(List.of("1+2"));
		final var outStreams = setup.getAllOutputStreamList(sourcesFiles);
		assertNotNull(outStreams);
		assertEquals(1, outStreams.size());
		final var channels = checkOutputAudioStream(outStreams.get(0), 0, 0, STEREO);
		assertEquals(2, channels.size());
		checkOutputAudioStream(channels.get(0), 0, 1, 1, 0);
		checkOutputAudioStream(channels.get(1), 0, 2, 0, 1);
	}

	@Test
	void testGetAllOutputStreamList_SourcesByRefs_1stream() {
		setup.setChannelMap(List.of("0:1"));
		final var outStreams = setup.getAllOutputStreamList(sourcesFiles);
		assertNotNull(outStreams);
		assertEquals(1, outStreams.size());
		final var channels = checkOutputAudioStream(outStreams.get(0), 0, 0, STEREO);
		assertEquals(2, channels.size());
		checkOutputAudioStream(channels.get(0), 0, 2, 0, 0);
		checkOutputAudioStream(channels.get(1), 0, 2, 1, 1);
	}

	@Test
	void testGetAllOutputStreamList_SourcesByRefsChannels_1stream() {
		setup.setChannelMap(List.of("0:0:1+0:1:0"));
		final var outStreams = setup.getAllOutputStreamList(sourcesFiles);
		assertNotNull(outStreams);
		assertEquals(1, outStreams.size());
		final var channels = checkOutputAudioStream(outStreams.get(0), 0, 0, STEREO);
		assertEquals(2, channels.size());
		checkOutputAudioStream(channels.get(0), 0, 1, 1, 0);
		checkOutputAudioStream(channels.get(1), 0, 2, 0, 1);
	}

	@Test
	void testGetAllOutputStreamList_PointAbsoluteSources_2streams() {
		setup.setChannelMap(List.of("1+2", "4"));
		final var outStreams = setup.getAllOutputStreamList(sourcesFiles);
		assertNotNull(outStreams);
		assertEquals(2, outStreams.size());
		final var channels0 = checkOutputAudioStream(outStreams.get(0), 0, 0, STEREO);
		assertEquals(2, channels0.size());
		checkOutputAudioStream(channels0.get(0), 0, 1, 1, 0);
		checkOutputAudioStream(channels0.get(1), 0, 2, 0, 1);

		final var channels1 = checkOutputAudioStream(outStreams.get(1), 0, 1, MONO);
		assertEquals(1, channels1.size());
		checkOutputAudioStream(channels1.get(0), 1, 0, 0, 0);
	}

	@Test
	void testGetAllOutputStreamList_SourcesByRefs_2streams() {
		setup.setChannelMap(List.of("0:1", "1:0"));
		final var outStreams = setup.getAllOutputStreamList(sourcesFiles);
		assertNotNull(outStreams);
		assertEquals(2, outStreams.size());
		final var channels0 = checkOutputAudioStream(outStreams.get(0), 0, 0, STEREO);
		assertEquals(2, channels0.size());
		checkOutputAudioStream(channels0.get(0), 0, 2, 0, 0);
		checkOutputAudioStream(channels0.get(1), 0, 2, 1, 1);

		final var channels1 = checkOutputAudioStream(outStreams.get(1), 0, 1, MONO);
		assertEquals(1, channels1.size());
		checkOutputAudioStream(channels1.get(0), 1, 0, 0, 0);
	}

	@Test
	void testGetAllOutputStreamList_PointAbsoluteSources_1stream_forceLayout() {
		setup.setChannelMap(List.of("1+2(downmix)"));
		final var outStreams = setup.getAllOutputStreamList(sourcesFiles);
		assertNotNull(outStreams);
		assertEquals(1, outStreams.size());
		final var channels = checkOutputAudioStream(outStreams.get(0), 0, 0, DOWNMIX);
		assertEquals(2, channels.size());
		checkOutputAudioStream(channels.get(0), 0, 1, 1, 0);
		checkOutputAudioStream(channels.get(1), 0, 2, 0, 1);
	}

	@Test
	void testGetAllOutputStreamList_SourcesByRefs_1stream_forceLayout() {
		setup.setChannelMap(List.of("0:1(downmix)"));
		final var outStreams = setup.getAllOutputStreamList(sourcesFiles);
		assertNotNull(outStreams);
		assertEquals(1, outStreams.size());
		final var channels = checkOutputAudioStream(outStreams.get(0), 0, 0, DOWNMIX);
		assertEquals(2, channels.size());
		checkOutputAudioStream(channels.get(0), 0, 2, 0, 0);
		checkOutputAudioStream(channels.get(1), 0, 2, 1, 1);
	}

	@Test
	void testGetAllOutputStreamList_SourcesByRefsChannels_1stream_forceLayout() {
		setup.setChannelMap(List.of("0:0:1+0:1:0(downmix)"));
		final var outStreams = setup.getAllOutputStreamList(sourcesFiles);
		assertNotNull(outStreams);
		assertEquals(1, outStreams.size());
		final var channels = checkOutputAudioStream(outStreams.get(0), 0, 0, DOWNMIX);
		assertEquals(2, channels.size());
		checkOutputAudioStream(channels.get(0), 0, 1, 1, 0);
		checkOutputAudioStream(channels.get(1), 0, 2, 0, 1);
	}

	@Test
	void testGetAllOutputStreamList_PointAbsoluteSources_ImpossibleStream() {
		setup.setChannelMap(List.of("2+3+4"));
		final var list = sourcesFiles;
		assertThrows(IllegalArgumentException.class, () -> setup.getAllOutputStreamList(list));
	}

	@Test
	void testGetAllOutputStreamList_SourcesByRefsChannels_ImpossibleStream() {
		setup.setChannelMap(List.of("0:0:1+0:1:0+1:0:0"));
		final var list = sourcesFiles;
		assertThrows(IllegalArgumentException.class, () -> setup.getAllOutputStreamList(list));
	}

	@Test
	void testGetAllOutputStreamList_PointAbsoluteSources_multipleFiles() {
		setup.setOutputFileIndexes(List.of(1, 0));
		setup.setChannelMap(List.of("1+2", "4"));
		final var outStreams = setup.getAllOutputStreamList(sourcesFiles);
		assertNotNull(outStreams);
		assertEquals(2, outStreams.size());
		final var channels0 = checkOutputAudioStream(outStreams.get(0), 1, 0, STEREO);
		assertEquals(2, channels0.size());
		checkOutputAudioStream(channels0.get(0), 0, 1, 1, 0);
		checkOutputAudioStream(channels0.get(1), 0, 2, 0, 1);

		final var channels1 = checkOutputAudioStream(outStreams.get(1), 0, 0, MONO);
		assertEquals(1, channels1.size());
		checkOutputAudioStream(channels1.get(0), 1, 0, 0, 0);
	}

	@Test
	void testGetAllOutputStreamList_SourcesByRefs_multipleFiles() {
		setup.setOutputFileIndexes(List.of(1, 0));
		setup.setChannelMap(List.of("0:1", "1:0"));
		final var outStreams = setup.getAllOutputStreamList(sourcesFiles);
		assertNotNull(outStreams);
		assertEquals(2, outStreams.size());
		final var channels0 = checkOutputAudioStream(outStreams.get(0), 1, 0, STEREO);
		assertEquals(2, channels0.size());
		checkOutputAudioStream(channels0.get(0), 0, 2, 0, 0);
		checkOutputAudioStream(channels0.get(1), 0, 2, 1, 1);

		final var channels1 = checkOutputAudioStream(outStreams.get(1), 0, 0, MONO);
		assertEquals(1, channels1.size());
		checkOutputAudioStream(channels1.get(0), 1, 0, 0, 0);
	}

	@Test
	void testGetAllOutputStreamList_SourcesByRefsChannels_multipleFiles() {
		setup.setOutputFileIndexes(List.of(1, 0));
		setup.setChannelMap(List.of("0:0:1+0:1:0", "1:0:0"));
		final var outStreams = setup.getAllOutputStreamList(sourcesFiles);
		assertNotNull(outStreams);
		assertEquals(2, outStreams.size());
		final var channels0 = checkOutputAudioStream(outStreams.get(0), 1, 0, STEREO);
		assertEquals(2, channels0.size());
		checkOutputAudioStream(channels0.get(0), 0, 1, 1, 0);
		checkOutputAudioStream(channels0.get(1), 0, 2, 0, 1);

		final var channels1 = checkOutputAudioStream(outStreams.get(1), 0, 0, MONO);
		assertEquals(1, channels1.size());
		checkOutputAudioStream(channels1.get(0), 1, 0, 0, 0);
	}

	@Test
	void testGetAllOutputStreamList_PointAbsoluteSources_multipleFiles_continueToTheLast() {
		setup.setOutputFileIndexes(List.of(1, 0));
		setup.setChannelMap(List.of("1+2", "4", "3"));
		final var outStreams = setup.getAllOutputStreamList(sourcesFiles);
		assertNotNull(outStreams);
		assertEquals(3, outStreams.size());
		final var channels0 = checkOutputAudioStream(outStreams.get(0), 1, 0, STEREO);
		assertEquals(2, channels0.size());
		checkOutputAudioStream(channels0.get(0), 0, 1, 1, 0);
		checkOutputAudioStream(channels0.get(1), 0, 2, 0, 1);

		final var channels1 = checkOutputAudioStream(outStreams.get(1), 0, 0, MONO);
		assertEquals(1, channels1.size());
		checkOutputAudioStream(channels1.get(0), 1, 0, 0, 0);

		final var channels2 = checkOutputAudioStream(outStreams.get(2), 0, 1, MONO);
		assertEquals(1, channels2.size());
		checkOutputAudioStream(channels2.get(0), 0, 2, 1, 0);
	}

	@Test
	void testGetAllOutputStreamList_SourcesByRefsChannels_syntaxError() {
		setup.setChannelMap(List.of("0:0:1:5"));
		assertThrows(IllegalArgumentException.class, () -> setup.getAllOutputStreamList(sourcesFiles));
	}

	static void checkOutputAudioStream(final OutputAudioChannel outChannel,
	                                   final int inFileIndex,
	                                   final int inStreamIndex,
	                                   final int chInIndex,
	                                   final int chOutIndex) {
		assertEquals(inFileIndex, outChannel.getInputAudioStream().getFileIndex());
		assertEquals(inStreamIndex, outChannel.getInputAudioStream().getStreamIndex());
		assertEquals(chInIndex, outChannel.getChInIndex().getPosInStream());
		assertEquals(chOutIndex, outChannel.getChOutIndex().getPosInStream());
	}

	static List<OutputAudioChannel> checkOutputAudioStream(final OutputAudioStream outStream,
	                                                       final int fileIndex,
	                                                       final int streamIndex,
	                                                       final ChannelLayout layout) {
		assertEquals(fileIndex, outStream.getFileIndex());
		assertEquals(streamIndex, outStream.getStreamIndex());
		assertEquals(layout, outStream.getLayout());
		return outStream.getChannels().stream().sorted().collect(toUnmodifiableList());
	}

}
