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
 * Copyright (C) hdsdi3g for hd3g.tv 2018
 *
 */
package tv.hd3g.processlauncher.cmdline;

import static java.util.Arrays.stream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;

/**
 * It will resolve/find valid executable files in *NIX and valid executable extensions in Windows.
 * On system PATH, classpath, current dir, and local user dir (/bin).
 * ThreadSafe
 */
@Slf4j
public class ExecutableFinder {

	/**
	 * Return exists and isDirectory and canRead
	 */
	public static final Predicate<File> isValidDirectory = f -> (f.exists() && f.isDirectory() && f.canRead());

	/**
	 * unmodifiableList
	 */
	public static final List<String> WINDOWS_EXEC_EXTENSIONS;

	/**
	 * unmodifiableList
	 * Specified by -Dexecfinder.searchdir=path1;path2... or path1:path2... on *Nix systems.
	 */
	public static final List<File> GLOBAL_DECLARED_DIRS;

	static {
		if (System.getenv().containsKey("PATHEXT")) {
			/**
			 * Like .COM;.EXE;.BAT;.CMD;.VBS;.VBE;.JS;.JSE;.WSF;.WSH;.MSC
			 */
			final var pathExt = System.getenv("PATHEXT");
			if (pathExt.indexOf(';') >= 0) {
				WINDOWS_EXEC_EXTENSIONS = stream(pathExt.split(";"))// NOSONAR S2386
						.map(ext -> ext.toLowerCase().substring(1))
						.toList();
			} else {
				log.warn("Invalid PATHEXT env.: {}", pathExt);
				WINDOWS_EXEC_EXTENSIONS = List.of("exe", "com", "cmd", "bat");
			}
		} else {
			WINDOWS_EXEC_EXTENSIONS = List.of("exe", "com", "cmd", "bat");
		}

		final var searchdir = System.getProperty("execfinder.searchdir");
		if (searchdir != null && searchdir.equals("") == false) {
			GLOBAL_DECLARED_DIRS = stream(searchdir.split(File.pathSeparator)) // NOSONAR S2386
					.map(File::new)
					.filter(isValidDirectory)
					.map(File::getAbsoluteFile)
					.toList();

			log.debug("Specific executable path declared via system property: {}",
					GLOBAL_DECLARED_DIRS.stream().map(File::getPath).collect(Collectors.joining(", ")));
		} else {
			GLOBAL_DECLARED_DIRS = List.of();
		}
	}

	/**
	 * synchronizedList
	 */
	private final Deque<File> paths;
	private final Map<String, File> declaredInConfiguration;
	private final boolean isWindowsStylePath;

	public ExecutableFinder() {
		declaredInConfiguration = Collections.synchronizedMap(new LinkedHashMap<>());
		isWindowsStylePath = File.separator.equals("\\");

		/**
		 * Adds only valid dirs
		 */
		paths = new ConcurrentLinkedDeque<>(GLOBAL_DECLARED_DIRS);

		addLocalPath("/bin");
		addLocalPath("/App/bin");

		paths.add(new File(System.getProperty("user.dir")));

		paths.addAll(Arrays.stream(System.getProperty("java.class.path").split(File.pathSeparator)).map(File::new)
				.filter(isValidDirectory).toList());

		paths.addAll(Arrays.stream(System.getenv("PATH").split(File.pathSeparator)).map(File::new).filter(
				isValidDirectory).toList());

		/**
		 * Remove duplicate entries
		 */
		final var newList = paths.stream().distinct().toList();
		paths.clear();
		paths.addAll(newList);

		log.trace("Full path: {}",
				paths.stream().map(File::getPath).collect(Collectors.joining(File.pathSeparator)));
	}

	public String getFullPathToString() {
		return paths.stream()
		        .map(File::getPath)
		        .reduce((left, right) -> (left + File.pathSeparator + right))
		        .orElse("");
	}

	/**
	 * @return unmodifiableList
	 */
	public List<File> getFullPath() {
		return paths.stream().toList();
	}

	/**
	 * Put in top priority.
	 * Path / or \ will be corrected
	 */
	public ExecutableFinder addLocalPath(final String relativeUserHomePath) {
		String converted;
		if (isWindowsStylePath) {
			converted = relativeUserHomePath.replace("/", "\\\\");
		} else {
			converted = relativeUserHomePath.replace("\\\\", "/");
		}

		final var userHome = System.getProperty("user.home");
		final var f = new File(userHome + File.separator + converted).getAbsoluteFile();

		return addPath(f);
	}

	/**
	 * Put in top priority.
	 */
	public ExecutableFinder addPath(final File filePath) {
		final var f = filePath.getAbsoluteFile();

		if (isValidDirectory.test(f)) {
			synchronized (this) {
				log.debug("Register path: {}", f.getPath());
				paths.addFirst(f);
			}
		}
		return this;
	}

	private boolean validExec(final File exec) {
		if (exec.exists() == false || exec.isFile() == false || exec.canRead() == false) {
			return false;
		} else {
			return exec.canExecute();
		}
	}

	public ExecutableFinder registerExecutable(final String name, final File fullPath) throws IOException {
		if (validExec(fullPath) == false) {
			throw new IOException("Invalid declaredInConfiguration executable: " + name
			                      + " can't be correctly found in " + fullPath);
		}
		declaredInConfiguration.put(name, fullPath);
		return this;
	}

	/**
	 * Can add .exe to name if OS == Windows and if it's missing.
	 * @param name can be a simple exec name, or a full path.
	 * @return never null
	 * @throws FileNotFoundException if exec don't exists or is not correctly registed.
	 */
	public File get(final String name) throws FileNotFoundException {
		if (declaredInConfiguration.containsKey(name)) {
			return declaredInConfiguration.get(name);
		}

		final var exec = new File(name);
		if (validExec(exec)) {
			return exec;
		}

		final var allFileCandidates = Stream.concat(declaredInConfiguration.values().stream().map(
				File::getParentFile), paths.stream()).map(dir -> new File(dir + File.separator + name)
						.getAbsoluteFile())
				.distinct().toList();

		if (isWindowsStylePath == false) {
			/**
			 * *nix flavors
			 */
			return allFileCandidates.stream().filter(this::validExec).findFirst().orElseThrow(
					() -> new FileNotFoundException("Can't found executable \"" + name + "\""));
		} else {
			/**
			 * Windows flavor
			 * Try with add windows ext
			 */
			return allFileCandidates.stream().flatMap(file -> {
				final var hasAlreadyValidExt = WINDOWS_EXEC_EXTENSIONS.stream().anyMatch(ext -> file.getName()
				        .toLowerCase().endsWith("." + ext.toLowerCase()));

				if (hasAlreadyValidExt) {
					if (validExec(file)) {
						return Stream.of(file);
					} else {
						return Stream.empty();
					}
				} else {
					/**
					 * We must to add ext, we try with all avaliable ext.
					 */
					return WINDOWS_EXEC_EXTENSIONS.stream().flatMap(ext -> Stream
					        .of(new File(file + "." + ext.toLowerCase()),
					                new File(file + "." + ext.toUpperCase())))
					        .filter(this::validExec);
				}
			}).findFirst().orElseThrow(() -> new FileNotFoundException("Can't found executable \"" + name + "\""));
		}
	}

}
