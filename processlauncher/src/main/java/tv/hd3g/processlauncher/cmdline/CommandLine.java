/*
 * This file is part of processlauncher.
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
 * Copyright (C) hdsdi3g for hd3g.tv 2019
 *
 */
package tv.hd3g.processlauncher.cmdline;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

public class CommandLine {

	private static final String PARAMETERS_CAN_T_TO_BE_NULL = "\"parameters\" can't to be null";
	private final File executable;
	private final ExecutableFinder executableFinder;
	private final Parameters parameters;

	/**
	 * @param parameters will be clone here
	 */
	public CommandLine(final File executable, final Parameters parameters) throws IOException {
		this.executable = executable;
		if (executable.isFile() == false || executable.exists() == false) {
			throw new FileNotFoundException("Can't found " + executable);
		} else if (executable.canExecute() == false) {
			throw new IOException("Can't execute " + executable);
		}
		executableFinder = null;

		this.parameters = Objects.requireNonNull(parameters, PARAMETERS_CAN_T_TO_BE_NULL).duplicate();
	}

	/**
	 * @param parameters will be clone here
	 */
	public CommandLine(final String execName, final Parameters parameters,
	                   final ExecutableFinder executableFinder) throws IOException {
		Objects.requireNonNull(execName, "\"execName\" can't to be null");
		this.executableFinder = executableFinder;
		if (executableFinder != null) {
			executable = executableFinder.get(execName);
		} else {
			executable = new File(execName);
			if (executable.isFile() == false || executable.exists() == false) {
				throw new FileNotFoundException("Can't found " + executable);
			} else if (executable.canExecute() == false) {
				throw new IOException("Can't execute " + executable);
			}
		}
		this.parameters = Objects.requireNonNull(parameters, PARAMETERS_CAN_T_TO_BE_NULL).duplicate();
	}

	public CommandLine(final File executable, final String bulkParameters) throws IOException {
		this(executable, Parameters.bulk(Objects.requireNonNull(bulkParameters, PARAMETERS_CAN_T_TO_BE_NULL)));
	}

	public CommandLine(final String execName, final String bulkParameters,
	                   final ExecutableFinder executableFinder) throws IOException {
		this(execName, Parameters.bulk(Objects.requireNonNull(bulkParameters, PARAMETERS_CAN_T_TO_BE_NULL)),
		        executableFinder);
	}

	@Override
	public String toString() {
		return executable.getPath() + " " + parameters.toString();
	}

	String getParametersToString() {
		return parameters.toString();
	}

	public Optional<ExecutableFinder> getExecutableFinder() {
		return Optional.ofNullable(executableFinder);
	}

	public File getExecutable() {
		return executable;
	}

	public Parameters getParameters() {
		return parameters;
	}
}
