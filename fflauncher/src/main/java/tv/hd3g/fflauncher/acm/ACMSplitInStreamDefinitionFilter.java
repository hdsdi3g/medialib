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

import java.util.LinkedHashMap;
import java.util.Map;

import tv.hd3g.fflauncher.acm.OutputAudioStream.OutputAudioChannel;
import tv.hd3g.fflauncher.filtering.AudioFilterChannelsplit;
import tv.hd3g.fflauncher.filtering.Filter;

class ACMSplitInStreamDefinitionFilter {

	private final InputAudioStream inputAudioStream;
	private final Map<InputAudioChannelSelector, SplittedOut> splittedOut;

	ACMSplitInStreamDefinitionFilter(final OutputAudioChannel firstOutChannel,
									 final int firstSplittedOutAbsolutePosIndex) {
		inputAudioStream = firstOutChannel.getInputAudioStream();
		final var firstChInIndex = firstOutChannel.getChInIndex();
		splittedOut = new LinkedHashMap<>(Map.of(firstChInIndex,
				new SplittedOut(firstOutChannel, firstSplittedOutAbsolutePosIndex)));
	}

	class SplittedOut extends ACMListIndexPositionHandler {
		private final OutputAudioChannel outputAudioChannel;

		SplittedOut(final OutputAudioChannel outputAudioChannel, final int absolutePosIndex) {
			this.outputAudioChannel = outputAudioChannel;
			this.absolutePosIndex = absolutePosIndex;
			if (outputAudioChannel.getInputAudioStream().equals(inputAudioStream) == false) {
				throw new IllegalArgumentException("Can't mix inputAudioStream sources ("
												   + outputAudioChannel.getInputAudioStream()
												   + "/" + inputAudioStream + ")");
			}
		}

		@Override
		public String toMapReferenceAsInput() {
			return "split" + absolutePosIndex;
		}

		@Override
		public OutputAudioStream getLinkableOutStreamReference() {
			return outputAudioChannel.getOutputAudioStream();
		}

		InputAudioStream getInStream() {
			return inputAudioStream;
		}

		OutputAudioChannel getOutputAudioChannel() {
			return outputAudioChannel;
		}
	}

	@Override
	public String toString() {
		return inputAudioStream + "~>" + splittedOut;
	}

	InputAudioStream getInputAudioStream() {
		return inputAudioStream;
	}

	Map<InputAudioChannelSelector, SplittedOut> getSplittedOut() {
		return splittedOut;
	}

	Filter toFilter() {
		final var inputLayout = inputAudioStream.getLayout();
		final var inputLayoutChannelList = inputLayout.getChannelList();

		final var selectedChannels = splittedOut.keySet().stream()
				.sorted()
				.map(InputAudioChannelSelector::getPosInStream)
				.map(inputLayoutChannelList::get)
				.toList();

		final var filter = new AudioFilterChannelsplit(inputLayout, selectedChannels).toFilter();
		filter.getSourceBlocks().add(inputAudioStream.toMapReferenceAsInput());

		filter.getDestBlocks().addAll(splittedOut.keySet().stream()
				.sorted()
				.map(splittedOut::get)
				.map(SplittedOut::toMapReferenceAsInput)
				.toList());

		return filter;
	}
}