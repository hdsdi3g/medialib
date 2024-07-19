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
 * Copyright (C) hdsdi3g for hd3g.tv 2023
 *
 */
package tv.hd3g.fflauncher.filtering.lavfimtd;

import static java.lang.Float.NaN;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static tv.hd3g.fflauncher.filtering.lavfimtd.LavfiMtdIdetRepeatedFrameType.NEITHER;
import static tv.hd3g.fflauncher.filtering.lavfimtd.LavfiMtdIdetSingleFrameType.PROGRESSIVE;

import java.time.Duration;
import java.util.DoubleSummaryStatistics;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LavfiMetadataFilterParserTest {

	LavfiMetadataFilterParser p;

	static final String RAW_LINES_OK = """
			frame:0 pts:7 pts_time:0.007
			timecode=00:00:00:00
			lavfi.black_start=0.007
			frame:2 pts:90 pts_time:0.09
			lavfi.black_end=0.09
			frame:66 pts:2757 pts_time:2.757
			lavfi.freezedetect.freeze_start=0.757
			frame:97 pts:4049 pts_time:4.049
			lavfi.freezedetect.freeze_duration=3.292
			lavfi.freezedetect.freeze_end=4.049
			frame:108 pts:4507 pts_time:4.507
			lavfi.black_start=4.507
			frame:111 pts:4632 pts_time:4.632
			timecode=00:00:20:00
			lavfi.block=2.204194
			frame:114 pts:4757 pts_time:4.757
			lavfi.blur=5.744382
			frame:116 pts:4841 pts_time:4.841
			lavfi.black_end=4.841
			frame:119 pts:4966 pts_time:4.966
			lavfi.siti.si=11.67
			lavfi.siti.ti=5.60
			lavfi.idet.repeated.current_frame=neither
			lavfi.idet.repeated.neither=115.00
			lavfi.idet.repeated.top=2.00
			lavfi.idet.repeated.bottom=3.00
			lavfi.idet.single.current_frame=progressive
			lavfi.idet.single.tff=0.00
			lavfi.idet.single.bff=0.00
			lavfi.idet.single.progressive=40.00
			lavfi.idet.single.undetermined=80.00
			lavfi.idet.multiple.current_frame=progressive
			lavfi.idet.multiple.tff=0.00
			lavfi.idet.multiple.bff=0.00
			lavfi.idet.multiple.progressive=120.00
			lavfi.idet.multiple.undetermined=0.00
			frame:1022 pts:981168 pts_time:20.441
			lavfi.aphasemeter.phase=1.000000
			lavfi.aphasemeter.mono_start=18.461
			frame:1801 pts:60041 pts_time:60.041
			lavfi.cropdetect.x1=0
			lavfi.cropdetect.x2=479
			lavfi.cropdetect.y1=0
			lavfi.cropdetect.y2=479
			lavfi.cropdetect.w=480
			lavfi.cropdetect.h=480
			lavfi.cropdetect.x=0
			lavfi.cropdetect.y=0
			frame:1299 pts:1247088 pts_time:25.981
			timecode=00:00:40:00
			lavfi.aphasemeter.phase=0.992454
			lavfi.aphasemeter.mono_end=25.981
			lavfi.aphasemeter.mono_duration=2.94
			frame:3306 pts:3173808 pts_time:66.121
			lavfi.silence_start.1=65.1366
			lavfi.silence_start.2=65.1366
			lavfi.blur=5.744382
			frame:3332 pts:3198768 pts_time:66.641
			lavfi.silence_end.1=66.6474
			lavfi.silence_duration.1=1.51079
			lavfi.silence_end.2=66.6474
			lavfi.silence_duration.2=1.51079
			frame:7616 pts:317340 pts_time:317.34
			lavfi.cropdetect.x1=0
			lavfi.cropdetect.x2=1919
			lavfi.cropdetect.y1=0
			lavfi.cropdetect.y2=1079
			lavfi.cropdetect.w=1920
			lavfi.cropdetect.h=1072
			lavfi.cropdetect.x=0
			lavfi.cropdetect.y=4
			lavfi.r128.M=-36.182
			lavfi.r128.S=-36.183
			lavfi.r128.I=-24.545
			lavfi.r128.LRA=17.320
			lavfi.r128.LRA.low=-36.190
			lavfi.r128.LRA.high=-18.870
			lavfi.r128.sample_peaks_ch0=0.133
			lavfi.r128.sample_peaks_ch1=0.117
			lavfi.r128.sample_peak=0.133
			lavfi.r128.true_peaks_ch0=0.133
			lavfi.r128.true_peaks_ch1=0.118
			lavfi.r128.true_peak=0.133
			frame:87883 pts:84367728 pts_time:1757.66
			lavfi.astats.1.DC_offset=0.000001
			lavfi.astats.1.Peak_level=-0.622282
			lavfi.astats.1.Flat_factor=0.000000
			lavfi.astats.1.Peak_count=2.000000
			lavfi.astats.1.Noise_floor=-78.266739
			lavfi.astats.1.Noise_floor_count=708.000000
			lavfi.astats.1.Entropy=0.788192
			lavfi.astats.2.DC_offset=0.000002
			lavfi.astats.2.Peak_level=-0.622282
			lavfi.astats.2.Flat_factor=0.000000
			lavfi.astats.2.Peak_count=2.000000
			lavfi.astats.2.Noise_floor=-78.266739
			lavfi.astats.2.Noise_floor_count=1074.000000
			lavfi.astats.2.Entropy=0.788152
			""";

	@BeforeEach
	void init() {
		p = new LavfiMetadataFilterParser();
	}

	@Test
	void testAddLavfiRawLine_values() {
		RAW_LINES_OK.lines().forEach(p::addLavfiRawLine);
		p.close();

		assertEquals(List.of(
				new LavfiMtdValue<>(111, 4632, 4.632f, 2.204194f)),
				p.getBlockDetectReport());

		assertEquals(List.of(
				new LavfiMtdValue<>(1022, 981168, 20.441f, 1.000000f),
				new LavfiMtdValue<>(1299, 1247088, 25.981f, 0.992454f)),
				p.getAPhaseMeterReport());
		assertEquals(List.of(
				new LavfiMtdValue<>(114, 4757, 4.757f, 5.744382f),
				new LavfiMtdValue<>(3306, 3173808, 66.121f, 5.744382f)),
				p.getBlurDetectReport());

		final var l = new LavfiMtdAstatsChannel(
				0.000001f,
				-0.622282f,
				0l,
				2l,
				-78.266739f,
				708l,
				0.788192f,
				0, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN,
				0l, 0l, 0l, 0l, 0l,
				new LinkedHashMap<>());

		final var r = new LavfiMtdAstatsChannel(
				0.000002f,
				-0.622282f,
				0l,
				2l,
				-78.266739f,
				1074l,
				0.788152f,
				0, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN,
				0l, 0l, 0l, 0l, 0l,
				new LinkedHashMap<>());

		assertEquals(List.of(
				new LavfiMtdValue<>(87883, 84367728, 1757.66f, new LavfiMtdAstats(List.of(l, r)))),
				p.getAStatsReport());

		assertEquals(List.of(
				new LavfiMtdValue<>(1801, 60041, 60.041f,
						new LavfiMtdCropdetect(0, 479, 0, 479, 480, 480, 0, 0)),
				new LavfiMtdValue<>(7616, 317340, 317.34f,
						new LavfiMtdCropdetect(0, 1919, 0, 1079, 1920, 1072, 0, 4))),
				p.getCropDetectReport());

		final var single = new LavfiMtdIdetFrame(PROGRESSIVE, 0, 0, 40, 80);
		final var multiple = new LavfiMtdIdetFrame(PROGRESSIVE, 0, 0, 120, 0);
		final var repeated = new LavfiMtdIdetRepeatedFrame(NEITHER, 115, 2, 3);
		assertEquals(List.of(
				new LavfiMtdValue<>(119, 4966, 4.966f, new LavfiMtdIdet(single, multiple, repeated))),
				p.getIdetReport());

		assertEquals(
				List.of(new LavfiMtdValue<>(119, 4966, 4.966f, new LavfiMtdSiti(11.67f, 5.60f))),
				p.getSitiReport());

		assertEquals(List.of(
				new LavfiMtdValue<>(7616, 317340, 317.34f, new LavfiMtdR128(
						-36.183f, -36.182f, -24.545f, 17.320f,
						-36.190f, -18.870f,
						-17.522966f,
						new Stereo<>(-17.522966f, -18.636282f),
						-17.522966f,
						new Stereo<>(-17.522966f, -18.562359f)))),
				p.getR128Report());
	}

	@Test
	void testAddLavfiRawLine_events_all() {
		RAW_LINES_OK.lines().forEach(p::addLavfiRawLine);
		p.close();

		assertEquals(List.of(new LavfiMtdEvent("freeze", null, 0.757f, 4.049f)), p.getFreezeEvents());
		assertEquals(List.of(new LavfiMtdEvent("mono", null, 18.461f, 25.981f)), p.getMonoEvents());
		assertEquals(List.of(
				new LavfiMtdEvent("silence", "1", 65.1366f, 66.6474f),
				new LavfiMtdEvent("silence", "2", 65.1366f, 66.6474f)),
				p.getSilenceEvents());
		assertEquals(List.of(
				new LavfiMtdEvent("black", null, 0.007f, 0.09f),
				new LavfiMtdEvent("black", null, 4.507f, 4.841f)),
				p.getBlackEvents());
	}

	@Test
	void testAddLavfiRawLine_values_twoPass() {
		"""
				frame:0 pts:7 pts_time:0.007
				lavfi.black_start=0.007
				frame:2 pts:90 pts_time:0.09
				lavfi.black_end=0.09
				frame:114 pts:4757 pts_time:4.757
				lavfi.blur=5.744382
				frame:22 pts:981168 pts_time:20.441
				lavfi.aphasemeter.phase=1.000000
				lavfi.aphasemeter.mono_start=22.0
				frame:99 pts:1247088 pts_time:25.981
				lavfi.aphasemeter.phase=0.992454
				lavfi.aphasemeter.mono_end=23.0
				lavfi.aphasemeter.mono_duration=1.0
				""".lines().forEach(p::addLavfiRawLine);
		p.close();

		assertEquals(List.of(
				new LavfiMtdValue<>(22, 981168, 20.441f, 1.000000f),
				new LavfiMtdValue<>(99, 1247088, 25.981f, 0.992454f)),
				p.getAPhaseMeterReport());
		assertEquals(List.of(
				new LavfiMtdValue<>(114, 4757, 4.757f, 5.744382f)),
				p.getBlurDetectReport());
		assertEquals(List.of(new LavfiMtdEvent("mono", null, 22f, 23f)), p.getMonoEvents());
		assertEquals(List.of(
				new LavfiMtdEvent("black", null, 0.007f, 0.09f)),
				p.getBlackEvents());
	}

	@Test
	void testAddLavfiRawLine_events_missingStart() {
		"""
				frame:0 pts:7 pts_time:0.007
				lavfi.nope_start=0.007
				frame:2 pts:90 pts_time:0.09
				lavfi.black_end=0.09
				""".lines().forEach(p::addLavfiRawLine);
		p.close();
		assertEquals(List.of(), p.getBlackEvents());
	}

	@Test
	void testAddLavfiRawLine_events_missingEnd() {
		"""
				frame:0 pts:7 pts_time:0.007
				lavfi.black_start=0.007
				frame:2 pts:90 pts_time:0.09
				lavfi.nope_end=0.09
				""".lines().forEach(p::addLavfiRawLine);
		p.close();
		assertEquals(List.of(
				new LavfiMtdEvent("black", null, Duration.ofMillis(7), Duration.ZERO)),
				p.getBlackEvents());
	}

	@Test
	void testAddLavfiRawLine_noSi() {
		"""
				frame:119 pts:4966 pts_time:4.966
				lavfi.siti.ti=5.60
				""".lines().forEach(p::addLavfiRawLine);
		p.close();
		assertEquals(List.of(), p.getSitiReport());
	}

	@Test
	void testAddLavfiRawLine_noTi() {
		"""
				frame:119 pts:4966 pts_time:4.966
				lavfi.siti.si=5.60
				""".lines().forEach(p::addLavfiRawLine);
		p.close();
		assertEquals(List.of(), p.getSitiReport());
	}

	@Test
	void testClose() {
		assertEquals(p, p.close());
	}

	@Test
	void testParseFrameLine() {
		final var result = p.parseFrameLine("frame:1022    pts:981168   pts_time:20.441");
		assertEquals(new LavfiMtdPosition(1022, 981168l, 20.441f), result);
	}

	@Test
	void testComputeSitiStats() {
		"""
				frame:118 pts:4965 pts_time:4.965
				lavfi.siti.si=5
				lavfi.siti.ti=7
				frame:119 pts:4966 pts_time:4.966
				lavfi.siti.si=15
				lavfi.siti.ti=3
				""".lines().forEach(p::addLavfiRawLine);
		p.close();

		final var sum = p.computeSitiStats();
		assertNotNull(sum);

		final var dssSi = new DoubleSummaryStatistics();
		dssSi.accept(5);
		dssSi.accept(15);

		final var dssTi = new DoubleSummaryStatistics();
		dssTi.accept(7);
		dssTi.accept(3);

		assertEquals(new LavfiMtdSitiSummary(dssSi, dssTi).toString(), sum.toString());
	}

	@Test
	void testOneEmptyFrame() {
		p.addLavfiRawLine("frame:1022 pts:981168  pts_time:20.441");
		p.close();
		assertEquals(0, p.getReportCount());
		assertEquals(0, p.getEventCount());
	}

	@Test
	void testNoFrame() {
		assertThrows(IllegalArgumentException.class,
				() -> p.addLavfiRawLine("lavfi.siti.si=5"));
		assertEquals(0, p.getReportCount());
		assertEquals(0, p.getEventCount());
	}

	@Test
	void testNoLavfi() {
		assertDoesNotThrow(() -> """
				frame:1022 pts:981168  pts_time:20.441
				timecode=00:00:10:00
				aaaa.aphasemeter.phase=1.000000
				bbbb.aphasemeter.mono_start=11.461
				""".lines().forEach(p::addLavfiRawLine));
	}

}
