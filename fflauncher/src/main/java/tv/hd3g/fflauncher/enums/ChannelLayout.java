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
package tv.hd3g.fflauncher.enums;

import static java.util.stream.Collectors.toUnmodifiableMap;
import static tv.hd3g.fflauncher.enums.Channel.BC;
import static tv.hd3g.fflauncher.enums.Channel.BL;
import static tv.hd3g.fflauncher.enums.Channel.BR;
import static tv.hd3g.fflauncher.enums.Channel.DL;
import static tv.hd3g.fflauncher.enums.Channel.DR;
import static tv.hd3g.fflauncher.enums.Channel.FC;
import static tv.hd3g.fflauncher.enums.Channel.FL;
import static tv.hd3g.fflauncher.enums.Channel.FLC;
import static tv.hd3g.fflauncher.enums.Channel.FR;
import static tv.hd3g.fflauncher.enums.Channel.FRC;
import static tv.hd3g.fflauncher.enums.Channel.LFE;
import static tv.hd3g.fflauncher.enums.Channel.SL;
import static tv.hd3g.fflauncher.enums.Channel.SR;
import static tv.hd3g.fflauncher.enums.Channel.TBC;
import static tv.hd3g.fflauncher.enums.Channel.TBL;
import static tv.hd3g.fflauncher.enums.Channel.TBR;
import static tv.hd3g.fflauncher.enums.Channel.TFC;
import static tv.hd3g.fflauncher.enums.Channel.TFL;
import static tv.hd3g.fflauncher.enums.Channel.TFR;
import static tv.hd3g.fflauncher.enums.Channel.WL;
import static tv.hd3g.fflauncher.enums.Channel.WR;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Get by ffmpeg -layouts
 */
public enum ChannelLayout {

	MONO(List.of(FC)),
	STEREO(List.of(FL, FR)),
	CH2_1(List.of(FL, FR, LFE)),
	CH3_0(List.of(FL, FR, FC)),
	CH3_0_BACK(List.of(FL, FR, BC)),
	CH4_0(List.of(FL, FR, FC, BC)),
	QUAD(List.of(FL, FR, BL, BR)),
	QUAD_SIDE(List.of(FL, FR, SL, SR)),
	CH3_1(List.of(FL, FR, FC, LFE)),
	CH5_0(List.of(FL, FR, FC, BL, BR)),
	CH5_0_SIDE(List.of(FL, FR, FC, SL, SR)),
	CH4_1(List.of(FL, FR, FC, LFE, BC)),
	CH5_1(List.of(FL, FR, FC, LFE, BL, BR)),
	CH5_1_SIDE(List.of(FL, FR, FC, LFE, SL, SR)),
	CH6_0(List.of(FL, FR, FC, BC, SL, SR)),
	CH6_0_FRONT(List.of(FL, FR, FLC, FRC, SL, SR)),
	HEXAGONAL(List.of(FL, FR, FC, BL, BR, BC)),
	CH6_1(List.of(FL, FR, FC, LFE, BC, SL, SR)),
	CH6_1_BACK(List.of(FL, FR, FC, LFE, BL, BR, BC)),
	CH6_1_FRONT(List.of(FL, FR, LFE, FLC, FRC, SL, SR)),
	CH7_0(List.of(FL, FR, FC, BL, BR, SL, SR)),
	CH7_0_FRONT(List.of(FL, FR, FC, FLC, FRC, SL, SR)),
	CH7_1(List.of(FL, FR, FC, LFE, BL, BR, SL, SR)),
	CH7_1_WIDE(List.of(FL, FR, FC, LFE, BL, BR, FLC, FRC)),
	CH7_1_WIDE_SIDE(List.of(FL, FR, FC, LFE, FLC, FRC, SL, SR)),
	OCTAGONAL(List.of(FL, FR, FC, BL, BR, BC, SL, SR)),
	HEXADECAGONAL(List.of(FL, FR, FC, BL, BR, BC, SL, SR, TFL, TFC, TFR, TBL, TBC, TBR, WL, WR)),
	DOWNMIX(List.of(DL, DR));

	private static final Map<String, ChannelLayout> layoutByName;

	static {
		layoutByName = Stream.of(ChannelLayout.values())
		        .collect(toUnmodifiableMap(ChannelLayout::toString, cl -> cl));
	}

	public static ChannelLayout parse(final String layout) {
		return layoutByName.get(layout.toLowerCase());
	}

	private final List<Channel> channelList;

	private ChannelLayout(final List<Channel> channelList) {
		this.channelList = channelList;
	}

	public List<Channel> getChannelList() {
		return channelList;
	}

	public int getChannelSize() {
		return getChannelList().size();
	}

	public boolean isMonoLayout() {
		return getChannelSize() == 1;
	}

	public static ChannelLayout getByChannelSize(final int channelSize) {
		switch (channelSize) {
		case 1:
			return MONO;
		case 2:
			return STEREO;
		default:
			throw new IllegalArgumentException("Can't assert certainty the channel layout from size " + channelSize);
		}
	}

	@Override
	public String toString() {
		switch (this) {
		case MONO:
			return "mono";
		case STEREO:
			return "stereo";
		case CH2_1:
			return "2.1";
		case CH3_0:
			return "3.0";
		case CH3_0_BACK:
			return "3.0(back)";
		case CH4_0:
			return "4.0";
		case QUAD:
			return "quad";
		case QUAD_SIDE:
			return "quad(side)";
		case CH3_1:
			return "3.1";
		case CH5_0:
			return "5.0";
		case CH5_0_SIDE:
			return "5.0(side)";
		case CH4_1:
			return "4.1";
		case CH5_1:
			return "5.1";
		case CH5_1_SIDE:
			return "5.1(side)";
		case CH6_0:
			return "6.0";
		case CH6_0_FRONT:
			return "6.0(front)";
		case HEXAGONAL:
			return "hexagonal";
		case CH6_1:
			return "6.1";
		case CH6_1_BACK:
			return "6.1(back)";
		case CH6_1_FRONT:
			return "6.1(front)";
		case CH7_0:
			return "7.0";
		case CH7_0_FRONT:
			return "7.0(front)";
		case CH7_1:
			return "7.1";
		case CH7_1_WIDE:
			return "7.1(wide)";
		case CH7_1_WIDE_SIDE:
			return "7.1(wide-side)";
		case OCTAGONAL:
			return "octagonal";
		case HEXADECAGONAL:
			return "hexadecagonal";
		case DOWNMIX:
			return "downmix";
		default:
			throw new IllegalArgumentException("No channel-layout name");
		}
	}

}
