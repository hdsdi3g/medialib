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
package tv.hd3g.fflauncher;

import static java.util.Objects.requireNonNull;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

import tv.hd3g.fflauncher.about.FFAbout;
import tv.hd3g.fflauncher.enums.FFHardwareCodec;
import tv.hd3g.ffprobejaxb.FFprobeJAXB;

public interface HardwareProcessTraits extends InternalParametersSupplier, SimpleSourceTraits {

	/**
	 * @return -1 by default
	 */
	default int getDeviceIdToUse() {
		return -1;
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
	default void addHardwareNVScalerFilter(final Point newSize, final String pixelFormat, final String interpAlgo) {
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

		getInternalParameters().addParameters("-vf", scale.toString());
	}

	/**
	 * Use nvresize
	 * Not checks will be done
	 * @param configuration resolution -&gt; filter out name ; resolution can be litteral like hd1080 or cif and filter out name can be "out0", usable after with "-map [out0] -vcodec xxx out.ext"
	 */
	default void addHardwareNVMultipleScalerFilterComplex(final Map<String, String> configuration) {
		requireNonNull(configuration, "\"configuration\" can't to be null");
		if (configuration.isEmpty()) {
			throw new IllegalArgumentException("\"configuration\" can't to be empty");
		}

		final var nvresize = new StringBuilder();
		nvresize.append("nvresize=outputs=" + configuration.size() + ":");
		nvresize.append("size=" + configuration.keySet().stream().collect(Collectors.joining("|")) + ":");

		if (getDeviceIdToUse() > -1) {
			nvresize.append("gpu=" + getDeviceIdToUse() + ":");
		}

		nvresize.append("readback=0" + configuration.keySet().stream()
				.map(configuration::get)
				.collect(Collectors.joining("", "[", "]")));

		getInternalParameters().addParameters("-filter_complex", nvresize.toString());
	}

	/**
	 * "Patch" ffmpeg command line for hardware decoding. Only first video stream will be decoded.
	 * Hardware decoding often works in tandem with hardware coding.
	 * @throws MediaException if hardware decoding is not possible.
	 */
	default void addHardwareVideoDecoding(final String source,
										  final FFprobeJAXB analysingResult,
										  final FFHardwareCodec hardwareCodec,
										  final FFAbout about) throws MediaException {
		final var oVideoStream = analysingResult.getFirstVideoStream();

		if (oVideoStream.isPresent() == false) {
			throw new MediaException("Can't found \"valid\" video stream on \"" + source + "\"");
		}

		final var videoStream = oVideoStream.get();

		final var codec = about.getCodecs().stream()
				.filter(c -> (c.decodingSupported && c.name.equals(videoStream.codecName())))
				.findFirst()
				.orElseThrow(() -> new MediaException("Can't found a valid decoder codec for " + videoStream
						.codecName() + " in \"" + source + "\""));

		if (hardwareCodec == FFHardwareCodec.NV && about.isNVToolkitIsAvaliable()) {
			final var oSourceCuvidCodecEngine = codec.decoders.stream().filter(decoder -> decoder
					.endsWith("_cuvid")).findFirst();

			if (oSourceCuvidCodecEngine.isPresent()) {
				/**
				 * [-hwaccel_device 0] -hwaccel cuvid -c:v source_cuvid_codec [-vsync 0] -i source
				 */
				final var sourceOptions = new ArrayList<String>();
				if (getDeviceIdToUse() > -1) {
					sourceOptions.add("-hwaccel_device");
					sourceOptions.add(Integer.toString(getDeviceIdToUse()));
				}
				sourceOptions.add("-hwaccel");
				sourceOptions.add("cuvid");
				sourceOptions.add("-vsync");
				sourceOptions.add("0");
				sourceOptions.add("-c:v");
				sourceOptions.add(oSourceCuvidCodecEngine.get());
				addSimpleInputSource(source, sourceOptions);
			}
		}

		throw new MediaException("Can't found a valid hardware decoder on \"" + source + "\" (\"" + videoStream
				.codecLongName() + "\")");
	}

	/**
	 * Set codec name, and if it possible, use hardware encoding.
	 * @param outputVideoStreamIndex (-1 by default), X -&gt; -c:v:X
	 */
	default void addHardwareVideoEncoding(final String destCodecName,
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
	}

}
