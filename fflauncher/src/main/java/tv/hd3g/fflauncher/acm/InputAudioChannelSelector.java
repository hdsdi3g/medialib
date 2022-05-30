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

/**
 * 0 is always the first !
 */
public class InputAudioChannelSelector extends ACMAudioChannelSelector {

	public static final InputAudioChannelSelector IN_CH0 = new InputAudioChannelSelector(0);
	public static final InputAudioChannelSelector IN_CH1 = new InputAudioChannelSelector(1);
	public static final InputAudioChannelSelector IN_CH2 = new InputAudioChannelSelector(2);
	public static final InputAudioChannelSelector IN_CH3 = new InputAudioChannelSelector(3);
	public static final InputAudioChannelSelector IN_CH4 = new InputAudioChannelSelector(4);
	public static final InputAudioChannelSelector IN_CH5 = new InputAudioChannelSelector(5);
	public static final InputAudioChannelSelector IN_CH6 = new InputAudioChannelSelector(6);
	public static final InputAudioChannelSelector IN_CH7 = new InputAudioChannelSelector(7);
	public static final InputAudioChannelSelector IN_CH8 = new InputAudioChannelSelector(8);
	public static final InputAudioChannelSelector IN_CH9 = new InputAudioChannelSelector(9);
	public static final InputAudioChannelSelector IN_CH10 = new InputAudioChannelSelector(10);
	public static final InputAudioChannelSelector IN_CH11 = new InputAudioChannelSelector(11);
	public static final InputAudioChannelSelector IN_CH12 = new InputAudioChannelSelector(12);
	public static final InputAudioChannelSelector IN_CH13 = new InputAudioChannelSelector(13);
	public static final InputAudioChannelSelector IN_CH14 = new InputAudioChannelSelector(14);
	public static final InputAudioChannelSelector IN_CH15 = new InputAudioChannelSelector(15);

	/**
	 * 0 is always the first !
	 */
	public InputAudioChannelSelector(final int posInStream) {
		super(posInStream);
	}

	@Override
	public String toString() {
		return "CHIN_" + getPosInStream();
	}

}