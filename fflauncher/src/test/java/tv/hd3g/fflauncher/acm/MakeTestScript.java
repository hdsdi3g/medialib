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

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.WRITE;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tv.hd3g.fflauncher.acm.InputAudioChannelSelector.IN_CH0;
import static tv.hd3g.fflauncher.acm.InputAudioChannelSelector.IN_CH1;
import static tv.hd3g.fflauncher.acm.InputAudioChannelSelector.IN_CH2;
import static tv.hd3g.fflauncher.acm.InputAudioChannelSelector.IN_CH3;
import static tv.hd3g.fflauncher.enums.ChannelLayout.CH4_0;
import static tv.hd3g.fflauncher.enums.ChannelLayout.MONO;
import static tv.hd3g.fflauncher.enums.ChannelLayout.STEREO;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import tv.hd3g.fflauncher.FFmpeg;
import tv.hd3g.processlauncher.cmdline.Parameters;

class MakeTestScript {

	@Test
	void writeScript() throws IOException {
		final var outFile = new File("target/acm-script.bash").toPath();
		outFile.toFile().delete();
		final var content = new ArrayList<String>();

		content.add("#!/bin/bash");
		content.add("# FFlauncher ACM demo script");
		content.add("# you needs ffmpeg in your PATH");
		content.add("set -eu");
		content.add("");

		content.add("# Create initial test files");
		IntStream.range(0, 4).forEach(pos -> content.add(createAudioGeneratedFile(pos)));
		final var inStreams = IntStream.range(0, 8)
		        .mapToObj(pos -> new InputAudioStream(MONO, pos, 0))
		        .collect(toUnmodifiableList());

		content.add("# createMerge");
		final var fileToSplit = createMerge(content, inStreams);
		content.add("# createSplit");
		createSplit(content, fileToSplit);
		content.add("# createMultipleMerge");
		final var fileToSplitMerge = createMultipleMerge(content, inStreams);
		content.add("# createJoin");
		createJoin(content, inStreams);
		content.add("# createMultipleJoin");
		createMultipleJoin(content, inStreams);
		content.add("# createMultipleSplitMerge");
		final var fileToShuffle = createMultipleSplitMerge(content, fileToSplitMerge);
		content.add("# createShuffle");
		createShuffle(content, fileToShuffle);

		content.add("");
		content.add("echo \"Done\"");
		content.add("");

		Files.writeString(outFile, content.stream().collect(joining("\n")), UTF_8, WRITE, CREATE_NEW);
		assertTrue(outFile.toFile().exists());
	}

	static String createAudioGeneratedFile(final int pos) {
		final var fq = pos + 1;
		return "ffmpeg -y -f lavfi -i sine=frequency=" + fq
		       + "000:sample_rate=48000:duration=1 -codec:a pcm_s16le -f wav "
		       + fq + "000hz.wav";
	}

	static String createMerge(final ArrayList<String> content, final List<InputAudioStream> inStreams) {
		final var outStreamSimpleStereo = new OutputAudioStream(STEREO, 0, 0)
		        .mapChannel(inStreams.get(0), IN_CH0)
		        .mapChannel(inStreams.get(1), IN_CH0);
		final var outFile = "1000L+2000R.mov";
		final var acm = new AudioChannelManipulation(List.of(outStreamSimpleStereo));

		final var ffmpeg = new FFmpeg("ffmpeg", new Parameters());
		ffmpeg.addSimpleInputSource("1000hz.wav");
		ffmpeg.addSimpleInputSource("2000hz.wav");
		pushFFmpeg(content, ffmpeg, outFile, acm, false);
		return outFile;
	}

	static String createJoin(final ArrayList<String> content, final List<InputAudioStream> inStreams) {
		final var outStreamSimpleStereo = new OutputAudioStream(STEREO, 0, 0)
		        .mapChannel(inStreams.get(0), IN_CH0)
		        .mapChannel(inStreams.get(1), IN_CH0);
		final var outFile = "1000L+2000R.mov";
		final var acm = new AudioChannelManipulation(List.of(outStreamSimpleStereo));

		final var ffmpeg = new FFmpeg("ffmpeg", new Parameters());
		ffmpeg.addSimpleInputSource("1000hz.wav");
		ffmpeg.addSimpleInputSource("2000hz.wav");
		pushFFmpeg(content, ffmpeg, "JOIN" + outFile, acm, true);
		return outFile;
	}

	static void createSplit(final ArrayList<String> content, final String inFile) {
		final var inStream = new InputAudioStream(STEREO, 0, 0);
		final var outStream0 = new OutputAudioStream(MONO, 0, 0).mapChannel(inStream, IN_CH1);
		final var outStream1 = new OutputAudioStream(MONO, 0, 1).mapChannel(inStream, IN_CH0);
		final var outFile = "2000,1000.mov";
		final var acm = new AudioChannelManipulation(List.of(outStream0, outStream1));

		final var ffmpeg = new FFmpeg("ffmpeg", new Parameters());
		ffmpeg.addSimpleInputSource(inFile);
		pushFFmpeg(content, ffmpeg, outFile, acm, false);
	}

	static String createMultipleMerge(final ArrayList<String> content, final List<InputAudioStream> inStreams) {
		final var outStreamSimpleStereo0 = new OutputAudioStream(STEREO, 0, 0)
		        .mapChannel(inStreams.get(0), IN_CH0)
		        .mapChannel(inStreams.get(1), IN_CH0);
		final var outStreamSimpleStereo1 = new OutputAudioStream(STEREO, 0, 1)
		        .mapChannel(inStreams.get(2), IN_CH0)
		        .mapChannel(inStreams.get(3), IN_CH0);
		final var outFile = "1000+2000,3000+4000.mov";
		final var acm = new AudioChannelManipulation(List.of(outStreamSimpleStereo0, outStreamSimpleStereo1));

		final var ffmpeg = new FFmpeg("ffmpeg", new Parameters());
		ffmpeg.addSimpleInputSource("1000hz.wav");
		ffmpeg.addSimpleInputSource("2000hz.wav");
		ffmpeg.addSimpleInputSource("3000hz.wav");
		ffmpeg.addSimpleInputSource("4000hz.wav");
		pushFFmpeg(content, ffmpeg, outFile, acm, false);
		return outFile;
	}

	static void createMultipleJoin(final ArrayList<String> content, final List<InputAudioStream> inStreams) {
		final var outStreamSimpleStereo0 = new OutputAudioStream(STEREO, 0, 0)
		        .mapChannel(inStreams.get(0), IN_CH0)
		        .mapChannel(inStreams.get(1), IN_CH0);
		final var outStreamSimpleStereo1 = new OutputAudioStream(STEREO, 0, 1)
		        .mapChannel(inStreams.get(2), IN_CH0)
		        .mapChannel(inStreams.get(3), IN_CH0);
		final var outFile = "1000+2000,3000+4000.mov";
		final var acm = new AudioChannelManipulation(List.of(outStreamSimpleStereo0, outStreamSimpleStereo1));

		final var ffmpeg = new FFmpeg("ffmpeg", new Parameters());
		ffmpeg.addSimpleInputSource("1000hz.wav");
		ffmpeg.addSimpleInputSource("2000hz.wav");
		ffmpeg.addSimpleInputSource("3000hz.wav");
		ffmpeg.addSimpleInputSource("4000hz.wav");
		pushFFmpeg(content, ffmpeg, "JOIN" + outFile, acm, true);
	}

	static String createMultipleSplitMerge(final ArrayList<String> content, final String fileToSplitMerge) {
		final var inStream0 = new InputAudioStream(STEREO, 0, 0);
		final var inStream1 = new InputAudioStream(STEREO, 0, 1);
		final var outStreamSimpleQuad = new OutputAudioStream(CH4_0, 0, 0)
		        .mapChannel(inStream0, IN_CH0)
		        .mapChannel(inStream0, IN_CH1)
		        .mapChannel(inStream1, IN_CH0)
		        .mapChannel(inStream1, IN_CH1);

		final var outFile = "1000+2000+3000+4000.mov";
		final var acm = new AudioChannelManipulation(List.of(outStreamSimpleQuad));

		final var ffmpeg = new FFmpeg("ffmpeg", new Parameters());
		ffmpeg.addSimpleInputSource(fileToSplitMerge);
		pushFFmpeg(content, ffmpeg, outFile, acm, false);
		return outFile;
	}

	static void createShuffle(final ArrayList<String> content, final String fileToShuffle) {
		final var inStream0 = new InputAudioStream(CH4_0, 0, 0);
		final var outStreamSimpleQuad = new OutputAudioStream(CH4_0, 0, 0)
		        .mapChannel(inStream0, IN_CH3)
		        .mapChannel(inStream0, IN_CH2)
		        .mapChannel(inStream0, IN_CH1)
		        .mapChannel(inStream0, IN_CH0);

		final var outFile = "4000+3000+2000+1000.mov";
		final var acm = new AudioChannelManipulation(List.of(outStreamSimpleQuad));

		final var ffmpeg = new FFmpeg("ffmpeg", new Parameters());
		ffmpeg.addSimpleInputSource(fileToShuffle);
		pushFFmpeg(content, ffmpeg, outFile, acm, false);
	}

	static void pushFFmpeg(final ArrayList<String> content,
	                       final FFmpeg ffmpeg,
	                       final String outFile,
	                       final AudioChannelManipulation acm,
	                       final boolean useJoinInsteadOfMerge) {
		acm.getFilterChains(useJoinInsteadOfMerge).pushFilterChainTo("-filter_complex", ffmpeg);
		ffmpeg.setOverwriteOutputFiles();
		ffmpeg.getInternalParameters().addAllFrom(acm.getMapParameters().get(0));
		ffmpeg.addAudioCodecName("pcm_s16le", -1);
		ffmpeg.addSimpleOutputDestination(outFile);
		content.add(ffmpeg.getReadyToRunParameters().exportToExternalCommandLine("ffmpeg"));
	}

}
