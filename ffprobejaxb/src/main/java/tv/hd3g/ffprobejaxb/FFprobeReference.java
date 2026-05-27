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
package tv.hd3g.ffprobejaxb;

import static java.lang.Boolean.compare;
import static java.util.stream.Stream.concat;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import tv.hd3g.ffprobejaxb.data.FFProbeChapter;
import tv.hd3g.ffprobejaxb.data.FFProbeError;
import tv.hd3g.ffprobejaxb.data.FFProbeFormat;
import tv.hd3g.ffprobejaxb.data.FFProbeKeyValue;
import tv.hd3g.ffprobejaxb.data.FFProbeLibraryVersion;
import tv.hd3g.ffprobejaxb.data.FFProbePixelFormat;
import tv.hd3g.ffprobejaxb.data.FFProbeProgram;
import tv.hd3g.ffprobejaxb.data.FFProbeProgramVersion;
import tv.hd3g.ffprobejaxb.data.FFProbeStream;

public interface FFprobeReference {

    Optional<FFProbeFormat> getFormat();

    List<FFProbeStream> getStreams();

    String getXmlContent();

    String getXSDVersionReference();

    List<FFProbeLibraryVersion> getLibraryVersions();

    Optional<FFProbeError> getError();

    List<FFProbeProgram> getPrograms();

    Optional<FFProbeProgramVersion> getProgramVersion();

    List<FFProbeChapter> getChapters();

    List<FFProbePixelFormat> getPixelFormats();

    /**
     * Only primary streams
     */
    Predicate<FFProbeStream> filterVideoStream = streamType -> "video".equals(streamType.codecType())
                                                               && streamType.width() > 0
                                                               && streamType.height() > 0
                                                               && streamType.isSecondary() == false;
    /**
     * Only primary streams
     */
    Predicate<FFProbeStream> filterAudioStream = streamType -> "audio".equals(streamType.codecType())
                                                               && streamType.isSecondary() == false;
    /**
     * Only primary streams
     */
    Predicate<FFProbeStream> filterDataStream = streamType -> "data".equals(streamType.codecType())
                                                              && streamType.isSecondary() == false;

    Predicate<FFProbeStream> filterSecondaryStream = streamType -> streamType.isSecondary() == true;

    /**
     * Only primary streams, and width/height greater than zero
     */
    default Stream<FFProbeStream> getVideoStreams() {
        return getStreams().stream().filter(filterVideoStream);
    }

    /**
     * Only primary streams
     */
    default Stream<FFProbeStream> getAudioStreams() {
        return getStreams().stream().filter(filterAudioStream);
    }

    default boolean isDefaultStreamIsSuitable() {
        return getStreams().stream()
                .filter(s -> "data".equals(s.codecType()) == false)
                .filter(s -> s.codecName() != null)
                .map(FFProbeStream::isDefault)
                .distinct()
                .count() == 2l;
    }

    /**
     * Get only from primaries streams
     */
    default Optional<FFProbeStream> getFirstVideoStream() {
        return getVideoStreams()
                .sorted((l, r) -> compare(r.isDefault(), l.isDefault()))
                .findFirst();
    }

    /**
     * @param discard0TC if true, don't return "00:00:00:00" values (return empty).
     */
    default Optional<String> getTimecode(final boolean discard0TC) {
        return concat(
                getFormat()
                        .stream()
                        .map(FFProbeFormat::tags),
                getStreams()
                        .stream()
                        .map(FFProbeStream::tags))
                                .flatMap(List::stream)
                                .filter(t -> "timecode".equals(t.key()))
                                .map(FFProbeKeyValue::value)
                                .findFirst()
                                .filter(tc -> (tc.equals("00:00:00:00") && discard0TC ? false : true));
    }

    default Optional<Duration> getDuration() {
        return getFormat()
                .map(FFProbeFormat::duration)
                .filter(d -> d > 0d)
                .map(d -> d * 1000d)
                .map(Math::round)
                .map(Duration::ofMillis);
    }

}
