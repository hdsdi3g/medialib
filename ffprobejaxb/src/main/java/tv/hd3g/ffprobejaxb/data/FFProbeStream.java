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

import static java.lang.Float.parseFloat;
import static java.util.Optional.empty;

import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

/**
 * StreamType
 */
@Slf4j
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
                            String mime_codec_string,
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

    private static final String LOG_INVALID_FRAME_RATE = "Invalid frameRate: {}";

    public boolean isDefault() {
        return Optional.ofNullable(disposition)
                .map(FFProbeStreamDisposition::asDefault)
                .orElse(false);
    }

    public boolean isSecondary() {
        if ("data".equals(codecType) && codecName == null) {
            return true;
        }
        if (disposition == null) {
            return false;
        }
        return disposition.attachedPic()
               || disposition.stillImage()
               || disposition.timedThumbnails();
    }

    public Optional<Float> getComputedRFrameRate() {
        return parseFrameRate(rFrameRate);
    }

    public Optional<Float> getComputedAvgFrameRate() {
        return parseFrameRate(avgFrameRate);
    }

    /**
     * @return can be null
     */
    static Optional<Float> parseFrameRate(final String rawValue) {
        if (rawValue == null
            || rawValue.isBlank()
            || rawValue.equals("0/0")
            || rawValue.equals("0")
            || rawValue.equals("0.0")
            || rawValue.startsWith("-")) {
            return empty();
        }

        if (rawValue.contains("/")) {
            final var parts = rawValue.split("/");
            if (parts.length != 2) {
                log.warn(LOG_INVALID_FRAME_RATE, rawValue);
                return empty();
            }
            try {
                final var numerator = Integer.parseInt(parts[0]);
                final var denominator = Integer.parseInt(parts[1]);
                if (denominator == 0) {
                    return Optional.ofNullable((float) numerator);
                } else if (numerator == 0) {
                    return empty();
                }

                return Optional.ofNullable((float) numerator / (float) denominator);
            } catch (final NumberFormatException _) {
                log.warn(LOG_INVALID_FRAME_RATE, rawValue);
                return empty();
            }
        }

        try {
            return Optional.ofNullable(parseFloat(rawValue));
        } catch (final NumberFormatException _) {
            log.warn(LOG_INVALID_FRAME_RATE, rawValue);
            return empty();
        }
    }

}
