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

	Predicate<FFProbeStream> filterVideoStream = streamType -> streamType.codecType().equals("video");
	Predicate<FFProbeStream> filterAudioStream = streamType -> streamType.codecType().equals("audio");
	Predicate<FFProbeStream> filterDataStream = streamType -> streamType.codecType().equals("data");

	default Stream<FFProbeStream> getVideoStreams() {
		return getStreams().stream().filter(filterVideoStream);
	}

	default Stream<FFProbeStream> getAudioStreams() {
		return getStreams().stream().filter(filterAudioStream);
	}

	default Optional<FFProbeStream> getFirstVideoStream() {
		return getVideoStreams()
				.filter(vs -> vs.disposition().attachedPic() == false)
				.filter(vs -> vs.disposition().timedThumbnails() == false)
				.filter(vs -> vs.disposition().stillImage() == false)
				.sorted((l, r) -> Boolean.compare(r.disposition().asDefault(), l.disposition().asDefault()))
				.findFirst();
	}

	/**
	 * @param discard0TC if true, don't return "00:00:00:00" values (return empty).
	 */
	default Optional<String> getTimecode(final boolean discard0TC) {
		return Stream.concat(
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

}
