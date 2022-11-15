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

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public interface InputSourceProviderTraits {

	ConversionTool addInputSource(String source,
								  String varNameInParameters,
								  Collection<String> parametersBeforeInputSource);

	ConversionTool addInputSource(File source,
								  String varNameInParameters,
								  Collection<String> parametersBeforeInputSource);

	/**
	 * Add a parameters via an input reference, like:
	 * [parametersBeforeInputSource] {varNameInParameters replaced by source}
	 * For example, set source = "myfile", varNameInParameters = "IN", parametersBeforeInputSource = [-i],
	 * For an parameters = "exec -VERBOSE &lt;%IN%&gt; -send &lt;%OUT%&gt;", you will get an updated parameters:
	 * "exec -VERBOSE -i myfile -send &lt;%OUT%&gt;"
	 * @param source can be another var name (mindfuck)
	 */
	default ConversionTool addInputSource(final String source,
										  final String varNameInParameters,
										  final String... parametersBeforeInputSource) {
		if (parametersBeforeInputSource != null) {
			return addInputSource(source, varNameInParameters,
					Arrays.stream(parametersBeforeInputSource)
							.filter(Objects::nonNull)
							.toList());
		}
		return addInputSource(source, varNameInParameters, Collections.emptyList());
	}

	/**
	 * Add a parameters via an input reference, like:
	 * [parametersBeforeInputSource] {varNameInParameters replaced by source}
	 * For example, set source = "/myfile", varNameInParameters = "IN", parametersBeforeInputSource = [-i],
	 * For an parameters = "exec -VERBOSE &lt;%IN%&gt; -send &lt;%OUT%&gt;", you will get an updated parameters:
	 * "exec -VERBOSE -i /myfile -send &lt;%OUT%&gt;"
	 */
	default ConversionTool addInputSource(final File source,
										  final String varNameInParameters,
										  final String... parametersBeforeInputSource) {
		if (parametersBeforeInputSource != null) {
			return addInputSource(source, varNameInParameters,
					Arrays.stream(parametersBeforeInputSource)
							.filter(Objects::nonNull)
							.toList());
		}
		return addInputSource(source, varNameInParameters, Collections.emptyList());
	}

}
