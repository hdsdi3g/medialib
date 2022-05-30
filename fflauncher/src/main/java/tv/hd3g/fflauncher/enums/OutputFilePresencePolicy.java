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
 * Copyright (C) hdsdi3g for hd3g.tv 2019
 *
 */
package tv.hd3g.fflauncher.enums;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;

public enum OutputFilePresencePolicy {
	ALL {
		@Override
		public Predicate<File> filter() {
			return f -> true;
		}
	},
	MUST_EXISTS {
		@Override
		public Predicate<File> filter() {
			return TEST_EXISTS;
		}
	},
	/**
	 * Implicit MUST_EXISTS
	 */
	MUST_BE_A_REGULAR_FILE {
		@Override
		public Predicate<File> filter() {
			return TEST_EXISTS.and(TEST_REGULAR_FILE);
		}
	},
	/**
	 * Implicit MUST_EXISTS. Check file size if file, or dir content count.
	 */
	NOT_EMPTY {
		@Override
		public Predicate<File> filter() {
			return TEST_EXISTS.and(TEST_REGULAR_DIR.and(TEST_NOT_EMPTY_DIR)).or(TEST_REGULAR_FILE.and(
			        TEST_NOT_EMPTY_FILE));
		}
	};

	private static final Predicate<File> TEST_EXISTS = File::exists;
	private static final Predicate<File> TEST_REGULAR_FILE = File::isFile;
	private static final Predicate<File> TEST_REGULAR_DIR = File::isDirectory;
	private static final Predicate<File> TEST_NOT_EMPTY_FILE = f -> f.length() > 0;
	private static final Predicate<File> TEST_NOT_HIDDEN = f -> f.isHidden() == false;
	private static final Predicate<File> TEST_NOT_DOTFILE = f -> f.getName().startsWith(".") == false;
	private static final Predicate<File> TEST_NOT_EMPTY_DIR = d -> {
		try {
			return Files.list(d.toPath()).map(Path::toFile).anyMatch(TEST_NOT_HIDDEN.and(TEST_NOT_DOTFILE));
		} catch (final IOException e) {
			return false;
		}
	};

	public abstract Predicate<File> filter();

}
