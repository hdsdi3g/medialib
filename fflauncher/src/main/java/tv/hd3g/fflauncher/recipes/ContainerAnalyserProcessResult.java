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
package tv.hd3g.fflauncher.recipes;

import static java.util.Collections.unmodifiableList;
import static tv.hd3g.fflauncher.ffprobecontainer.FFprobePictType.B;
import static tv.hd3g.fflauncher.ffprobecontainer.FFprobePictType.I;
import static tv.hd3g.fflauncher.ffprobecontainer.FFprobePictType.UNKNOWN;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import tv.hd3g.fflauncher.ffprobecontainer.FFprobeAudioFrame;
import tv.hd3g.fflauncher.ffprobecontainer.FFprobeAudioFrameConst;
import tv.hd3g.fflauncher.ffprobecontainer.FFprobePacket;
import tv.hd3g.fflauncher.ffprobecontainer.FFprobeResultSAX;
import tv.hd3g.fflauncher.ffprobecontainer.FFprobeVideoFrame;
import tv.hd3g.fflauncher.ffprobecontainer.FFprobeVideoFrameConst;

public record ContainerAnalyserProcessResult(List<FFprobePacket> packets,
											 List<FFprobeAudioFrame> audioFrames,
											 List<FFprobeVideoFrame> videoFrames,
											 FFprobeVideoFrameConst videoConst,
											 FFprobeAudioFrameConst audioConst,
											 List<FFprobeVideoFrameConst> olderVideoConsts,
											 List<FFprobeAudioFrameConst> olderAudioConsts,
											 String ffprobeCommandLine) {

	private static <T> boolean isNotNullAndNotEmpty(final List<T> list) {
		return Optional.ofNullable(list)
				.map(l -> l.isEmpty() == false)
				.orElse(false);
	}

	public boolean isEmpty() {
		return isNotNullAndNotEmpty(packets) == false
			   && isNotNullAndNotEmpty(audioFrames) == false
			   && isNotNullAndNotEmpty(videoFrames) == false
			   && videoConst == null
			   && audioConst == null
			   && isNotNullAndNotEmpty(olderVideoConsts) == false
			   && isNotNullAndNotEmpty(olderAudioConsts) == false;
	}

	/**
	 * @return empty if no video frames, or no GOPs (all frames are I or UNKNOWN)
	 */
	public List<GOPStatItem> extractGOPStats() {
		if (videoFrames == null
			|| videoFrames.isEmpty()
			|| videoFrames.stream().allMatch(f -> I.equals(f.pictType())
												  || UNKNOWN.equals(f.pictType()))) {
			return List.of();
		}

		return videoFrames.stream()
				.reduce(new ArrayList<List<FFprobeVideoFrame>>(),
						(list, f) -> {
							if (I.equals(f.pictType()) || list.isEmpty()) {
								list.add(new ArrayList<>(List.of(f)));
							} else {
								list.get(list.size() - 1).add(f);
							}
							return list;
						},
						(l, r) -> {
							l.addAll(r);
							return l;
						}).stream()
				.map(list -> {
					final var gopFrameCount = list.size();
					final var gopDataSize = list.stream()
							.mapToLong(f -> (long) f.frame().pktSize())
							.sum();
					final var bFramesCount = (int) list.stream()
							.filter(f -> B.equals(f.pictType()))
							.count();
					final var iFrameDataSize = list.get(0).frame().pktSize();
					final var bFramesDataSize = list.stream()
							.filter(f -> B.equals(f.pictType()))
							.mapToLong(f -> (long) f.frame().pktSize())
							.sum();

					return new GOPStatItem(
							gopFrameCount,
							gopDataSize,
							bFramesCount,
							iFrameDataSize,
							bFramesDataSize,
							unmodifiableList(list));
				})
				.toList();
	}

	public static ContainerAnalyserProcessResult importFromOffline(final InputStream ffprobeStdOut,
																   final String ffprobeCommandLine) {
		final var parser = new FFprobeResultSAX();
		parser.onProcessStart(ffprobeStdOut, null);
		parser.onClose(null);
		return parser.getResult(ffprobeCommandLine);
	}
}
