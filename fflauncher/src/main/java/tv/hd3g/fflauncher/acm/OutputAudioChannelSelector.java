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
public class OutputAudioChannelSelector extends ACMAudioChannelSelector {

	public static final OutputAudioChannelSelector OUT_CH0 = new OutputAudioChannelSelector(0);
	public static final OutputAudioChannelSelector OUT_CH1 = new OutputAudioChannelSelector(1);
	public static final OutputAudioChannelSelector OUT_CH2 = new OutputAudioChannelSelector(2);
	public static final OutputAudioChannelSelector OUT_CH3 = new OutputAudioChannelSelector(3);
	public static final OutputAudioChannelSelector OUT_CH4 = new OutputAudioChannelSelector(4);
	public static final OutputAudioChannelSelector OUT_CH5 = new OutputAudioChannelSelector(5);
	public static final OutputAudioChannelSelector OUT_CH6 = new OutputAudioChannelSelector(6);
	public static final OutputAudioChannelSelector OUT_CH7 = new OutputAudioChannelSelector(7);
	public static final OutputAudioChannelSelector OUT_CH8 = new OutputAudioChannelSelector(8);
	public static final OutputAudioChannelSelector OUT_CH9 = new OutputAudioChannelSelector(9);
	public static final OutputAudioChannelSelector OUT_CH10 = new OutputAudioChannelSelector(10);
	public static final OutputAudioChannelSelector OUT_CH11 = new OutputAudioChannelSelector(11);
	public static final OutputAudioChannelSelector OUT_CH12 = new OutputAudioChannelSelector(12);
	public static final OutputAudioChannelSelector OUT_CH13 = new OutputAudioChannelSelector(13);
	public static final OutputAudioChannelSelector OUT_CH14 = new OutputAudioChannelSelector(14);
	public static final OutputAudioChannelSelector OUT_CH15 = new OutputAudioChannelSelector(15);

	/**
	 * 0 is always the first !
	 */
	public OutputAudioChannelSelector(final int posInStream) {
		super(posInStream);
	}

	@Override
	public String toString() {
		return "CHOUT_" + getPosInStream();
	}

}