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
package tv.hd3g.fflauncher;

import static java.util.Objects.requireNonNull;

import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ffmpeg.ffprobe.StreamType;

import tv.hd3g.fflauncher.about.FFAbout;
import tv.hd3g.fflauncher.enums.FFUnit;
import tv.hd3g.ffprobejaxb.FFprobeJAXB;
import tv.hd3g.processlauncher.cmdline.Parameters;

public class FFmpeg extends FFbase implements InputGeneratorsTraits {

	private static final Logger log = LogManager.getLogger();
	private int deviceIdToUse = -1;

	public FFmpeg(final String execName, final Parameters parameters) {
		super(execName, parameters);
	}

	/**
	 * Define cmd var name like &lt;%OUT_AUTOMATIC_n%&gt; with "n" the # of setted destination.
	 * Add "-f container destination"
	 * Don't forget to call fixIOParametredVars
	 */
	public FFmpeg addSimpleOutputDestination(final String destinationName, final String destinationContainer) {
		requireNonNull(destinationName, "\"destinationName\" can't to be null");
		requireNonNull(destinationContainer, "\"destinationContainer\" can't to be null");

		final var varname = getInternalParameters()
		        .tagVar("OUT_AUTOMATIC_" + outputExpectedDestinations.size());
		addOutputDestination(destinationName, varname, "-f", destinationContainer);
		return this;
	}

	/**
	 * Define cmd var name like &lt;%OUT_AUTOMATIC_n%&gt; with "n" the # of setted destination.
	 * Add "-f container /destination"
	 * Don't forget to call fixIOParametredVars
	 */
	public FFmpeg addSimpleOutputDestination(final File destinationFile, final String destinationContainer) {
		requireNonNull(destinationFile, "\"destinationFile\" can't to be null");
		requireNonNull(destinationContainer, "\"destinationContainer\" can't to be null");

		final var varname = getInternalParameters()
		        .tagVar("OUT_AUTOMATIC_" + outputExpectedDestinations.size());
		addOutputDestination(destinationFile, varname, "-f", destinationContainer);
		return this;
	}

	/**
	 * Add "-movflags faststart"
	 * Please, put it a the end of command line, before output stream.
	 */
	public FFmpeg addFastStartMovMp4File() {
		getInternalParameters().addBulkParameters("-movflags faststart");
		return this;
	}

	/**
	 * Not checks will be done
	 * NVIDIA Performance Primitives via libnpp.
	 * Via -vf ffmpeg's option.
	 * @param newSize like 1280x720 or -1x720
	 * @param pixelFormat can be null (== same) or nv12, yuv444p16...
	 * @param interpAlgo can be null or nn (Nearest neighbour), linear (2-parameter cubic (B=1, C=0)), cubic2p_catmullrom (2-parameter cubic (B=0, C=1/2)), cubic2p_b05c03 (2-parameter cubic (B=1/2,
	 *        C=3/10)), super (Supersampling), lanczos ...
	 */
	public FFmpeg addHardwareNVScalerFilter(final Point newSize, final String pixelFormat, final String interpAlgo) {
		final var scale = new StringBuilder();

		scale.append("scale_npp=");
		scale.append("w=" + newSize.x + ":");
		scale.append("h=" + newSize.y + ":");
		if (pixelFormat != null) {
			scale.append("format=" + pixelFormat + ":");
		}
		if (interpAlgo != null) {
			scale.append("interp_algo=" + interpAlgo);
		}

		log.debug("Add vf: {}", scale);
		getInternalParameters().addParameters("-vf", scale.toString());

		return this;
	}

	/**
	 * Use nvresize
	 * Not checks will be done
	 * @param configuration resolution -&gt; filter out name ; resolution can be litteral like hd1080 or cif and filter out name can be "out0", usable after with "-map [out0] -vcodec xxx out.ext"
	 */
	public FFmpeg addHardwareNVMultipleScalerFilterComplex(final Map<String, String> configuration) {
		requireNonNull(configuration, "\"configuration\" can't to be null");
		if (configuration.isEmpty()) {
			throw new IllegalArgumentException("\"configuration\" can't to be empty");
		}

		final var nvresize = new StringBuilder();
		nvresize.append("nvresize=outputs=" + configuration.size() + ":");
		nvresize.append("size=" + configuration.keySet().stream().collect(Collectors.joining("|")) + ":");

		if (deviceIdToUse > -1) {
			nvresize.append("gpu=" + deviceIdToUse + ":");
		}

		nvresize.append("readback=0" + configuration.keySet().stream()
		        .map(configuration::get)
		        .collect(Collectors.joining("", "[", "]")));

		log.debug("Add filter_complex: {}", nvresize);
		getInternalParameters().addParameters("-filter_complex", nvresize.toString());
		return this;
	}

	public static Optional<StreamType> getFirstVideoStream(final FFprobeJAXB analysingResult) {
		final var oVideoStream = analysingResult.getVideoStreams().findFirst();

		if (oVideoStream.isPresent() && oVideoStream.get().getDisposition().getAttachedPic() == 0) {
			return oVideoStream;
		}
		return Optional.empty();
	}

	public enum FFHardwareCodec {
		/**
		 * cuvid and nvenc
		 * ALL CODECS ARE NOT AVAILABLE FOR ALL GRAPHICS CARDS, EVEN IF FFMPEG SUPPORT IT.
		 */
		NV;
	}

	/**
	 * Used with hardware transcoding.
	 * @param deviceIdToUse -1 by default
	 */
	public FFmpeg setDeviceIdToUse(final int deviceIdToUse) {
		this.deviceIdToUse = deviceIdToUse;
		return this;
	}

	/**
	 * @return -1 by default
	 */
	public int getDeviceIdToUse() {
		return deviceIdToUse;
	}

	/**
	 * "Patch" ffmpeg command line for hardware decoding. Only first video stream will be decoded.
	 * Hardware decoding often works in tandem with hardware coding.
	 * @throws MediaException if hardware decoding is not possible.
	 */
	public FFmpeg addHardwareVideoDecoding(final String source,
	                                       final FFprobeJAXB analysingResult,
	                                       final FFHardwareCodec hardwareCodec,
	                                       final FFAbout about) throws MediaException {
		final var oVideoStream = getFirstVideoStream(analysingResult);

		if (oVideoStream.isPresent() == false) {
			throw new MediaException("Can't found \"valid\" video stream on \"" + source + "\"");
		}

		final var videoStream = oVideoStream.get();

		final var codec = about.getCodecs().stream()
		        .filter(c -> (c.decodingSupported && c.name.equals(videoStream.getCodecName())))
		        .findFirst()
		        .orElseThrow(() -> new MediaException("Can't found a valid decoder codec for " + videoStream
		                .getCodecName() + " in \"" + source + "\""));

		if (hardwareCodec == FFHardwareCodec.NV && about.isNVToolkitIsAvaliable()) {
			final var oSourceCuvidCodecEngine = codec.decoders.stream().filter(decoder -> decoder
			        .endsWith("_cuvid")).findFirst();

			if (oSourceCuvidCodecEngine.isPresent()) {
				/**
				 * [-hwaccel_device 0] -hwaccel cuvid -c:v source_cuvid_codec [-vsync 0] -i source
				 */
				final var sourceOptions = new ArrayList<String>();
				if (deviceIdToUse > -1) {
					sourceOptions.add("-hwaccel_device");
					sourceOptions.add(Integer.toString(deviceIdToUse));
				}
				sourceOptions.add("-hwaccel");
				sourceOptions.add("cuvid");
				sourceOptions.add("-vsync");
				sourceOptions.add("0");
				sourceOptions.add("-c:v");
				sourceOptions.add(oSourceCuvidCodecEngine.get());

				log.debug(() -> "Add hardware decoded source "
				                + sourceOptions.stream().collect(Collectors.joining(" "))
				                + " -i " + source);
				addSimpleInputSource(source, sourceOptions);
				return this;
			}
		}

		throw new MediaException("Can't found a valid hardware decoder on \"" + source + "\" (\"" + videoStream
		        .getCodecLongName() + "\")");
	}

	/**
	 * Set codec name, and if it possible, use hardware encoding.
	 * @param outputVideoStreamIndex (-1 by default), X -&gt; -c:v:X
	 */
	public FFmpeg addHardwareVideoEncoding(final String destCodecName,
	                                       final int outputVideoStreamIndex,
	                                       final FFHardwareCodec hardwareCodec,
	                                       final FFAbout about) throws MediaException {

		if (destCodecName.equals("copy")) {
			throw new MediaException("\"copy\" codec can't be handled by hardware !");
		}

		final var codec = about.getCodecs().stream().filter(c -> (c.encodingSupported && c.name.equals(
		        destCodecName))).findFirst().orElseThrow(() -> new MediaException("Can't found a valid codec for "
		                                                                          + destCodecName));

		String coder;
		if (hardwareCodec == FFHardwareCodec.NV && about.isNVToolkitIsAvaliable()) {
			coder = codec.encoders.stream()
			        .filter(encoder -> (encoder.endsWith("_nvenc")
			                            || encoder.startsWith("nvenc_")
			                            || encoder.equals("nvenc")))
			        .findFirst()
			        .orElseThrow(() -> new MediaException("Can't found a valid hardware " + hardwareCodec
			                                              + " codec for " + destCodecName));

		} else {
			throw new MediaException("Can't found a valid hardware coder to \"" + destCodecName + "\"");
		}

		if (outputVideoStreamIndex > -1) {
			getInternalParameters().addParameters("-c:v:" + outputVideoStreamIndex, coder);
		} else {
			getInternalParameters().addParameters("-c:v", coder);
		}

		return this;
	}

	public enum Preset {
		ULTRAFAST,
		SUPERFAST,
		VERYFAST,
		FASTER,
		FAST,
		MEDIUM,
		SLOW,
		SLOWER,
		VERYSLOW,
		PLACEBO;

		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}

	public enum Tune {
		FILM,
		ANIMATION,
		GRAIN,
		STILLIMAGE,
		PSNR,
		SSIM,
		FASTDECODE,
		ZEROLATENCY;

		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}

	public FFmpeg addPreset(final Preset preset) {
		getInternalParameters().addParameters("-preset", preset.toString());
		return this;
	}

	public FFmpeg addTune(final Tune tune) {
		getInternalParameters().addParameters("-tune", tune.toString());
		return this;
	}

	/**
	 * @param outputVideoStreamIndex -1 by default
	 */
	public FFmpeg addBitrate(final int bitrate, final FFUnit bitrateUnit, final int outputVideoStreamIndex) {
		if (outputVideoStreamIndex > -1) {
			getInternalParameters().addParameters("-b:v:" + outputVideoStreamIndex, bitrate + bitrateUnit
			        .toString());
		} else {
			getInternalParameters().addParameters("-b:v", bitrate + bitrateUnit.toString());
		}
		return this;
	}

	/**
	 * @param minRate set -1 for default
	 * @param maxRate set -1 for default
	 * @param bufsize set -1 for default
	 */
	public FFmpeg addBitrateControl(final int minRate,
	                                final int maxRate,
	                                final int bufsize,
	                                final FFUnit bitrateUnit) {
		if (minRate > 0) {
			getInternalParameters().addParameters("-minrate", minRate + bitrateUnit.toString());
		}
		if (maxRate > 0) {
			getInternalParameters().addParameters("-maxrate", maxRate + bitrateUnit.toString());
		}
		if (bufsize > 0) {
			getInternalParameters().addParameters("-bufsize", bufsize + bitrateUnit.toString());
		}
		return this;
	}

	/**
	 * Constant bitrate factor, 0=lossless.
	 */
	public FFmpeg addCRF(final int crf) {
		getInternalParameters().addParameters("-crf", String.valueOf(crf));
		return this;
	}

	/**
	 * No checks will be done.
	 * See FFmpeg.addVideoEncoding for hardware use
	 * @param outputVideoStreamIndex -1 by default
	 */
	public FFmpeg addVideoCodecName(final String codecName, final int outputVideoStreamIndex) {
		if (outputVideoStreamIndex > -1) {
			getInternalParameters().addParameters("-c:v:" + outputVideoStreamIndex, codecName);
		} else {
			getInternalParameters().addParameters("-c:v", codecName);
		}
		return this;
	}

	/**
	 * @param b_frames set 0 for default
	 * @param gop_size set 0 for default
	 * @param ref_frames set 0 for default
	 */
	public FFmpeg addGOPControl(final int b_frames, final int gop_size, final int ref_frames) {
		if (b_frames > 0) {
			getInternalParameters().addParameters("-bf", String.valueOf(b_frames));
		}
		if (gop_size > 0) {
			getInternalParameters().addParameters("-g", String.valueOf(gop_size));
		}
		if (ref_frames > 0) {
			getInternalParameters().addParameters("-ref", String.valueOf(ref_frames));
		}
		return this;
	}

	/**
	 * @param i_qfactor set 0 for default
	 * @param b_qfactor set 0 for default
	 */
	public FFmpeg addIBQfactor(final float i_qfactor, final float b_qfactor) {
		if (i_qfactor > 0f) {
			getInternalParameters().addParameters("-i_qfactor", String.valueOf(i_qfactor));
		}
		if (b_qfactor > 0f) {
			getInternalParameters().addParameters("-b_qfactor", String.valueOf(b_qfactor));
		}
		return this;
	}

	/**
	 * @param qmin set 0 for default
	 * @param qmax set 0 for default
	 */
	public FFmpeg addQMinMax(final int qmin, final int qmax) {
		if (qmin > 0) {
			getInternalParameters().addParameters("-qmin", String.valueOf(qmin));
		}
		if (qmax > 0) {
			getInternalParameters().addParameters("-qmax", String.valueOf(qmax));
		}
		return this;
	}

	/**
	 * No checks will be done.
	 * @param outputAudioStreamIndex -1 by default
	 */
	public FFmpeg addAudioCodecName(final String codecName, final int outputAudioStreamIndex) {
		if (outputAudioStreamIndex > -1) {
			getInternalParameters().addParameters("-c:a:" + outputAudioStreamIndex, codecName);
		} else {
			getInternalParameters().addParameters("-c:a", codecName);
		}
		return this;
	}

	/**
	 * No checks will be done.
	 * like -vsync value
	 */
	public FFmpeg addVsync(final int value) {
		getInternalParameters().addParameters("-vsync", String.valueOf(value));
		return this;
	}

	/**
	 * No checks will be done.
	 * like -map sourceIndex:streamIndexInSource ; 0 is the first.
	 */
	public FFmpeg addMap(final int sourceIndex, final int streamIndexInSource) {
		getInternalParameters().addParameters("-map", sourceIndex + ":" + streamIndexInSource);
		return this;
	}

}
