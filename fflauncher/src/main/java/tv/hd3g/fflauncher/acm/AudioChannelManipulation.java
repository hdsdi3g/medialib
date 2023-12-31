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
 * Copyright (C) hdsdi3g for hd3g.tv 2020
 *
 */
package tv.hd3g.fflauncher.acm;

import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import tv.hd3g.fflauncher.acm.ACMSplitInStreamDefinitionFilter.SplittedOut;
import tv.hd3g.fflauncher.acm.OutputAudioStream.OutputAudioChannel;
import tv.hd3g.fflauncher.enums.ChannelLayout;
import tv.hd3g.fflauncher.filtering.FilterChains;
import tv.hd3g.ffprobejaxb.FFprobeJAXB;
import tv.hd3g.ffprobejaxb.FFprobeReference;
import tv.hd3g.ffprobejaxb.data.FFProbeStream;
import tv.hd3g.processlauncher.cmdline.Parameters;

/**
 * 0 is always the first !
 * One by setup and source file (OutputAudioStream is relative to input stream, retative to sources files).
 */
public class AudioChannelManipulation {

	static final Pattern checkClassicStreamDesc = Pattern.compile("^[0-9]*\\:[0-9]*$");

	private final List<ACMSplitInStreamDefinitionFilter> toSplitFilterList;
	private final List<ACMMergeJoinToStreamDefinitionFilter> amergeJoinList;
	private final Map<OutputAudioStream, ACMRemapDefinitionFilter> streamRemapFilterMap;
	private final List<OutputAudioStream> allOutputStreamList;

	public AudioChannelManipulation(final List<OutputAudioStream> allOutputStreamList) {
		this.allOutputStreamList = allOutputStreamList;

		/**
		 * Manage direct map
		 */
		final var mapDirectlyMap = new LinkedHashMap<OutputAudioStream, ACMMapDirectly>();
		final var streamRemapMap = new LinkedHashMap<OutputAudioStream, ACMRemapDefinitionFilter>();
		streamRemapFilterMap = Collections.unmodifiableMap(streamRemapMap);

		allOutputStreamList.stream().forEach(outStream -> {
			final var layoutOutChannelSize = outStream.getLayout().getChannelSize();
			final var allSelectedInStream = outStream.getChannels().stream()
					.map(OutputAudioChannel::getInputAudioStream)
					.distinct()
					.toList();
			if (allSelectedInStream.size() > 1) {
				/**
				 * Different source mapping, no optimizations here.
				 */
				return;
			}
			final var selectedInStream = allSelectedInStream.get(0);
			if (selectedInStream.getLayout().getChannelSize() != layoutOutChannelSize) {
				/**
				 * Not all channels will be consumed, no optimizations here.
				 */
				return;
			}
			if (selectedInStream.getLayout() == outStream.getLayout()) {
				final var straightMapping = outStream.getChannels().stream().noneMatch(outChannel -> {
					final var chInPos = outChannel.getChInIndex().getPosInStream();
					final var chOutPos = outChannel.getChOutIndex().getPosInStream();
					return chInPos != chOutPos;
				});
				if (straightMapping) {
					/**
					 * same layout, same channel count, same channel map -> copy
					 */
					mapDirectlyMap.put(outStream, new ACMMapDirectly(selectedInStream, outStream));
					return;
				}
			}
			/**
			 * reorder with channelmap
			 */
			streamRemapMap.put(outStream, new ACMRemapDefinitionFilter(selectedInStream, outStream));
		});

		final var toProcess = allOutputStreamList.stream()
				.flatMap(s -> s.getChannels().stream())
				.sorted()
				.filter(ch -> mapDirectlyMap.containsKey(ch.getOutputAudioStream()) == false
							  && streamRemapMap.containsKey(ch.getOutputAudioStream()) == false)
				.toList();

		/**
		 * Manage split operations
		 */
		final var toSplit = new ACMAudioStreamToSplitList();
		toSplitFilterList = Collections.unmodifiableList(toSplit);

		final var counter = new AtomicInteger(0);
		toProcess.stream()
				.filter(ch -> ch.getInputAudioStream().getLayout().isMonoLayout() == false)
				.forEach(ch -> {
					final var optSplit = toSplit.findFirst(ch.getInputAudioStream());
					if (optSplit.isPresent()) {
						final var splitterByInStream = optSplit.get();
						if (splitterByInStream.getSplittedOut().containsKey(ch.getChInIndex())) {
							toSplit.add(new ACMSplitInStreamDefinitionFilter(ch, counter.getAndIncrement()));
						} else {
							splitterByInStream.getSplittedOut().put(ch.getChInIndex(),
									splitterByInStream.new SplittedOut(ch, counter.getAndIncrement()));
						}
					} else {
						toSplit.add(new ACMSplitInStreamDefinitionFilter(ch, counter.getAndIncrement()));
					}
				});

		final var mapDirectlySplited = new ArrayList<SplittedOut>();
		amergeJoinList = new ArrayList<>();

		allOutputStreamList.stream()
				.filter(outStream -> mapDirectlyMap.containsKey(outStream) == false
									 && streamRemapMap.containsKey(outStream) == false)
				.forEach(outStream -> {
					if (outStream.getLayout() == ChannelLayout.MONO) {
						/**
						 * One channel: no merge/join to do
						 */
						final var outChannel = outStream.getChannels().stream().findFirst().orElse(null);
						final var splittedOut = toSplit.search(outChannel)
								.orElseThrow(() -> new IllegalStateException("nCh to 1Ch, missing toSplit list item"));
						mapDirectlySplited.add(splittedOut);
						return;
					}
					/**
					 * Merge/join channels
					 */
					final var mergeJoinCurrentList = new ArrayList<ACMExportableMapReference>();
					outStream.getChannels().stream().sorted().forEach(channel -> {
						final var toMergeJoin = toSplit.search(channel)
								.map(ACMExportableMapReference.class::cast)
								.orElseGet(channel::getInputAudioStream);
						mergeJoinCurrentList.add(toMergeJoin);
					});
					amergeJoinList.add(new ACMMergeJoinToStreamDefinitionFilter(mergeJoinCurrentList, outStream));
				});

		/**
		 * Set Index pos on items
		 */
		Stream.of(amergeJoinList,
				mapDirectlySplited,
				streamRemapMap.values().stream().toList())
				.forEach(l -> {
					for (var pos = 0; pos < l.size(); pos++) {
						l.get(pos).setAbsoluteIndex(pos);
					}
				});

		/**
		 * Filter out to map
		 */
		final var collectorForReferenceByOutStreams = Collectors.toUnmodifiableMap(
				k -> ((ACMLinkableOutStreamReference) k).getLinkableOutStreamReference(),
				k -> ((ACMExportableMapReference) k).toMapReferenceAsInput());

		final var referenceByOutStreams = List.of(
				amergeJoinList.stream().collect(collectorForReferenceByOutStreams),
				mapDirectlySplited.stream().collect(collectorForReferenceByOutStreams),
				streamRemapMap.values().stream().collect(collectorForReferenceByOutStreams),
				mapDirectlyMap.values().stream().collect(collectorForReferenceByOutStreams));

		/**
		 * Set setMapReference for all out streams
		 */
		allOutputStreamList.forEach(outStream -> {
			final var streamRefs = referenceByOutStreams.stream()
					.filter(ref -> ref.containsKey(outStream))
					.map(ref -> ref.get(outStream))
					.toList();
			outStream.setMapReference(streamRefs.get(0));
		});
	}

	@Override
	public String toString() {
		final var sb = new StringBuilder();
		sb.append("split: ");
		sb.append(toSplitFilterList);
		sb.append(", merge/join: ");
		sb.append(amergeJoinList);
		sb.append(", remap: ");
		sb.append(streamRemapFilterMap);
		sb.append(", map: ");
		sb.append(allOutputStreamList);
		return sb.toString();
	}

	List<ACMSplitInStreamDefinitionFilter> getToSplitFilterList() {
		return toSplitFilterList;
	}

	List<ACMMergeJoinToStreamDefinitionFilter> getMergeJoinList() {
		return amergeJoinList;
	}

	Map<OutputAudioStream, ACMRemapDefinitionFilter> getStreamRemapFilterMap() {
		return streamRemapFilterMap;
	}

	List<OutputAudioStream> getAllOutputStreams() {
		return allOutputStreamList;
	}

	/**
	 * You should add yourself "-map stream_ref"
	 * @param parametersMapper (OutputAudioStream absolute index, OutputAudioStream) -&gt; Parameters to appends
	 * @return one item by output file
	 */
	public List<Parameters> getMapParameters(final BiFunction<Integer, OutputAudioStream, Parameters> parametersMapper) {
		final var fileParameters = new ArrayList<Parameters>();

		Parameters currentParameters = null;
		for (var pos = 0; pos < allOutputStreamList.size(); pos++) {
			final var outStream = allOutputStreamList.get(pos);
			if (fileParameters.size() - 1 != outStream.getFileIndex()) {
				currentParameters = new Parameters();
				fileParameters.add(currentParameters);
			}
			final var map = parametersMapper.apply(pos, outStream);
			Objects.requireNonNull(currentParameters).addAllFrom(map);
		}
		return unmodifiableList(fileParameters);
	}

	private static Parameters getMapStreamParam(final String mapRef) {
		if (checkClassicStreamDesc.matcher(mapRef).find()) {
			return Parameters.of("-map", mapRef);
		}
		return Parameters.of("-map", "[" + mapRef + "]");
	}

	/**
	 * Only add -map "-map stream_ref"...
	 * @return one item by output file
	 */
	public List<Parameters> getMapParameters() {
		return getMapParameters((pos, astream) -> getMapStreamParam(astream.toMapReferenceAsInput()));
	}

	public List<Parameters> getMapParameters(final List<String> prependToMapList) {
		final var prepend = prependToMapList.stream().map(AudioChannelManipulation::getMapStreamParam);
		return Stream.of(prepend, getMapParameters().stream())
				.flatMap(p -> p)
				.toList();
	}

	/**
	 * @param sourceFiles original file analysing
	 * @param addNonAudioStreamFromSources: File index in sourceFiles, non-audio stream in file -&gt; add to map list
	 * @return add non-audio sources (video, data) + getMapParameters
	 */
	public List<Parameters> getMapParameters(final List<FFprobeJAXB> sourceFiles,
											 final BiPredicate<Integer, FFProbeStream> addNonAudioStreamFromSources) {

		final var selectedFileStreams = new LinkedHashMap<Integer, FFProbeStream>();
		for (var pos = 0; pos < sourceFiles.size(); pos++) {
			final var fileIndex = pos;
			sourceFiles.get(pos).getStreams().stream()
					.filter(s -> FFprobeReference.filterVideoStream.test(s) || FFprobeReference.filterDataStream.test(
							s))
					.filter(s -> addNonAudioStreamFromSources.test(fileIndex, s))
					.forEach(s -> selectedFileStreams.put(fileIndex, s));
		}

		return getMapParameters(selectedFileStreams.entrySet().stream()
				.map(entry -> {
					final var fileIndex = entry.getKey();
					final var streamInFile = entry.getValue();
					return fileIndex + ":" + streamInFile.index();
				})
				.toList());
	}

	public FilterChains getFilterChains(final boolean useJoinInsteadOfMerge) {
		final var chain = new FilterChains();

		toSplitFilterList.forEach(s -> chain.addFilterInLastChain(s.toFilter(), true));
		if (useJoinInsteadOfMerge) {
			amergeJoinList.forEach(s -> chain.addFilterInLastChain(s.toJoinFilter(), true));
		} else {
			amergeJoinList.forEach(s -> chain.addFilterInLastChain(s.toAmergeFilter(), true));
		}
		streamRemapFilterMap.values()
				.stream()
				.forEach(remapDefinitionFilter -> chain.addFilterInLastChain(remapDefinitionFilter.toFilter(), true));
		return chain;
	}

}
