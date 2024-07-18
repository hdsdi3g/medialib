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
package tv.hd3g.fflauncher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import tv.hd3g.processlauncher.cmdline.Parameters;

@Slf4j
public class ConversionToolParameterReference {

	private static final int TRY_COUNT_TO_OPEN_FILE = 5;

	private static final Set<OpenOption> openOptionsReadOnly = Set.of(StandardOpenOption.READ);

	private final String ressource;
	private final boolean ressourceAsFile;
	private final String varNameInParameters;
	private final Parameters parametersBeforeRef;

	/**
	 * @param varNameInParameters should be with tags
	 * @param parametersBeforeRef can be null
	 */
	ConversionToolParameterReference(final String reference, final String varNameInParameters,
									 final Collection<String> parametersBeforeRef) {
		ressource = Objects.requireNonNull(reference, "\"reference\" can't to be null");
		this.varNameInParameters = Objects.requireNonNull(varNameInParameters,
				"\"var_name_in_parameters\" can't to be null");
		this.parametersBeforeRef = Optional.ofNullable(parametersBeforeRef)
				.map(Parameters::of)
				.orElseGet(Parameters::of);
		ressourceAsFile = false;
	}

	/**
	 * @param parametersBeforeRef can be null
	 */
	ConversionToolParameterReference(final File reference, final String varNameInParameters,
									 final Collection<String> parametersBeforeRef) {
		ressource = Objects.requireNonNull(reference, "\"reference\" can't to be null").getPath();
		this.varNameInParameters = Objects.requireNonNull(varNameInParameters,
				"\"var_name_in_parameters\" can't to be null");
		this.parametersBeforeRef = Optional.ofNullable(parametersBeforeRef)
				.map(Parameters::of)
				.orElseGet(Parameters::of);
		ressourceAsFile = true;
	}

	String getRessource() {
		return ressource;
	}

	Parameters getParametersBeforeRef() {
		return parametersBeforeRef;
	}

	/**
	 * Replace on this Param all the founded char in search.
	 * All this Param count args = N
	 * @param actual search N args on the this current var pos, from the right to the left.
	 */
	void manageCollisionsParameters(final Parameters actualParameters) {
		final var parametersList = parametersBeforeRef.getParameters();
		if (parametersList.isEmpty()) {
			return;
		}
		final var actual = actualParameters.getParameters();
		final var allNParamBeforeThisVarCount = (int) actual.stream()
				.takeWhile(arg -> arg.equals(varNameInParameters) == false)
				.count();

		/**
		 * Trim by ends
		 * [a, b, c, d] / [e, c, d]
		 * <<<----^--^ / <<<--^--^ => [a, b, c, d] / [e]
		 */
		log.trace("Compare collisions, actual: \"{}\", this: \"{}\"", actual, parametersList);
		final var toRemove = new ArrayList<Integer>();
		for (var pos = 0; pos < Math.min(parametersList.size(), allNParamBeforeThisVarCount); pos++) {
			final var argActual = actual.get(allNParamBeforeThisVarCount - (pos + 1));
			final var argThis = parametersList.get(parametersList.size() - (pos + 1));
			if (argThis.equals(argActual)) {
				toRemove.add(pos);
			} else {
				break;
			}
		}

		Collections.reverse(toRemove);
		toRemove.forEach(pos -> parametersList.remove((int) pos));

		if (parametersList.isEmpty()) {
			return;
		}

		/**
		 * Sliding mask
		 * [a, b, c, d] / [c, d, e]
		 * ......=> [c] => Nope
		 * ...=> [c, d] => Yes, trim => [a, b, c, d] / [e]
		 */
		for (var windowsWidth = 0; windowsWidth < Math.min(parametersList.size(),
				allNParamBeforeThisVarCount) - 1; windowsWidth++) {
			final var actualMinBound = allNParamBeforeThisVarCount - (windowsWidth + 1);
			final var actualMaxBound = allNParamBeforeThisVarCount;
			final var beforePMinBound = 0;
			final var beforePMaxBound = windowsWidth + 1;

			final var actualSubList = actual.subList(actualMinBound, actualMaxBound);
			final var parametersListSubList = parametersList.subList(beforePMinBound, beforePMaxBound);
			if (actualSubList.equals(parametersListSubList)) {
				parametersListSubList.clear();
				break;
			}
		}
	}

	List<String> getParametersListBeforeRef() {
		return getParametersBeforeRef().getParameters();
	}

	/**
	 * @return with tags
	 */
	String getVarNameInParameters() {
		return varNameInParameters;
	}

	/**
	 * @param varName with tags
	 */
	boolean isVarNameInParametersEquals(final String varName) {
		return varNameInParameters.equals(varName);
	}

	void checkOpenRessourceAsFile() throws IOException, InterruptedException {
		if (ressourceAsFile == false) {
			return;
		}
		final var file = new File(ressource);
		if (file.isDirectory()) {
			return;
		}

		for (var pos = 0; pos < TRY_COUNT_TO_OPEN_FILE; ++pos) {
			if (file.canRead()) {
				try (var sbc = Files.newByteChannel(file.toPath(), openOptionsReadOnly)) {
					log.debug("Successfully open file \"{}\" for check access", file);
					return;
				} catch (final IOException e) {
					if (pos + 1 == TRY_COUNT_TO_OPEN_FILE) {
						throw e;
					}
					Thread.sleep(10l + 100 * pos);
				}
			} else {
				if (pos + 1 == TRY_COUNT_TO_OPEN_FILE) {
					throw new IOException("Can't read file \"" + file + "\" for check access");
				}
				Thread.sleep(10l + 100 * pos);
			}
		}
	}

	/**
	 * @return getRessource()
	 */
	@Override
	public String toString() {
		return getRessource();
	}
}
