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
 * Copyright (C) hdsdi3g for hd3g.tv 2018
 *
 */
package tv.hd3g.fflauncher.enums;

/**
 * Get by ffmpeg -layouts
 */
public enum Channel {
	/** front left */
	FL("front left"),
	/** front right */
	FR("front right"),
	/** front center */
	FC("front center"),
	/** low frequency */
	LFE("low frequency"),
	/** back left */
	BL("back left"),
	/** back right */
	BR("back right"),
	/** front left-of-center */
	FLC("front left-of-center"),
	/** front right-of-center */
	FRC("front right-of-center"),
	/** back center */
	BC("back center"),
	/** side left */
	SL("side left"),
	/** side right */
	SR("side right"),
	/** top center */
	TC("top center"),
	/** top front left */
	TFL("top front left"),
	/** top front center */
	TFC("top front center"),
	/** top front right */
	TFR("top front right"),
	/** top back left */
	TBL("top back left"),
	/** top back center */
	TBC("top back center"),
	/** top back right */
	TBR("top back right"),
	/** downmix left */
	DL("downmix left"),
	/** downmix right */
	DR("downmix right"),
	/** wide left */
	WL("wide left"),
	/** wide right */
	WR("wide right"),
	/** surround direct left */
	SDL("surround direct left"),
	/** surround direct right */
	SDR("surround direct right"),
	/** low frequency 2 */
	LFE2("low frequency 2");

	private final String longName;

	private Channel(final String longName) {
		this.longName = longName;
	}

	public String getLongName() {
		return longName;
	}

	@Override
	public String toString() {
		return name();
	}
}
