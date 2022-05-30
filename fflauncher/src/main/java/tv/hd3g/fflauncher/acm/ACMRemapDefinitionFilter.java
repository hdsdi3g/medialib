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

import static java.util.stream.Collectors.toUnmodifiableMap;

import java.util.List;
import java.util.Objects;

import tv.hd3g.fflauncher.filtering.AudioFilterChannelmap;
import tv.hd3g.fflauncher.filtering.Filter;

class ACMRemapDefinitionFilter extends ACMListIndexPositionHandler {
	private final OutputAudioStream outputAudioStream;
	private final InputAudioStream inputAudioStream;

	ACMRemapDefinitionFilter(final InputAudioStream inputAudioStream, final OutputAudioStream outputAudioStream) {
		this.inputAudioStream = Objects.requireNonNull(inputAudioStream);
		this.outputAudioStream = Objects.requireNonNull(outputAudioStream);

		final var notSameSource = outputAudioStream.getChannels().stream()
		        .anyMatch(c -> c.getInputAudioStream().equals(inputAudioStream) == false);
		if (notSameSource) {
			throw new IllegalArgumentException("Can't merge source streams for remap, outputs must only take from: "
			                                   + inputAudioStream);
		} else if (inputAudioStream.getLayout().getChannelSize() != outputAudioStream.getLayout().getChannelSize()) {
			throw new IllegalArgumentException("Incompablity layouts size with input (" +
			                                   inputAudioStream.getLayout().getChannelSize() + ") and output (" +
			                                   outputAudioStream.getLayout().getChannelSize() + ")");
		}
	}

	@Override
	public OutputAudioStream getLinkableOutStreamReference() {
		return outputAudioStream;
	}

	@Override
	public String toMapReferenceAsInput() {
		return "remap" + absolutePosIndex;
	}

	public Filter toFilter() {
		final var sourceLayout = inputAudioStream.getLayout();
		final var destLayout = outputAudioStream.getLayout();

		final var map = outputAudioStream.getChannels().stream()
		        .collect(toUnmodifiableMap(
		                outChannel -> destLayout.getChannelList().get(outChannel.getChOutIndex().getPosInStream()),
		                outChannel -> sourceLayout.getChannelList().get(outChannel.getChInIndex().getPosInStream())));

		final var filter = new AudioFilterChannelmap(destLayout, map).toFilter();
		filter.setSourceBlocks(List.of(inputAudioStream.toMapReferenceAsInput()));
		filter.setDestBlocks(List.of(toMapReferenceAsInput()));
		return filter;
	}
}