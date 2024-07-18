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
 * Copyright (C) hdsdi3g for hd3g.tv 2024
 *
 */
package tv.hd3g.fflauncher.recipes.wavmeasure;

import static tv.hd3g.fflauncher.enums.FFLogLevel.WARNING;
import static tv.hd3g.fflauncher.recipes.wavmeasure.WavMeasureSetup.SAMPLE_RATE;

import tv.hd3g.fflauncher.FFmpeg;
import tv.hd3g.fflauncher.processingtool.FFmpegToolBuilder;
import tv.hd3g.processlauncher.ProcesslauncherLifecycle;
import tv.hd3g.processlauncher.processingtool.DirectStdoutGetStderrWatcher;

public class WavMeasure extends FFmpegToolBuilder<WavMeasureSetup, MeasuredWav, DirectStdoutGetStderrWatcher> {

	private SampleCapture sampleCapture;

	public WavMeasure(final String execName) {
		super(new FFmpeg(execName), new DirectStdoutGetStderrWatcher());
	}

	@Override
	protected FFmpeg getParametersProvider(final WavMeasureSetup setup) {
		sampleCapture = new SampleCapture(
				(int) (setup.getFileDuration().toMillis()
					   * SAMPLE_RATE
					   / 1000l
					   / setup.getOutputWide()),
				setup.getOutputWide());
		executorWatcher.setStdOutConsumer(sampleCapture);

		ffmpeg.setHidebanner();
		ffmpeg.setNostats();
		ffmpeg.setLogLevel(WARNING, false, true);
		ffmpeg.setNoVideo();
		ffmpeg.addAudioChannelCount(1, -1);
		ffmpeg.addAudioSamplingRate(48000, -1);
		setup.applySource(ffmpeg);
		ffmpeg.addSimpleOutputDestination("-", "s16be");
		return ffmpeg;
	}

	@Override
	protected MeasuredWav compute(final WavMeasureSetup setup, final ProcesslauncherLifecycle lifeCycle) {
		return sampleCapture.getMeasuredWav();
	}

}
