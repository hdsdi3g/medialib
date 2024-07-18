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

import tv.hd3g.processlauncher.cmdline.Parameters;

public class FFprobe extends FFbase {

	private static final String P_SHOW_FORMAT = "-show_format";
	private static final String P_SHOW_DATA = "-show_data";
	private static final String P_SHOW_ERROR = "-show_error";
	private static final String P_SHOW_FRAMES = "-show_frames";
	private static final String P_SHOW_LOG = "-show_log";
	private static final String P_SHOW_PACKETS = "-show_packets";
	private static final String P_SHOW_PROGRAMS = "-show_programs";
	private static final String P_SHOW_STREAMS = "-show_streams";
	private static final String P_SHOW_CHAPTERS = "-show_chapters";
	private static final String P_PRINT_FORMAT = "-print_format";
	private static final String P_PRETTY = "-pretty";

	public FFprobe(final String execName) {
		super(execName, new Parameters());
	}

	public FFprobe(final String execName, final Parameters parameters) {
		super(execName, parameters);
	}

	/**
	 * -pretty prettify the format of displayed values, make it more human readable
	 */
	public FFprobe setPretty() {
		getInternalParameters().ifHasNotParameter(() -> getInternalParameters().addParameters(P_PRETTY), P_PRETTY);
		return this;
	}

	public boolean isPretty() {
		return getInternalParameters().hasParameters(P_PRETTY);
	}

	public enum FFPrintFormat {
		BY_DEFAULT {
			@Override
			public String toString() {
				return "default";
			}
		},
		COMPACT,
		CSV,
		FLAT,
		INI,
		JSON,
		XML;

		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}

	/**
	 * -print_format format set the output printing format
	 */
	public FFprobe setPrintFormat(final FFPrintFormat printFormat) {
		getInternalParameters().ifHasNotParameter(() -> getInternalParameters().addParameters(P_PRINT_FORMAT,
				printFormat.toString().toLowerCase()), P_PRINT_FORMAT, "-of");
		return this;
	}

	public boolean hasPrintFormat() {
		return getInternalParameters().hasParameters(P_PRINT_FORMAT, "-of");
	}

	/**
	 * -show_format show format/container INFO
	 */
	public FFprobe setShowFormat() {
		getInternalParameters().ifHasNotParameter(() -> getInternalParameters().addParameters(P_SHOW_FORMAT),
				P_SHOW_FORMAT);
		return this;
	}

	public boolean isShowFormat() {
		return getInternalParameters().hasParameters(P_SHOW_FORMAT);
	}

	/**
	 * -show_data show packets data
	 */
	public FFprobe setShowData() {
		getInternalParameters().ifHasNotParameter(() -> getInternalParameters().addParameters(P_SHOW_DATA),
				P_SHOW_DATA);
		return this;
	}

	public boolean isShowData() {
		return getInternalParameters().hasParameters(P_SHOW_DATA);
	}

	/**
	 * -show_error show probing ERROR
	 */
	public FFprobe setShowError() {
		getInternalParameters().ifHasNotParameter(() -> getInternalParameters().addParameters(P_SHOW_ERROR),
				P_SHOW_ERROR);
		return this;
	}

	public boolean isShowError() {
		return getInternalParameters().hasParameters(P_SHOW_ERROR);
	}

	/**
	 * -show_frames show frames INFO
	 */
	public FFprobe setShowFrames() {
		getInternalParameters().ifHasNotParameter(() -> getInternalParameters().addParameters(P_SHOW_FRAMES),
				P_SHOW_FRAMES);
		return this;
	}

	public boolean isShowFrames() {
		return getInternalParameters().hasParameters(P_SHOW_FRAMES);
	}

	/**
	 * -show_log show log
	 */
	public FFprobe setShowLog() {
		getInternalParameters().ifHasNotParameter(() -> getInternalParameters().addParameters(P_SHOW_LOG), P_SHOW_LOG);
		return this;
	}

	public boolean isShowLog() {
		return getInternalParameters().hasParameters(P_SHOW_LOG);
	}

	/**
	 * -show_packets show packets INFO
	 */
	public FFprobe setShowPackets() {
		getInternalParameters().ifHasNotParameter(() -> getInternalParameters().addParameters(P_SHOW_PACKETS),
				P_SHOW_PACKETS);
		return this;
	}

	public boolean isShowPackets() {
		return getInternalParameters().hasParameters(P_SHOW_PACKETS);
	}

	/**
	 * -show_programs show programs INFO
	 */
	public FFprobe setShowPrograms() {
		getInternalParameters().ifHasNotParameter(() -> getInternalParameters().addParameters(P_SHOW_PROGRAMS),
				P_SHOW_PROGRAMS);
		return this;
	}

	public boolean isShowPrograms() {
		return getInternalParameters().hasParameters(P_SHOW_PROGRAMS);
	}

	/**
	 * -show_streams show streams INFO
	 */
	public FFprobe setShowStreams() {
		getInternalParameters().ifHasNotParameter(() -> getInternalParameters().addParameters(P_SHOW_STREAMS),
				P_SHOW_STREAMS);
		return this;
	}

	public boolean isShowStreams() {
		return getInternalParameters().hasParameters(P_SHOW_STREAMS);
	}

	/**
	 * -show_chapters show chapters INFO
	 */
	public FFprobe setShowChapters() {
		getInternalParameters().ifHasNotParameter(() -> getInternalParameters().addParameters(P_SHOW_CHAPTERS),
				P_SHOW_CHAPTERS);
		return this;
	}

	public boolean isShowChapters() {
		return getInternalParameters().hasParameters(P_SHOW_CHAPTERS);
	}

}
