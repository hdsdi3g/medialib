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

import static java.lang.Integer.parseInt;
import static java.util.Collections.unmodifiableList;
import static tv.hd3g.fflauncher.enums.SourceNotFoundPolicy.ERROR;
import static tv.hd3g.fflauncher.enums.SourceNotFoundPolicy.REMOVE_OUT_STREAM;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import tv.hd3g.fflauncher.acm.InputAudioStream.SelectedInputChannel;
import tv.hd3g.fflauncher.enums.ChannelLayout;
import tv.hd3g.fflauncher.enums.SourceNotFoundPolicy;
import tv.hd3g.fflauncher.enums.SourceNotFoundPolicy.SourceNotFoundException;
import tv.hd3g.ffprobejaxb.FFprobeJAXB;

public class AudioChannelManipulationSetup {

	static final Pattern extractFromParentheses = Pattern.compile("\\(([^)]+)\\)");

	private List<String> channelMap;
	private List<Integer> outputFileIndexes;
	private SourceNotFoundPolicy notFound;

	/**
	 * 0 is always the first channel/stream.
	 * One entry = one output stream.
	 * "+" separate channels in stream. Spaces will be ignored.
	 * Each stream can set a ffmpeg channel layout (mono, stereo, 5.1, ...) in parentheses "(" and ")" like:
	 * - "0+1(stereo)"
	 * - "0+1+2+3+4+5(5.1)"
	 * You MUST set a channel layout if the channel count is different from 1 (mono), 2 (stereo), 6 (5.1).
	 * @param channelMap entry like:
	 *        - "0+1" absolute source channel selection. Shuffle like "1+1"/"1+0".
	 *        Each number point to an absolute source channel, ordered by files and streams.
	 *        - "0:1:0+0:1:1" Specific source channel, with "A:B:C":
	 *        A = source file index
	 *        B = source stream index in selected file A (0 is the first audio stream, ignore video/data streams)
	 *        C = source channel index in selected stream B
	 *        - "0:1" take the full stream for simple mapping.
	 *        No channel operations here. With "A:B" like just above.
	 *        - "0:1(5.1(side))" take the full stream for mapping with layout change (here set to "5.1(side)")
	 */
	public void setChannelMap(final List<String> channelMap) {
		this.channelMap = channelMap;
	}

	public List<String> getChannelMap() {
		return channelMap;
	}

	public SourceNotFoundPolicy getNotFound() {
		return notFound;
	}

	public void setNotFound(final SourceNotFoundPolicy notFound) {
		this.notFound = notFound;
	}

	public List<Integer> getOutputFileIndexes() {
		return outputFileIndexes;
	}

	/**
	 * By default, all is mapped to the first (0) output file.
	 * outputFileIndexes miss some channelMap ref, the mapped output file will be the last set.
	 * @param outputFileIndexes ["Absolute stream0" mapped to # output file, "Absolute stream1" mapped to # output file, ...]
	 */
	public void setOutputFileIndexes(final List<Integer> outputFileIndexes) {
		this.outputFileIndexes = outputFileIndexes;
	}

	private class SetupBuilder {
		final List<InputAudioStream> inputStreams;
		final List<OutputAudioStream> outputStreams;
		final HashMap<Integer, Integer> relativeOutStrIdxByOutFileIdx;

		SetupBuilder(final List<FFprobeJAXB> sourcesAnalysis) {
			inputStreams = InputAudioStream.getListFromAnalysis(sourcesAnalysis);
			outputStreams = new ArrayList<>();
			relativeOutStrIdxByOutFileIdx = new HashMap<>();
		}

		private class ChannelMap {
			final String entry;
			final int outputStreamAbsoluteIndex;
			final int outputFileIndex;

			ChannelLayout outputLayout;
			int relativeOutStrmIdx;
			String outputStreamTopologyRaw;

			ChannelMap(final String entry) {
				this.entry = entry;
				outputStreamAbsoluteIndex = outputStreams.size();

				if (outputStreamAbsoluteIndex < outputFileIndexes.size()) {
					outputFileIndex = outputFileIndexes.get(outputStreamAbsoluteIndex);
				} else if (outputFileIndexes.isEmpty()) {
					outputFileIndex = 0;
				} else {
					outputFileIndex = outputFileIndexes.get(outputFileIndexes.size() - 1);
				}
			}

			/**
			 * All channels map entry, like "0:2" (stream2 in file0)
			 */
			void mapFullStream() {
				final var sourceFileIndex = Integer.parseInt(outputStreamTopologyRaw.split(":")[0]);
				final var relativeSourceStreamIndex = Integer.parseInt(outputStreamTopologyRaw.split(":")[1]);
				final var inputAudioStream = InputAudioStream.getFromRelativeIndexes(inputStreams,
				        sourceFileIndex,
				        relativeSourceStreamIndex);
				if (inputAudioStream == null) {
					applySourceNotFoundBehavior("Can't found input channel by file/stream indexes: "
					                            + sourceFileIndex + "/" + relativeSourceStreamIndex);
					return;
				}

				outputLayout = Optional.ofNullable(outputLayout).orElse(inputAudioStream.getLayout());
				final var outputStream = new OutputAudioStream(outputLayout, outputFileIndex,
				        relativeOutStrmIdx);

				final var chSize = inputAudioStream.getLayout().getChannelSize();
				for (var pos = 0; pos < chSize; pos++) {
					/**
					 * Map all In channels
					 */
					outputStream.mapChannel(inputAudioStream, new InputAudioChannelSelector(pos));
				}
				outputStreams.add(outputStream);
			}

			/**
			 * Try to search absolute channel in input streams
			 * @return true for cancel out stream
			 */
			boolean mapSingleSourceAbsoluteChannel(final int absoluteInputChannel,
			                                       final List<SelectedInputChannel> selectedInputChannels) {
				final var selected = InputAudioStream.getFromAbsoluteIndex(inputStreams,
				        absoluteInputChannel);
				if (selected == null) {
					return applySourceNotFoundBehavior("Can't found absolute input channel index: "
					                                   + absoluteInputChannel);
				}
				final var inputStream = selected.getInputAudioStream();
				final var channelSelector = selected.getChannelSelector();
				selectedInputChannels.add(inputStream.new SelectedInputChannel(inputStream,
				        channelSelector));
				return false;
			}

			boolean applySourceNotFoundBehavior(final String message) {
				if (notFound == ERROR) {
					throw new SourceNotFoundException(message);
				}
				return notFound == REMOVE_OUT_STREAM;
			}

			/**
			 * Search relative channel in input streams
			 * @return true for cancel out stream
			 */
			boolean mapSingleSourceRelativeChannel(final String absoluteInputRaw,
			                                       final List<SelectedInputChannel> selectedInputChannels) {
				final var absoluteInput = absoluteInputRaw.split(":");
				if (absoluteInput.length != 3) {
					throw new IllegalArgumentException("Invalid channel map entry: " + absoluteInputRaw);
				}
				final var sourceFileIndex = parseInt(absoluteInput[0]);
				final var relativeSourceStreamIndex = parseInt(absoluteInput[1]);
				final var channelSelector = new InputAudioChannelSelector(parseInt(absoluteInput[2]));
				final var inputStream = InputAudioStream.getFromRelativeIndexes(inputStreams,
				        sourceFileIndex,
				        relativeSourceStreamIndex);
				if (inputStream == null) {
					return applySourceNotFoundBehavior("Can't found file/stream index: "
					                                   + sourceFileIndex + "/"
					                                   + relativeSourceStreamIndex);
				}
				selectedInputChannels.add(inputStream.new SelectedInputChannel(inputStream,
				        channelSelector));
				return false;
			}

			void mapSingleChannels() {
				final var selectedInputChannels = new ArrayList<SelectedInputChannel>();
				final var outputChannelTopologyRaw = outputStreamTopologyRaw.split("\\+");
				/**
				 * For each source channel
				 */
				for (var posOutCh = 0; posOutCh < outputChannelTopologyRaw.length; posOutCh++) {
					final var absoluteInputRaw = outputChannelTopologyRaw[posOutCh];
					boolean isStop;
					try {
						isStop = mapSingleSourceAbsoluteChannel(parseInt(absoluteInputRaw),
						        selectedInputChannels);
					} catch (final NumberFormatException e) {
						isStop = mapSingleSourceRelativeChannel(absoluteInputRaw,
						        selectedInputChannels);
					}
					if (isStop) {
						return;
					}
				}

				if (outputLayout == null) {
					outputLayout = ChannelLayout.getByChannelSize(selectedInputChannels.size());
				}
				final var outputStream = new OutputAudioStream(outputLayout, outputFileIndex, relativeOutStrmIdx);
				selectedInputChannels.forEach(selectedInput -> outputStream
				        .mapChannel(selectedInput.getInputAudioStream(), selectedInput.getChannelSelector()));
				outputStreams.add(outputStream);
			}

			/**
			 * @return true for cancel out stream
			 */
			void extractMap() {
				final var layoutMatcher = extractFromParentheses.matcher(entry);
				if (layoutMatcher.find()) {
					final var rawLayout = layoutMatcher.group();
					outputLayout = ChannelLayout.parse(rawLayout.substring(1, rawLayout.length() - 1));
				}

				relativeOutStrmIdx = relativeOutStrIdxByOutFileIdx
				        .getOrDefault(outputFileIndex, -1) + 1;
				relativeOutStrIdxByOutFileIdx.put(outputFileIndex, relativeOutStrmIdx);

				outputStreamTopologyRaw = layoutMatcher.replaceAll("");
				if (outputStreamTopologyRaw.indexOf('+') == -1
				    && outputStreamTopologyRaw.split(":").length == 2) {
					mapFullStream();
				} else {
					mapSingleChannels();
				}
			}

		}

		private void onChannelMapEntry(final String entry) {
			new ChannelMap(entry).extractMap();
		}

		List<OutputAudioStream> build() {
			channelMap.stream()
			        .map(entry -> entry.trim().replace(" ", ""))
			        .filter(entry -> entry.isEmpty() == false)
			        .forEach(this::onChannelMapEntry);
			return unmodifiableList(outputStreams);
		}
	}

	public List<OutputAudioStream> getAllOutputStreamList(final List<FFprobeJAXB> sourcesAnalysis) {
		Objects.requireNonNull(channelMap);
		if (notFound == null) {
			notFound = ERROR;
		}
		if (outputFileIndexes == null) {
			outputFileIndexes = List.of();
		}
		if (channelMap.isEmpty()) {
			return List.of();
		}

		return new SetupBuilder(sourcesAnalysis).build();
	}

}
