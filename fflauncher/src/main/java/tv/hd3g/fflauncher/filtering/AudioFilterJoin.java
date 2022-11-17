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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import tv.hd3g.fflauncher.enums.Channel;
import tv.hd3g.fflauncher.enums.ChannelLayout;

/**
 * See https://ffmpeg.org/ffmpeg-filters.html#join
 */
public class AudioFilterJoin implements FilterSupplier {

	private final int inputs;
	private final ChannelLayout channelLayout;
	private final Map<Channel, String> sourceByDestChannel;
	private final List<Channel> destChannelList;

	/**
	 * @param sourceByDestChannel Source: input_idx.in_channel-out_channel
	 *        input_idx is relative by Filter sources !
	 */
	public AudioFilterJoin(final int inputs,
	                       final ChannelLayout channelLayout,
	                       final Map<Channel, String> sourceByDestChannel) {
		this.inputs = inputs;
		this.channelLayout = channelLayout;
		this.sourceByDestChannel = sourceByDestChannel;
		destChannelList = channelLayout.getChannelList();

		if (sourceByDestChannel.size() != destChannelList.size()) {
			throw new IllegalArgumentException("Invalid channel count (layout <-> map)");
		} else if (destChannelList.stream().allMatch(sourceByDestChannel::containsKey) == false) {
			throw new IllegalArgumentException("Missing channel(s) in map. You need only " + destChannelList);
		}
	}

	@Override
	public Filter toFilter() {
		final var map = destChannelList.stream().map(channel -> {
			final var dest = sourceByDestChannel.get(channel);
			return dest.replace(':', '.') + "-" + channel.toString();
		}).collect(Collectors.joining("|"));

		return new Filter("join",
		        new FilterArgument("inputs", inputs),
		        new FilterArgument("channel_layout", channelLayout),
		        new FilterArgument("map", map));
	}

}
