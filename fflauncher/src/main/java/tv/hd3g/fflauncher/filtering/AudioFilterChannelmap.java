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
package tv.hd3g.fflauncher.filtering;

import static java.util.stream.Collectors.joining;

import java.util.Map;

import tv.hd3g.fflauncher.enums.Channel;
import tv.hd3g.fflauncher.enums.ChannelLayout;

/**
 * See https://ffmpeg.org/ffmpeg-filters.html#channelmap
 */
public class AudioFilterChannelmap implements AudioFilterSupplier {

	private final ChannelLayout destChannelLayout;
	private final Map<Channel, Channel> channelMap;

	/**
	 * @param channelMap output -> input
	 */
	public AudioFilterChannelmap(final ChannelLayout destChannelLayout, final Map<Channel, Channel> channelMap) {
		this.destChannelLayout = destChannelLayout;
		this.channelMap = channelMap;
		final var destChList = destChannelLayout.getChannelList();
		final var invalidMapEntry = channelMap.keySet().stream()
				.anyMatch(c -> destChList.contains(c) == false);
		if (invalidMapEntry) {
			throw new IllegalArgumentException("Invalid channelMap ("
											   + channelMap + "), it contain missing channels from "
											   + destChannelLayout);
		}
	}

	@Override
	public Filter toFilter() {
		final var map = destChannelLayout.getChannelList().stream()
				.filter(channelMap::containsKey)
				.map(destCh -> channelMap.get(destCh) + "-" + destCh)
				.collect(joining("|"));
		return new Filter("channelmap",
				new FilterArgument("map", map),
				new FilterArgument("channel_layout", destChannelLayout));
	}
}
