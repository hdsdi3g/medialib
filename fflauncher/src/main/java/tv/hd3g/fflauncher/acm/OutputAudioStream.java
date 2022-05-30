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

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import tv.hd3g.fflauncher.enums.ChannelLayout;

/**
 * 0 is always the first !
 */
public class OutputAudioStream extends ACMAudioStream implements Comparable<OutputAudioStream> {
	private final Set<OutputAudioChannel> channels;

	private String mapReference;

	/**
	 * 0 is always the first !
	 */
	public OutputAudioStream(final ChannelLayout layout, final int fileIndex, final int streamIndex) {
		super(layout, fileIndex, streamIndex);
		channels = new HashSet<>();
	}

	Set<OutputAudioChannel> getChannels() {
		return channels;
	}

	@Override
	public String toMapReferenceAsInput() {
		return mapReference;
	}

	void setMapReference(final String mapReference) {
		this.mapReference = mapReference;
	}

	@Override
	public String toString() {
		final var mapRef = Optional.ofNullable(toMapReferenceAsInput()).orElse("");
		return getFileIndex() + ":" + getStreamIndex() + "(" + getLayout() + ")" + mapRef;
	}

	void addChannel(final OutputAudioChannel channel) {
		channels.add(channel);
	}

	@Override
	public int compareTo(final OutputAudioStream o) {
		final var compareFile = Integer.compare(getFileIndex(), o.getFileIndex());
		if (compareFile == 0) {
			return Integer.compare(getStreamIndex(), o.getStreamIndex());
		}
		return compareFile;
	}

	@Override
	public boolean equals(final Object obj) {// NOSONAR S1206
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final var other = (OutputAudioStream) obj;
		return Objects.equals(channels, other.channels) && Objects.equals(mapReference, other.mapReference);
	}

	/**
	 * @return this
	 */
	public OutputAudioStream mapChannel(final InputAudioStream inputAudioStream,
	                                    final InputAudioChannelSelector chInIndex,
	                                    final OutputAudioChannelSelector chOutIndex) {
		if (channels.size() == getLayout().getChannelSize()) {
			throw new IllegalArgumentException("Can't add channel in stream, the stream is full (" +
			                                   channels.size() + ") for layout " + getLayout());
		}
		final var created = new OutputAudioChannel(inputAudioStream, chInIndex, this, chOutIndex);
		channels.add(created);
		return this;
	}

	/**
	 * chOutIndex will be auto-incr
	 * 0 is always the first !
	 * @return this
	 */
	public OutputAudioStream mapChannel(final InputAudioStream inputAudioStream,
	                                    final InputAudioChannelSelector chInIndex) {
		if (channels.size() == getLayout().getChannelSize()) {
			throw new IllegalArgumentException("Can't add channel in stream, the stream is full (" +
			                                   channels.size() + ") for layout " + getLayout());
		}
		if (chInIndex.getPosInStream() >= inputAudioStream.getLayout().getChannelSize()) {
			throw new IllegalArgumentException("Can't add channel in stream, the selected channel in input stream "
			                                   + chInIndex.getPosInStream() + " don't exists for layout "
			                                   + inputAudioStream.getLayout());
		}

		final var created = new OutputAudioChannel(inputAudioStream, chInIndex, this,
		        new OutputAudioChannelSelector(channels.size()));
		channels.add(created);
		return this;
	}

	class OutputAudioChannel implements Comparable<OutputAudioChannel> {

		private final InputAudioStream inputAudioStream;
		private final InputAudioChannelSelector chInIndex;

		private final OutputAudioStream outputAudioStream;
		private final OutputAudioChannelSelector chOutIndex;

		OutputAudioChannel(final InputAudioStream inputAudioStream,
		                   final InputAudioChannelSelector chInIndex,
		                   final OutputAudioStream outputAudioStream,
		                   final OutputAudioChannelSelector chOutIndex) {
			this.inputAudioStream = Objects.requireNonNull(inputAudioStream);
			this.chInIndex = Objects.requireNonNull(chInIndex);
			final var inChSize = inputAudioStream.getLayout().getChannelSize();
			if (chInIndex.getPosInStream() >= inChSize) {
				throw new IllegalArgumentException("Can't found in channel #" + inChSize + " in "
				                                   + inputAudioStream + " stream");
			}

			this.outputAudioStream = Objects.requireNonNull(outputAudioStream);
			this.chOutIndex = Objects.requireNonNull(chOutIndex);
			final var outChSize = outputAudioStream.getLayout().getChannelSize();
			if (chOutIndex.getPosInStream() >= outChSize) {
				throw new IllegalArgumentException("Can't found out channel #" + outChSize + " in "
				                                   + outputAudioStream + " stream");
			}
		}

		/**
		 * By outputAudioStream and chOutIndex
		 */
		@Override
		public int compareTo(final OutputAudioChannel o) {
			final var outStreams = outputAudioStream.compareTo(o.outputAudioStream);
			if (outStreams == 0) {
				return chOutIndex.compareTo(o.chOutIndex);
			} else {
				return outStreams;
			}
		}

		InputAudioStream getInputAudioStream() {
			return inputAudioStream;
		}

		InputAudioChannelSelector getChInIndex() {
			return chInIndex;
		}

		OutputAudioStream getOutputAudioStream() {
			return outputAudioStream;
		}

		OutputAudioChannelSelector getChOutIndex() {
			return chOutIndex;
		}

		@Override
		public int hashCode() {
			final var prime = 31;
			var result = 1;
			result = prime * result + getEnclosingInstance().hashCode();
			result = prime * result + Objects.hash(chInIndex, chOutIndex, inputAudioStream, outputAudioStream);
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final var other = (OutputAudioChannel) obj;
			if (!getEnclosingInstance().equals(other.getEnclosingInstance())) {
				return false;
			}
			return Objects.equals(chInIndex, other.chInIndex) && Objects.equals(chOutIndex, other.chOutIndex) && Objects
			        .equals(inputAudioStream, other.inputAudioStream) && Objects.equals(outputAudioStream,
			                other.outputAudioStream);
		}

		@Override
		public String toString() {
			return inputAudioStream + "." + chInIndex + "->" + outputAudioStream + "." + chOutIndex;
		}

		protected OutputAudioStream getEnclosingInstance() {
			return OutputAudioStream.this;
		}
	}
}