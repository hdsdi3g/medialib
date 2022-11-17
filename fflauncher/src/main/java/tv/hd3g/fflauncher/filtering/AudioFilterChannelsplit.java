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

import tv.hd3g.fflauncher.enums.Channel;
import tv.hd3g.fflauncher.enums.ChannelLayout;

/**
 * See https://ffmpeg.org/ffmpeg-filters.html#channelsplit
 */
public class AudioFilterChannelsplit implements FilterSupplier {

	private final ChannelLayout sourceChannelLayout;
	private final List<Channel> sourceChannelList;
	private final List<Channel> selectedChannels;

	public AudioFilterChannelsplit(final ChannelLayout sourceChannelLayout, final List<Channel> selectedChannels) {
		this.sourceChannelLayout = sourceChannelLayout;
		this.selectedChannels = selectedChannels;
		sourceChannelList = sourceChannelLayout.getChannelList();

		if (selectedChannels.stream().allMatch(sourceChannelList::contains) == false) {
			throw new IllegalArgumentException("Invalid selected channel(s) from source. You can choose only on "
											   + sourceChannelList);
		}
	}

	@Override
	public Filter toFilter() {
		return new Filter("channelsplit",
				new FilterArgument("channel_layout", sourceChannelLayout),
				new FilterArgument("channels", selectedChannels, "+"));
	}

}
