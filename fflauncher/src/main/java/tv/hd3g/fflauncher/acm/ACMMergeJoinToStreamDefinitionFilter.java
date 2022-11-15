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

import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.joining;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import tv.hd3g.fflauncher.enums.Channel;
import tv.hd3g.fflauncher.filtering.AudioFilterAmerge;
import tv.hd3g.fflauncher.filtering.AudioFilterJoin;
import tv.hd3g.fflauncher.filtering.Filter;

class ACMMergeJoinToStreamDefinitionFilter extends ACMListIndexPositionHandler {

	private final List<ACMExportableMapReference> inputs;
	private final OutputAudioStream toOutput;

	ACMMergeJoinToStreamDefinitionFilter(final List<ACMExportableMapReference> inputs,
										 final OutputAudioStream toOutput) {
		this.inputs = Objects.requireNonNull(inputs);
		this.toOutput = Objects.requireNonNull(toOutput);
	}

	@Override
	public String toMapReferenceAsInput() {
		return "mergjoin" + absolutePosIndex;
	}

	@Override
	public OutputAudioStream getLinkableOutStreamReference() {
		return toOutput;
	}

	@Override
	public String toString() {
		final var inRefs = inputs.stream()
				.map(ACMExportableMapReference::toMapReferenceAsInput)
				.collect(joining("+"));
		return inRefs + ">~" + toMapReferenceAsInput() + "(" + toOutput.getLayout() + ")";
	}

	List<ACMExportableMapReference> getInputs() {
		return inputs;
	}

	private void internalCheck() {
		final var inChCount = inputs.size();
		final var outChCount = toOutput.getLayout().getChannelList().size();
		if (inChCount == 0) {
			throw new IllegalArgumentException("No inputs for filter " + toOutput.toMapReferenceAsInput());
		} else if (inChCount != outChCount) {
			throw new IllegalArgumentException("Invalid input stream count (" +
											   inChCount + ")" + " for filter "
											   + toOutput.toMapReferenceAsInput() + " with expected "
											   + outChCount + " audio channels");
		}
	}

	/**
	 * Simple ffmpeg approach
	 */
	public Filter toAmergeFilter() {
		internalCheck();
		final var filter = new AudioFilterAmerge(inputs.size()).toFilter();
		filter.setSourceBlocks(inputs.stream()
				.map(ACMExportableMapReference::toMapReferenceAsInput)
				.toList());
		filter.setDestBlocks(List.of(toMapReferenceAsInput()));
		return filter;
	}

	/**
	 * Better ffmpeg approach than amerge with set ChannelLayout and explict channel shuffling
	 */
	public Filter toJoinFilter() {
		internalCheck();
		final var layout = toOutput.getLayout();

		final var sourceByDestChannel = new LinkedHashMap<Channel, String>(inputs.size());
		final var channelList = layout.getChannelList();
		for (var pos = 0; pos < channelList.size(); pos++) {
			sourceByDestChannel.put(channelList.get(pos), pos + ".0");
		}

		final var filter = new AudioFilterJoin(inputs.size(), layout, unmodifiableMap(sourceByDestChannel)).toFilter();
		filter.setSourceBlocks(inputs.stream()
				.map(ACMExportableMapReference::toMapReferenceAsInput)
				.toList());
		filter.setDestBlocks(List.of(toMapReferenceAsInput()));
		return filter;
	}

}