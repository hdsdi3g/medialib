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
 * Copyright (C) hdsdi3g for hd3g.tv 2023
 *
 */
package tv.hd3g.ffprobejaxb;

import java.util.List;
import java.util.Optional;

import org.ffmpeg.ffprobe700.ChapterType;
import org.ffmpeg.ffprobe700.FfprobeType;
import org.ffmpeg.ffprobe700.LibraryVersionType;
import org.ffmpeg.ffprobe700.PacketSideDataType;
import org.ffmpeg.ffprobe700.PixelFormatComponentType;
import org.ffmpeg.ffprobe700.PixelFormatFlagsType;
import org.ffmpeg.ffprobe700.PixelFormatType;
import org.ffmpeg.ffprobe700.ProgramType;
import org.ffmpeg.ffprobe700.StreamType;
import org.ffmpeg.ffprobe700.StreamsType;
import org.ffmpeg.ffprobe700.TagType;
import org.ffmpeg.ffprobe700.TagsType;

import tv.hd3g.ffprobejaxb.data.FFProbeChapter;
import tv.hd3g.ffprobejaxb.data.FFProbeError;
import tv.hd3g.ffprobejaxb.data.FFProbeFormat;
import tv.hd3g.ffprobejaxb.data.FFProbeKeyValue;
import tv.hd3g.ffprobejaxb.data.FFProbeLibraryVersion;
import tv.hd3g.ffprobejaxb.data.FFProbePacketSideData;
import tv.hd3g.ffprobejaxb.data.FFProbePixelFormat;
import tv.hd3g.ffprobejaxb.data.FFProbeProgram;
import tv.hd3g.ffprobejaxb.data.FFProbeProgramVersion;
import tv.hd3g.ffprobejaxb.data.FFProbeStream;
import tv.hd3g.ffprobejaxb.data.FFProbeStreamDisposition;

public class FFprobeJAXB700 extends FFprobeJAXB {

	private FfprobeType ffprobe;

	protected FFprobeJAXB700(final String xmlContent) {
		super(xmlContent);
	}

	@Override
	protected void setJAXB(final Object rawJAXB) {
		ffprobe = (FfprobeType) rawJAXB;
	}

	@Override
	public String getXSDVersionReference() {
		final var pname = ffprobe.getClass().getPackageName();
		return pname.substring(pname.lastIndexOf(".") + 1);
	}

	@Override
	public List<FFProbeLibraryVersion> getLibraryVersions() {
		return getSubList(ffprobe.getLibraryVersions(), LibraryVersionType.class)
				.map(f -> new FFProbeLibraryVersion(
						f.getName(),
						f.getMajor(),
						f.getMinor(),
						f.getMicro(),
						f.getVersion(),
						f.getIdent()))
				.toList();
	}

	@Override
	public Optional<FFProbeProgramVersion> getProgramVersion() {
		return Optional.ofNullable(ffprobe.getProgramVersion())
				.map(p -> new FFProbeProgramVersion(
						p.getVersion(),
						p.getCopyright(),
						p.getBuildDate(),
						p.getBuildTime(),
						p.getCompilerIdent(),
						p.getConfiguration()));
	}

	@Override
	public Optional<FFProbeError> getError() {
		return Optional.ofNullable(ffprobe.getError())
				.map(e -> new FFProbeError(e.getCode(), e.getString()));
	}

	private static List<FFProbeKeyValue> getTags(final TagsType tagsType) {
		return getSubList(tagsType, TagType.class)
				.map(t -> new FFProbeKeyValue(t.getKey(), t.getValue()))
				.toList();
	}

	@Override
	public Optional<FFProbeFormat> getFormat() {
		return Optional.ofNullable(ffprobe.getFormat())
				.map(f -> new FFProbeFormat(
						getTags(f.getTags()),
						f.getFilename(),
						f.getNbStreams(),
						f.getNbPrograms(),
						f.getFormatName(),
						f.getFormatLongName(),
						getNonNull(f.getStartTime()),
						getNonNull(f.getDuration()),
						getNonNull(f.getSize()),
						getNonNull(f.getBitRate()),
						getNonNull(f.getProbeScore())));
	}

	private static List<FFProbeKeyValue> getChapterTags(final ChapterType c) {
		return c.getTags().stream()
				.flatMap(t -> Optional.ofNullable(t.getTag())
						.stream()
						.flatMap(List::stream))
				.map(t -> new FFProbeKeyValue(t.getKey(), t.getValue()))
				.toList();
	}

	@Override
	public List<FFProbeChapter> getChapters() {
		return getSubList(ffprobe.getChapters(), ChapterType.class)
				.map(c -> new FFProbeChapter(
						getChapterTags(c),
						c.getId(),
						c.getTimeBase(),
						c.getStart(),
						c.getStartTime(),
						c.getEnd(),
						c.getEndTime()))
				.toList();
	}

	private static FFProbeStreamDisposition getDispositions(final StreamType s) {
		return Optional.ofNullable(s.getDisposition())
				.map(d -> new FFProbeStreamDisposition(
						d.getDefault() == 1,
						d.getDub() == 1,
						d.getOriginal() == 1,
						d.getComment() == 1,
						d.getLyrics() == 1,
						d.getKaraoke() == 1,
						d.getForced() == 1,
						d.getHearingImpaired() == 1,
						d.getVisualImpaired() == 1,
						d.getCleanEffects() == 1,
						d.getAttachedPic() == 1,
						d.getTimedThumbnails() == 1,
						d.getNonDiegetic() == 1,
						d.getCaptions() == 1,
						d.getDescriptions() == 1,
						d.getMetadata() == 1,
						d.getDependent() == 1,
						d.getStillImage() == 1))
				.orElseGet(FFProbeStreamDisposition::getByNames);
	}

	private static List<FFProbePacketSideData> getSideDataList(final StreamType s) {
		return getSubList(s.getSideDataList(), PacketSideDataType.class)
				.map(sdl -> new FFProbePacketSideData(
						sdl.getSideDatum().stream().map(sd -> new FFProbeKeyValue(
								sd.getKey(),
								sd.getValue()))
								.toList(),
						sdl.getType(),
						0))
				.toList();
	}

	private static List<FFProbeStream> getStreams(final StreamsType streamsType) {
		return getSubList(streamsType, StreamType.class)
				.map(s -> new FFProbeStream( // NOSONAR 5612
						getDispositions(s),
						getTags(s.getTags()),
						getSideDataList(s),
						s.getIndex(),
						s.getCodecName(),
						s.getCodecLongName(),
						s.getProfile(),
						s.getCodecType(),
						s.getCodecTag(),
						s.getCodecTagString(),
						s.getExtradata(),
						s.getExtradataSize() != null ? s.getExtradataSize() : 0,
						s.getExtradataHash(),
						getNonNull(s.getWidth()),
						getNonNull(s.getHeight()),
						getNonNull(s.getCodedWidth()),
						getNonNull(s.getCodedHeight()),
						getNonNull(s.isClosedCaptions()),
						getNonNull(s.isFilmGrain()),
						s.getHasBFrames() != null && s.getHasBFrames() > 0,
						s.getSampleAspectRatio(),
						s.getDisplayAspectRatio(),
						s.getPixFmt(),
						getNonNull(s.getLevel()),
						s.getColorRange(),
						s.getColorSpace(),
						s.getColorTransfer(),
						s.getColorPrimaries(),
						s.getChromaLocation(),
						s.getFieldOrder(),
						getNonNull(s.getRefs()),
						s.getSampleFmt(),
						getNonNull(s.getSampleRate()),
						getNonNull(s.getChannels()),
						s.getChannelLayout(),
						getNonNull(s.getBitsPerSample()),
						getNonNull(s.getInitialPadding()),
						s.getId(),
						s.getRFrameRate(),
						s.getAvgFrameRate(),
						s.getTimeBase(),
						getNonNull(s.getStartPts()),
						getNonNull(s.getStartTime()),
						getNonNull(s.getDurationTs()),
						getNonNull(s.getDuration()),
						getNonNull(s.getBitRate()),
						getNonNull(s.getMaxBitRate()),
						getNonNull(s.getBitsPerRawSample()),
						getNonNull(s.getNbFrames()),
						getNonNull(s.getNbReadFrames()),
						getNonNull(s.getNbReadPackets())))
				.toList();
	}

	@Override
	public List<FFProbeProgram> getPrograms() {
		return getSubList(ffprobe.getPrograms(), ProgramType.class)
				.map(p -> new FFProbeProgram(
						getTags(p.getTags()),
						getStreams(p.getStreams()),
						p.getProgramId(),
						p.getProgramNum(),
						p.getNbStreams(),
						p.getPmtPid(),
						p.getPcrPid()))
				.toList();
	}

	@Override
	public List<FFProbeStream> getStreams() {
		return getStreams(ffprobe.getStreams());
	}

	@Override
	public List<FFProbePixelFormat> getPixelFormats() {
		return getSubList(ffprobe.getPixelFormats(), PixelFormatType.class)
				.map(pf -> {
					final var flags = Optional.ofNullable(pf.getFlags())
							.orElse(new PixelFormatFlagsType());
					final var bitDepthByComponent = getSubList(pf.getComponents(), PixelFormatComponentType.class)
							.sorted((l, r) -> Integer.compare(l.getIndex(), r.getIndex()))
							.map(PixelFormatComponentType::getBitDepth)
							.toList();

					return new FFProbePixelFormat(
							pf.getName(),
							pf.getNbComponents(),
							getNonNull(pf.getLog2ChromaW()),
							getNonNull(pf.getLog2ChromaH()),
							getNonNull(pf.getBitsPerPixel()),
							getNonNull(flags.getBigEndian()) == 1,
							getNonNull(flags.getPalette()) == 1,
							getNonNull(flags.getBitstream()) == 1,
							getNonNull(flags.getHwaccel()) == 1,
							getNonNull(flags.getPlanar()) == 1,
							getNonNull(flags.getRgb()) == 1,
							getNonNull(flags.getAlpha()) == 1,
							bitDepthByComponent);
				})
				.toList();
	}
}
