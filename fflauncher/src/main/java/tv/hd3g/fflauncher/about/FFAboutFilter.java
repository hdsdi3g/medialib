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
package tv.hd3g.fflauncher.about;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import tv.hd3g.fflauncher.UnknownFormatException;
import tv.hd3g.fflauncher.enums.FilterConnectorType;

public class FFAboutFilter {

	static List<FFAboutFilter> parseFilters(final List<String> lines) {
		return lines.stream()
				.map(String::trim)
				.filter(line -> (line.toLowerCase().startsWith("Filters:".toLowerCase()) == false))
				.filter(line -> (line.startsWith("---") == false))
				.filter(line -> (line.indexOf('=') == -1))
				.map(FFAboutFilter::new)
				.toList();
	}

	/**
	 * Like "aeval"
	 */
	private final String tag;

	/**
	 * Like "Filter audio signal according to a specified expression."
	 */
	private final String longName;

	private final boolean timelineSupport;
	private final boolean sliceThreading;
	private final boolean commandSupport;

	private final FilterConnectorType sourceConnector;
	private final FilterConnectorType destConnector;

	private final int sourceConnectorsCount;
	private final int destConnectorsCount;

	FFAboutFilter(final String line) {

		final var lineBlocs = Arrays.stream(line.split(" "))
				.filter(lb -> lb.trim().equals("") == false)
				.map(String::trim)
				.toList();

		if (lineBlocs.size() < 4) {
			throw new UnknownFormatException("Can't parse line: \"" + line + "\"");
		}

		tag = lineBlocs.get(1);
		longName = lineBlocs.stream()
				.filter(lb -> lb.trim().equals("") == false)
				.skip(3)
				.collect(Collectors.joining(" "));

		timelineSupport = lineBlocs.get(0).contains("T");
		sliceThreading = lineBlocs.get(0).contains("S");
		commandSupport = lineBlocs.get(0).contains("C");

		final var filter_graph = lineBlocs.get(2);

		final var pos = filter_graph.indexOf("->");
		final var s_source_connector = filter_graph.substring(0, pos);
		final var s_dest_connector = filter_graph.substring(pos + "->".length());

		if (s_source_connector.contains("A")) {
			sourceConnector = FilterConnectorType.AUDIO;
		} else if (s_source_connector.contains("V")) {
			sourceConnector = FilterConnectorType.VIDEO;
		} else if (s_source_connector.contains("N")) {
			sourceConnector = FilterConnectorType.DYNAMIC;
		} else if (s_source_connector.contains("|")) {
			sourceConnector = FilterConnectorType.SOURCE_SINK;
		} else {
			throw new UnknownFormatException("Invalid line : \"" + line + "\", invalid filter_graph sourceConnector");
		}

		if (s_dest_connector.contains("A")) {
			destConnector = FilterConnectorType.AUDIO;
		} else if (s_dest_connector.contains("V")) {
			destConnector = FilterConnectorType.VIDEO;
		} else if (s_dest_connector.contains("N")) {
			destConnector = FilterConnectorType.DYNAMIC;
		} else if (s_dest_connector.contains("|")) {
			destConnector = FilterConnectorType.SOURCE_SINK;
		} else {
			throw new UnknownFormatException("Invalid line : \"" + line + "\", invalid filter_graph sourceConnector");
		}

		sourceConnectorsCount = s_source_connector.length();
		destConnectorsCount = s_dest_connector.length();
	}

	@Override
	public String toString() {
		final var sb = new StringBuilder();

		sb.append(longName);
		sb.append(" [");
		sb.append(tag);
		sb.append("] ");

		sb.append(sourceConnector.toString().toLowerCase());

		if (sourceConnectorsCount > 1) {
			sb.append(" (");
			sb.append(sourceConnectorsCount);
			sb.append(")");
		}

		sb.append(" -> ");
		sb.append(destConnector.toString().toLowerCase());

		if (destConnectorsCount > 1) {
			sb.append(" (");
			sb.append(destConnectorsCount);
			sb.append(")");
		}

		if (timelineSupport) {
			sb.append(" <timeline support>");
		}
		if (sliceThreading) {
			sb.append(" <slice threading>");
		}
		if (commandSupport) {
			sb.append(" <command support>");
		}

		return sb.toString();
	}

	public String getTag() {
		return tag;
	}

	public String getLongName() {
		return longName;
	}

	public boolean isTimelineSupport() {
		return timelineSupport;
	}

	public boolean isSliceThreading() {
		return sliceThreading;
	}

	public boolean isCommandSupport() {
		return commandSupport;
	}

	public FilterConnectorType getSourceConnector() {
		return sourceConnector;
	}

	public FilterConnectorType getDestConnector() {
		return destConnector;
	}

	public int getSourceConnectorsCount() {
		return sourceConnectorsCount;
	}

	public int getDestConnectorsCount() {
		return destConnectorsCount;
	}

}
