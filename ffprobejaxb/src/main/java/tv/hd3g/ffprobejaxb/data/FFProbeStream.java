/*
 * This file is part of ffprobejaxb.
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
 * Copyright (C) hdsdi3g for hd3g.tv 2024
 *
 */
package tv.hd3g.ffprobejaxb.data;

import java.util.List;

/**
 * StreamType
 */
public record FFProbeStream(FFProbeStreamDisposition disposition,
							List<FFProbeKeyValue> tags,
							List<FFProbePacketSideData> sideDataList,
							int index,
							String codecName,
							String codecLongName,
							String profile,
							String codecType,
							String codecTag,
							String codecTagString,
							String extradata,
							int extradataSize,
							String extradataHash,
							int width,
							int height,
							int codedWidth,
							int codedHeight,
							boolean closedCaptions,
							boolean filmGrain,
							boolean hasBFrames,
							String sampleAspectRatio,
							String displayAspectRatio,
							String pixFmt,
							int level,
							String colorRange,
							String colorSpace,
							String colorTransfer,
							String colorPrimaries,
							String chromaLocation,
							String fieldOrder,
							int refs,
							String sampleFmt,
							int sampleRate,
							int channels,
							String channelLayout,
							int bitsPerSample,
							int initialPadding,
							String id,
							String rFrameRate,
							String avgFrameRate,
							String timeBase,
							long startPts,
							float startTime,
							long durationTs,
							float duration,
							int bitRate,
							int maxBitRate,
							int bitsPerRawSample,
							int nbFrames,
							int nbReadFrames,
							int nbReadPackets) {

}
