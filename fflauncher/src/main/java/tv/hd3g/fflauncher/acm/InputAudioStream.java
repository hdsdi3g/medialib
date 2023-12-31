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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import tv.hd3g.fflauncher.enums.ChannelLayout;
import tv.hd3g.ffprobejaxb.FFprobeJAXB;

/**
 * 0 is always the first !
 */
public class InputAudioStream extends ACMAudioStream {

	/**
	 * 0 is always the first !
	 */
	public InputAudioStream(final ChannelLayout layout, final int fileIndex, final int streamIndex) {
		super(layout, fileIndex, streamIndex);
	}

	@Override
	public String toMapReferenceAsInput() {
		return getFileIndex() + ":" + getStreamIndex();
	}

	@Override
	public String toString() {
		return toMapReferenceAsInput() + "(" + getLayout() + ")";
	}

	public static List<InputAudioStream> getListFromAnalysis(final FFprobeJAXB... sourcesAnalysis) {
		for (var pos = 0; pos < sourcesAnalysis.length; pos++) {
			Objects.requireNonNull(sourcesAnalysis[pos]);
		}
		return getListFromAnalysis(List.of(sourcesAnalysis));
	}

	public static List<InputAudioStream> getListFromAnalysis(final List<FFprobeJAXB> sourcesAnalysis) {
		final var allSourceStreams = new ArrayList<InputAudioStream>();
		for (var pos = 0; pos < sourcesAnalysis.size(); pos++) {
			final var analysis = sourcesAnalysis.get(pos);
			final var absoluteSourceIndex = pos;
			analysis.getAudioStreams()
					.sorted((l, r) -> Integer.compare(l.index(), r.index()))
					.map(as -> {
						final var layout = as.channelLayout();
						if (layout == null || layout.isEmpty()) {
							return new InputAudioStream(ChannelLayout.getByChannelSize(as.channels()),
									absoluteSourceIndex, as.index());
						}
						return new InputAudioStream(ChannelLayout.parse(layout), absoluteSourceIndex, as.index());
					})
					.forEach(allSourceStreams::add);
		}
		return Collections.unmodifiableList(allSourceStreams);
	}

	public static InputAudioStream getFromRelativeIndexes(final List<InputAudioStream> streamList,
														  final int fileIndex,
														  final int audioStreamRelativeIndex) {
		final var streamsInFile = streamList.stream()
				.filter(inStream -> inStream.getFileIndex() == fileIndex)
				.toList();
		for (var pos = 0; pos < streamsInFile.size(); pos++) {
			if (pos == audioStreamRelativeIndex) {
				return streamsInFile.get(pos);
			}
		}
		return null;
	}

	public static ACMSelectedInputChannel getFromAbsoluteIndex(final List<InputAudioStream> streamList,
															final int channelIndex) {
		var totalChannelCount = 0;
		for (final var inStream : streamList) {
			final var layout = inStream.getLayout();
			final var layoutSize = layout.getChannelSize();
			final var relativeChannelIndex = channelIndex - totalChannelCount;
			if (relativeChannelIndex < layoutSize) {
				return new ACMSelectedInputChannel(inStream,
						new InputAudioChannelSelector(relativeChannelIndex));
			}
			totalChannelCount += layoutSize;
		}
		return null;
	}

}