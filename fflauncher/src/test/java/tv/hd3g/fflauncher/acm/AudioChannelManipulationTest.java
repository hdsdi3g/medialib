package tv.hd3g.fflauncher.acm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static tv.hd3g.fflauncher.acm.AudioChannelManipulation.checkClassicStreamDesc;
import static tv.hd3g.fflauncher.acm.InputAudioChannelSelector.IN_CH0;
import static tv.hd3g.fflauncher.acm.InputAudioChannelSelector.IN_CH1;
import static tv.hd3g.fflauncher.enums.ChannelLayout.DOWNMIX;
import static tv.hd3g.fflauncher.enums.ChannelLayout.MONO;
import static tv.hd3g.fflauncher.enums.ChannelLayout.STEREO;

import java.util.LinkedHashMap;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import tv.hd3g.ffprobejaxb.FFprobeJAXB;
import tv.hd3g.ffprobejaxb.data.FFProbeStream;
import tv.hd3g.processlauncher.cmdline.Parameters;

class AudioChannelManipulationTest {
	static final InputAudioStream inMono0 = new InputAudioStream(MONO, 0, 1);
	static final InputAudioStream inMono1 = new InputAudioStream(MONO, 0, 2);
	static final InputAudioStream inMono2 = new InputAudioStream(MONO, 0, 3);
	static final InputAudioStream inMono3 = new InputAudioStream(MONO, 0, 4);
	static final InputAudioStream inStereo = new InputAudioStream(STEREO, 1, 0);
	static final OutputAudioStream outStreamMono0 = new OutputAudioStream(MONO, 0, 0)
			.mapChannel(inStereo, IN_CH0);
	static final OutputAudioStream outStreamMono1 = new OutputAudioStream(MONO, 0, 1)
			.mapChannel(inStereo, IN_CH1);
	static final OutputAudioStream outStreamStereo0 = new OutputAudioStream(STEREO, 1, 0)
			.mapChannel(inMono1, IN_CH0)
			.mapChannel(inMono0, IN_CH0);
	static final OutputAudioStream outStreamStereo1 = new OutputAudioStream(STEREO, 2, 1)
			.mapChannel(inMono2, IN_CH0)
			.mapChannel(inMono3, IN_CH0);
	static final OutputAudioStream outStreamSimpleMono = new OutputAudioStream(MONO, 0, 1)
			.mapChannel(inMono0, IN_CH0);

	@Test
	void testEmpty() {
		final var acm = new AudioChannelManipulation(List.of());
		assertTrue(acm.getToSplitFilterList().isEmpty());
		assertTrue(acm.getMergeJoinList().isEmpty());
		assertTrue(acm.getStreamRemapFilterMap().isEmpty());
		assertEquals(List.of(), acm.getAllOutputStreams());
	}

	@Test
	void testStraight_1stream() {
		final var outStreamSimpleStereo = new OutputAudioStream(STEREO, 0, 0)
				.mapChannel(inStereo, IN_CH0).mapChannel(inStereo, IN_CH1);

		final var acm = new AudioChannelManipulation(List.of(outStreamSimpleStereo));
		assertTrue(acm.getToSplitFilterList().isEmpty());
		assertTrue(acm.getMergeJoinList().isEmpty());
		assertTrue(acm.getStreamRemapFilterMap().isEmpty());
		assertEquals(List.of(outStreamSimpleStereo), acm.getAllOutputStreams());
	}

	@Test
	void testStraight_4streams() {
		final var outStreamSimpleMono0 = new OutputAudioStream(MONO, 0, 0)
				.mapChannel(inMono0, IN_CH0);
		final var outStreamSimpleMono1 = new OutputAudioStream(MONO, 0, 1)
				.mapChannel(inMono1, IN_CH0);
		final var outStreamSimpleMono2 = new OutputAudioStream(MONO, 0, 2)
				.mapChannel(inMono2, IN_CH0);
		final var outStreamSimpleMono3 = new OutputAudioStream(MONO, 0, 3)
				.mapChannel(inMono3, IN_CH0);

		final var sourceList = List.of(
				outStreamSimpleMono0, outStreamSimpleMono1, outStreamSimpleMono2, outStreamSimpleMono3);
		final var acm = new AudioChannelManipulation(sourceList);
		assertTrue(acm.getToSplitFilterList().isEmpty());
		assertTrue(acm.getMergeJoinList().isEmpty());
		assertTrue(acm.getStreamRemapFilterMap().isEmpty());
		assertEquals(sourceList, acm.getAllOutputStreams());
	}

	@Test
	void testStraight_remap() {
		final var outStreamSwappedStereo = new OutputAudioStream(STEREO, 0, 0)
				.mapChannel(inStereo, IN_CH1).mapChannel(inStereo, IN_CH0);

		final var acm = new AudioChannelManipulation(List.of(outStreamSwappedStereo));
		assertTrue(acm.getToSplitFilterList().isEmpty());
		assertTrue(acm.getMergeJoinList().isEmpty());
		assertEquals(1, acm.getStreamRemapFilterMap().size());
		final var remap = acm.getStreamRemapFilterMap().get(outStreamSwappedStereo);
		assertNotNull(remap);
		final var remapFilter = remap.toFilter();
		assertEquals("channelmap", remapFilter.getFilterName());
		assertEquals(List.of(inStereo.toMapReferenceAsInput()), remapFilter.getSourceBlocks());
		assertEquals(List.of("remap0"), remapFilter.getDestBlocks());
		assertEquals(List.of(outStreamSwappedStereo), acm.getAllOutputStreams());
	}

	@Test
	void testStraight_layoutChange() {
		final var outStreamDownmix = new OutputAudioStream(DOWNMIX, 0, 0)
				.mapChannel(inStereo, IN_CH0).mapChannel(inStereo, IN_CH1);

		final var acm = new AudioChannelManipulation(List.of(outStreamDownmix));
		assertTrue(acm.getToSplitFilterList().isEmpty());
		assertTrue(acm.getMergeJoinList().isEmpty());
		assertEquals(1, acm.getStreamRemapFilterMap().size());
		final var remap = acm.getStreamRemapFilterMap().get(outStreamDownmix);
		assertNotNull(remap);
		final var remapFilter = remap.toFilter();
		assertEquals("channelmap", remapFilter.getFilterName());
		assertEquals(List.of(inStereo.toMapReferenceAsInput()), remapFilter.getSourceBlocks());
		assertEquals(List.of("remap0"), remapFilter.getDestBlocks());
		assertEquals(List.of(outStreamDownmix), acm.getAllOutputStreams());
	}

	@Test
	void testACMSandbox_split() {
		final var acm = new AudioChannelManipulation(List.of(outStreamMono0, outStreamMono1));
		assertEquals(1, acm.getToSplitFilterList().size());
		final var splitFilter = acm.getToSplitFilterList().get(0).toFilter();
		assertEquals("channelsplit", splitFilter.getFilterName());
		assertEquals(List.of(inStereo.toMapReferenceAsInput()), splitFilter.getSourceBlocks());
		assertEquals(List.of("split0", "split1"), splitFilter.getDestBlocks());

		assertTrue(acm.getMergeJoinList().isEmpty());
		assertTrue(acm.getStreamRemapFilterMap().isEmpty());
		assertEquals(List.of(outStreamMono0, outStreamMono1), acm.getAllOutputStreams());
	}

	@Test
	void testACMSandbox_split_duplicate() {
		final var o = new OutputAudioStream(MONO, 0, 0)
				.mapChannel(inStereo, IN_CH0);
		final var outStreamMono0Again = new OutputAudioStream(MONO, 0, 1)
				.mapChannel(inStereo, IN_CH0);

		final var acm = new AudioChannelManipulation(List.of(o, outStreamMono0Again));
		assertEquals(2, acm.getToSplitFilterList().size());
		final var splitFilter0 = acm.getToSplitFilterList().get(0).toFilter();
		assertEquals("channelsplit", splitFilter0.getFilterName());
		assertEquals(List.of(inStereo.toMapReferenceAsInput()), splitFilter0.getSourceBlocks());
		assertEquals(List.of("split0"), splitFilter0.getDestBlocks());

		final var splitFilter1 = acm.getToSplitFilterList().get(1).toFilter();
		assertEquals("channelsplit", splitFilter1.getFilterName());
		assertEquals(List.of(inStereo.toMapReferenceAsInput()), splitFilter1.getSourceBlocks());
		assertEquals(List.of("split1"), splitFilter1.getDestBlocks());

		assertTrue(acm.getMergeJoinList().isEmpty());
		assertTrue(acm.getStreamRemapFilterMap().isEmpty());
		assertEquals(List.of(o, outStreamMono0Again), acm.getAllOutputStreams());
	}

	@Test
	void testACMSandbox_mergejoin() {
		final var acm = new AudioChannelManipulation(List.of(outStreamStereo0));
		assertEquals(1, acm.getMergeJoinList().size());
		final var amergeFilter = acm.getMergeJoinList().get(0).toAmergeFilter();
		assertEquals("amerge", amergeFilter.getFilterName());
		final var sources = List.of(inMono1.toMapReferenceAsInput(), inMono0.toMapReferenceAsInput());
		assertEquals(sources, amergeFilter.getSourceBlocks());
		assertEquals(List.of("mergjoin0"), amergeFilter.getDestBlocks());

		assertTrue(acm.getToSplitFilterList().isEmpty());
		assertTrue(acm.getStreamRemapFilterMap().isEmpty());
		assertEquals(List.of(outStreamStereo0), acm.getAllOutputStreams());
	}

	@Test
	void testACMSandbox_mergejoin_4() {
		final var acm = new AudioChannelManipulation(List.of(outStreamStereo0, outStreamStereo1));
		assertEquals(2, acm.getMergeJoinList().size());

		final var amergeFilter0 = acm.getMergeJoinList().get(0).toAmergeFilter();
		assertEquals("amerge", amergeFilter0.getFilterName());
		final var sources0 = List.of(inMono1.toMapReferenceAsInput(), inMono0.toMapReferenceAsInput());
		assertEquals(sources0, amergeFilter0.getSourceBlocks());
		assertEquals(List.of("mergjoin0"), amergeFilter0.getDestBlocks());

		final var amergeFilter1 = acm.getMergeJoinList().get(1).toAmergeFilter();
		assertEquals("amerge", amergeFilter1.getFilterName());
		final var sources1 = List.of(inMono2.toMapReferenceAsInput(), inMono3.toMapReferenceAsInput());
		assertEquals(sources1, amergeFilter1.getSourceBlocks());
		assertEquals(List.of("mergjoin1"), amergeFilter1.getDestBlocks());

		assertTrue(acm.getToSplitFilterList().isEmpty());
		assertTrue(acm.getStreamRemapFilterMap().isEmpty());
		assertEquals(List.of(outStreamStereo0, outStreamStereo1), acm.getAllOutputStreams());
	}

	@Test
	void testToString() {
		final var acm = new AudioChannelManipulation(List.of());
		assertNotNull(acm.toString());
	}

	@Test
	void testGetFilterChains_amerge() {
		final var acm = new AudioChannelManipulation(List.of(outStreamStereo0, outStreamMono0));
		final var fChain = acm.getFilterChains(false);
		assertEquals(2, fChain.getChainsCount());
		assertEquals(1, fChain.getChain(0).size());
		assertEquals(1, fChain.getChain(1).size());
		assertEquals("channelsplit", fChain.getChain(0).get(0).getFilterName());
		assertEquals("amerge", fChain.getChain(1).get(0).getFilterName());
	}

	@Test
	void testGetFilterChains_join() {
		final var acm = new AudioChannelManipulation(List.of(outStreamStereo0, outStreamMono0));
		final var fChain = acm.getFilterChains(true);
		assertEquals(2, fChain.getChainsCount());
		assertEquals(1, fChain.getChain(0).size());
		assertEquals(1, fChain.getChain(1).size());
		assertEquals("channelsplit", fChain.getChain(0).get(0).getFilterName());
		assertEquals("join", fChain.getChain(1).get(0).getFilterName());
	}

	@Nested
	class GetMapParameters {

		@Test
		void testGetMapParameters() {
			final var acm = new AudioChannelManipulation(List.of(outStreamStereo0, outStreamMono0, outStreamStereo1));
			final var pList = acm.getMapParameters();
			assertNotNull(pList);
			assertEquals(2, pList.size());
			assertEquals("-map [mergjoin0] -map [split0]", pList.get(0).toString());
			assertEquals("-map [mergjoin1]", pList.get(1).toString());
		}

		@Test
		void testGetMapParameters_directMap() {
			final var acm = new AudioChannelManipulation(List.of(outStreamSimpleMono));
			final var pList = acm.getMapParameters();
			assertNotNull(pList);
			assertEquals(1, pList.size());
			assertEquals("-map 0:1", pList.get(0).toString());
		}

		@Test
		void testGetMapParameters_manual() {
			final var acm = new AudioChannelManipulation(List.of(outStreamStereo0, outStreamMono0, outStreamStereo1));
			final var pList = acm.getMapParameters(
					(idx, outStream) -> Parameters.of(String.valueOf(idx), outStream.toMapReferenceAsInput()));
			assertNotNull(pList);
			assertEquals(2, pList.size());
			assertEquals("0 mergjoin0 1 split0", pList.get(0).toString());
			assertEquals("2 mergjoin1", pList.get(1).toString());
		}

		@Test
		void testRegexCheckClassicStreamDesc() {
			assertTrue(checkClassicStreamDesc.matcher("5:3").find());
			assertTrue(checkClassicStreamDesc.matcher("44:554").find());
			assertFalse(checkClassicStreamDesc.matcher("44:554:6").find());
			assertFalse(checkClassicStreamDesc.matcher("fd:4").find());
			assertFalse(checkClassicStreamDesc.matcher("f4:dd:5").find());
		}

		@Test
		void testGetMapParameters_ListString() {
			final var acm = new AudioChannelManipulation(List.of(outStreamStereo0, outStreamMono0, outStreamStereo1));
			final var pList = acm.getMapParameters(List.of("0:0", "0:1"));
			assertNotNull(pList);
			assertEquals(4, pList.size());
			assertEquals("-map 0:0", pList.get(0).toString());
			assertEquals("-map 0:1", pList.get(1).toString());
			assertEquals("-map [mergjoin0] -map [split0]", pList.get(2).toString());
			assertEquals("-map [mergjoin1]", pList.get(3).toString());
		}

		@Test
		void testGetMapParameters_InputStreams() {
			final var probe = Mockito.mock(FFprobeJAXB.class);

			final var streamTypeVideo = Mockito.mock(FFProbeStream.class);
			when(streamTypeVideo.index()).thenReturn(0);
			when(streamTypeVideo.codecType()).thenReturn("video");

			final var streamTypeAudio = Mockito.mock(FFProbeStream.class);
			when(streamTypeAudio.index()).thenReturn(1);
			when(streamTypeAudio.codecType()).thenReturn("audio");

			Mockito.when(probe.getStreams()).thenReturn(List.of(streamTypeVideo, streamTypeAudio));

			final var acm = new AudioChannelManipulation(List.of(outStreamStereo0, outStreamMono0, outStreamStereo1));
			final var tested = new LinkedHashMap<Integer, FFProbeStream>();
			final var pList = acm.getMapParameters(List.of(probe), (i, s) -> {
				tested.put(i, s);
				return true;
			});

			assertNotNull(pList);
			assertEquals(3, pList.size());
			assertEquals("-map 0:0", pList.get(0).toString());
			assertEquals("-map [mergjoin0] -map [split0]", pList.get(1).toString());
			assertEquals("-map [mergjoin1]", pList.get(2).toString());
			assertEquals(1, tested.size());
			assertTrue(tested.containsKey(0));
			assertEquals(streamTypeVideo, tested.get(0));
		}

	}

}
