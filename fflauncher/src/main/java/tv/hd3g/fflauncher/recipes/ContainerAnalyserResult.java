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

import java.util.List;

import tv.hd3g.fflauncher.ffprobecontainer.FFprobeAudioFrame;
import tv.hd3g.fflauncher.ffprobecontainer.FFprobeAudioFrameConst;
import tv.hd3g.fflauncher.ffprobecontainer.FFprobePacket;
import tv.hd3g.fflauncher.ffprobecontainer.FFprobeVideoFrame;
import tv.hd3g.fflauncher.ffprobecontainer.FFprobeVideoFrameConst;

public record ContainerAnalyserResult(ContainerAnalyserSession session,
									  List<FFprobePacket> packets,
									  List<FFprobeAudioFrame> audioFrames,
									  List<FFprobeVideoFrame> videoFrames,
									  FFprobeVideoFrameConst videoConst,
									  FFprobeAudioFrameConst audioConst,
									  List<FFprobeVideoFrameConst> olderVideoConsts,
									  List<FFprobeAudioFrameConst> olderAudioConsts) {

}
