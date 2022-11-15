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
 * Copyright (C) hdsdi3g for hd3g.tv 2022
 *
 */
package tv.hd3g.fflauncher;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public interface SimpleSourceTraits extends InputSourceProviderTraits, InternalParametersSupplier {

	/**
	 * Define cmd var name like &lt;%IN_AUTOMATIC_n%&gt; with "n" the # of setted sources.
	 * Add -i parameter
	 * Add now in current Parameters the new add var only if not exists (you should call fixIOParametredVars, if you have add manually vars in Parametres)
	 */
	default void addSimpleInputSource(final String sourceName, final String... sourceOptions) {
		requireNonNull(sourceName, "\"sourceName\" can't to be null");

		if (sourceOptions == null) {
			addSimpleInputSource(sourceName, Collections.emptyList());
		} else {
			addSimpleInputSource(sourceName, Arrays.stream(sourceOptions).toList());
		}
	}

	/**
	 * Define cmd var name like &lt;%IN_AUTOMATIC_n%&gt; with "n" the # of setted sources.
	 * Add -i parameter
	 * Add now in current Parameters the new add var only if not exists (you should call fixIOParametredVars, if you have add manually vars in Parametres)
	 */
	default void addSimpleInputSource(final File file, final String... sourceOptions) {
		requireNonNull(file, "\"file\" can't to be null");

		if (sourceOptions == null) {
			addSimpleInputSource(file, Collections.emptyList());
		} else {
			addSimpleInputSource(file, Arrays.stream(sourceOptions).toList());
		}
	}

	void addSimpleInputSource(final String sourceName, final List<String> sourceOptions);

	void addSimpleInputSource(final File file, final List<String> sourceOptions);
}
