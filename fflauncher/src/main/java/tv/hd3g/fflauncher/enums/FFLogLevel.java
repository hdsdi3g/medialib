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
package tv.hd3g.fflauncher.enums;

public enum FFLogLevel {
	/**
	 * Show nothing at all; be silent.
	 */
	QUIET,

	/**
	 * Only show FATAL errors which could lead the process to crash, such as an assertion failure. This is not currently used for anything.
	 */
	PANIC,

	/**
	 * Only show FATAL errors. These are errors after which the process absolutely cannot continue.
	 */
	FATAL,

	/**
	 * Show all errors, including ones which can be recovered from.
	 */
	ERROR,

	/**
	 * Show all warnings and errors. Any message related to possibly incorrect or unexpected events will be shown.
	 */
	WARNING,

	/**
	 * Show informative messages during processing. This is in addition to warnings and errors. This is the default value.
	 */
	INFO,
	/**
	 * Same as INFO, except more VERBOSE.
	 */
	VERBOSE,

	/**
	 * Show everything, including debugging information.
	 */
	DEBUG,

	TRACE;

	@Override
	public String toString() {
		return name().toLowerCase();
	}
}
