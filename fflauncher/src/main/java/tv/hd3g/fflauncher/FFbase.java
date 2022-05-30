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
import static java.util.stream.Collectors.toUnmodifiableList;

import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import tv.hd3g.fflauncher.about.FFAbout;
import tv.hd3g.fflauncher.enums.FFLogLevel;
import tv.hd3g.fflauncher.enums.FilterConnectorType;
import tv.hd3g.fflauncher.filtering.Filter;
import tv.hd3g.fflauncher.filtering.FilterChains;
import tv.hd3g.processlauncher.ProcesslauncherBuilder;
import tv.hd3g.processlauncher.cmdline.ExecutableFinder;
import tv.hd3g.processlauncher.cmdline.Parameters;

public class FFbase extends ConversionTool {

	private static final String IN_AUTOMATIC = "IN_AUTOMATIC_";
	private static final String P_LOGLEVEL = "-loglevel";
	private static final String P_HIDE_BANNER = "-hide_banner";
	private FFAbout about;

	public FFbase(final String execName, final Parameters parameters) {
		super(execName, parameters);
	}

	@Override
	public void beforeRun(final ProcesslauncherBuilder processBuilder) {
		super.beforeRun(processBuilder);
		if (processBuilder.getEnvironmentVar("AV_LOG_FORCE_COLOR") == null) {
			processBuilder.setEnvironmentVarIfNotFound("AV_LOG_FORCE_NOCOLOR", "1");
		}
	}

	@Override
	protected PrintStream getStdErrPrintStreamToDisplayLinesEvents() {
		return System.out;// NOSONAR
	}

	/**
	 * Add like -loglevel repeat+level+verbose
	 */
	public FFbase setLogLevel(final FFLogLevel level, final boolean repeat, final boolean display_level) {
		parameters.ifHasNotParameter(() -> {
			final var sb = new StringBuilder();
			if (repeat) {
				sb.append("repeat+");
			}
			if (display_level) {
				sb.append("level+");
			}
			sb.append(level);
			parameters.prependParameters(P_LOGLEVEL, sb.toString());
		}, P_LOGLEVEL, "-v");

		return this;
	}

	public boolean isLogLevelSet() {
		return parameters.hasParameters(P_LOGLEVEL, "-v");
	}

	public FFbase setHidebanner() {
		parameters.ifHasNotParameter(() -> parameters.prependParameters(P_HIDE_BANNER), P_HIDE_BANNER);
		return this;
	}

	public boolean isHidebanner() {
		return parameters.hasParameters(P_HIDE_BANNER);
	}

	public FFbase setOverwriteOutputFiles() {
		parameters.ifHasNotParameter(() -> parameters.prependParameters("-y"), "-y");
		return this;
	}

	public boolean isOverwriteOutputFiles() {
		return parameters.hasParameters("-y");
	}

	public FFbase setNeverOverwriteOutputFiles() {
		parameters.ifHasNotParameter(() -> parameters.prependParameters("-n"), "-n");
		return this;
	}

	public boolean isNeverOverwriteOutputFiles() {
		return parameters.hasParameters("-n");
	}

	/**
	 * Define cmd var name like &lt;%IN_AUTOMATIC_n%&gt; with "n" the # of setted sources.
	 * Add -i parameter
	 * Add now in current Parameters the new add var only if not exists (you should call fixIOParametredVars, if you have add manually vars in Parametres)
	 */
	public FFbase addSimpleInputSource(final String sourceName, final String... sourceOptions) {
		requireNonNull(sourceName, "\"sourceName\" can't to be null");

		if (sourceOptions == null) {
			return addSimpleInputSource(sourceName, Collections.emptyList());
		} else {
			return addSimpleInputSource(sourceName, Arrays.stream(sourceOptions).collect(Collectors
			        .toUnmodifiableList()));
		}
	}

	/**
	 * Define cmd var name like &lt;%IN_AUTOMATIC_n%&gt; with "n" the # of setted sources.
	 * Add -i parameter
	 * Add now in current Parameters the new add var only if not exists (you should call fixIOParametredVars, if you have add manually vars in Parametres)
	 */
	public FFbase addSimpleInputSource(final File file, final String... sourceOptions) {
		requireNonNull(file, "\"file\" can't to be null");

		if (sourceOptions == null) {
			return addSimpleInputSource(file, Collections.emptyList());
		} else {
			return addSimpleInputSource(file, Arrays.stream(sourceOptions).collect(Collectors.toUnmodifiableList()));
		}
	}

	/**
	 * Define cmd var name like &lt;%IN_AUTOMATIC_n%&gt; with "n" the # of setted sources.
	 * Add -i parameter
	 * Add now in current Parameters the new add var only if not exists (you should call fixIOParametredVars, if you have add manually vars in Parametres)
	 */
	public FFbase addSimpleInputSource(final String sourceName, final List<String> sourceOptions) {
		requireNonNull(sourceName, "\"sourceName\" can't to be null");
		requireNonNull(sourceOptions, "\"sourceOptions\" can't to be null");

		final var varname = parameters.tagVar(IN_AUTOMATIC + inputSources.size());
		addVarInParametersIfNotExists(varname);
		addInputSource(sourceName, varname,
		        Stream.concat(sourceOptions.stream(), Stream.of("-i")).collect(toUnmodifiableList()));
		return this;
	}

	/**
	 * Define cmd var name like &lt;%IN_AUTOMATIC_n%&gt; with "n" the # of setted sources.
	 * Add -i parameter
	 * Add now in current Parameters the new add var only if not exists (you should call fixIOParametredVars, if you have add manually vars in Parametres)
	 */
	public FFbase addSimpleInputSource(final File file, final List<String> sourceOptions) {
		requireNonNull(file, "\"file\" can't to be null");
		requireNonNull(sourceOptions, "\"sourceOptions\" can't to be null");

		final var varname = parameters.tagVar(IN_AUTOMATIC + inputSources.size());
		addVarInParametersIfNotExists(varname);
		addInputSource(file, varname,
		        Stream.concat(sourceOptions.stream(), Stream.of("-i")).collect(toUnmodifiableList()));
		return this;
	}

	private void addVarInParametersIfNotExists(final String varname) {
		if (parameters.getParameters().contains(varname) == false) {
			final var defaultInVar = parameters.extractVarNameFromTaggedParameter(varname);
			if (defaultInVar.startsWith(IN_AUTOMATIC)) {
				final var indexInput = Integer.valueOf(defaultInVar.substring(IN_AUTOMATIC.length()));

				if (indexInput > 0) {
					final var expectedPreviousVarName = parameters.tagVar(IN_AUTOMATIC + (indexInput - 1));
					final var posInParams = parameters.getParameters().indexOf(expectedPreviousVarName);
					if (posInParams == parameters.count() - 1) {
						parameters.addParameters(varname);
					} else {
						final var newList = List.of(
						        parameters.getParameters().stream().limit(posInParams + 1L),
						        Stream.of(varname),
						        parameters.getParameters().stream().skip(posInParams + 1L))
						        .stream()
						        .flatMap(s -> s)
						        .collect(toUnmodifiableList());
						parameters.replaceParameters(newList);
					}
				} else {
					parameters.prependParameters(varname);
				}
			} else {
				parameters.addParameters(varname);
			}
		}
	}

	/**
	 * Define cmd var name like &lt;%OUT_AUTOMATIC_n%&gt; with "n" the # of setted destination.
	 * Add now in current Parameters the new add var only if not exists (you should call fixIOParametredVars, if you have add manually vars in Parametres)
	 */
	@Override
	public ConversionTool addSimpleOutputDestination(final String destinationName) {
		final var varname = parameters.tagVar("OUT_AUTOMATIC_" + outputExpectedDestinations.size());
		addVarInParametersIfNotExists(varname);
		return super.addSimpleOutputDestination(destinationName);
	}

	/**
	 * Define cmd var name like &lt;%OUT_AUTOMATIC_n%&gt; with "n" the # of setted destination.
	 * Add now in current Parameters the new add var only if not exists (you should call fixIOParametredVars, if you have add manually vars in Parametres)
	 */
	@Override
	public ConversionTool addSimpleOutputDestination(final File destinationFile) {
		final var varname = parameters.tagVar("OUT_AUTOMATIC_" + outputExpectedDestinations.size());
		addVarInParametersIfNotExists(varname);
		return super.addSimpleOutputDestination(destinationFile);
	}

	public synchronized FFAbout getAbout(final ExecutableFinder executableFinder) {
		if (about == null) {
			final var maxExecTimeScheduler = getMaxExecTimeScheduler();
			if (maxExecTimeScheduler == null) {
				about = new FFAbout(execName, executableFinder, Executors.newSingleThreadScheduledExecutor());
			} else {
				about = new FFAbout(execName, executableFinder, maxExecTimeScheduler);
			}
		}
		return about;
	}

	public synchronized List<Filter> checkFiltersAvailability(final ExecutableFinder executableFinder) {
		getAbout(executableFinder);

		final var badVideoFilters = FilterChains.merge(FilterChains.parse("-vf", this))
		        .checkFiltersAvailability(about, FilterConnectorType.VIDEO);
		final var badAudioFilters = FilterChains.merge(FilterChains.parse("-af", this))
		        .checkFiltersAvailability(about, FilterConnectorType.AUDIO);
		final var badGenericFilterChainsLists = FilterChains.merge(FilterChains.parse("-filter", this))
		        .checkFiltersAvailability(about);
		final var badGenericComplexFilterChainsLists = FilterChains.merge(FilterChains.parse("-filter_complex", this))
		        .checkFiltersAvailability(about);

		return Stream.of(badVideoFilters.stream(),
		        badAudioFilters.stream(),
		        badGenericFilterChainsLists.stream(),
		        badGenericComplexFilterChainsLists.stream())
		        .flatMap(s -> s)
		        .collect(Collectors.toUnmodifiableList());
	}

	private static final Predicate<String> filterOutErrorLines = rawL -> {
		final var l = rawL.trim();
		if (l.startsWith("[")) {
			return true;
		} else if (l.startsWith("ffmpeg version")
		           || l.startsWith("ffprobe version")
		           || l.startsWith("built with")
		           || l.startsWith("configuration:")
		           || l.startsWith("Press [q]")) {
			return false;
		} else if (l.startsWith("libavutil")
		           || l.startsWith("libavcodec")
		           || l.startsWith("libavformat")
		           || l.startsWith("libavdevice")
		           || l.startsWith("libavfilter")
		           || l.startsWith("libswscale")
		           || l.startsWith("libswresample")
		           || l.startsWith("libpostproc")) {
			return false;
		}
		return true;
	};

	@Override
	public Predicate<String> filterOutErrorLines() {
		return filterOutErrorLines;
	}
}
