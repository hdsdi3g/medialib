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

import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

public class Parameters extends SimpleParameters {

	private static final BinaryOperator<List<String>> LIST_COMBINER = (list1, list2) -> Stream.concat(list1.stream(),
			list2.stream()).toList();

	private String startVarTag;
	private String endVarTag;

	/**
	 * Use "&lt;%" and "%&gt;" by default
	 */
	public Parameters() {
		super();
		setVarTags("<%", "%>");
	}

	/**
	 * @param parameters add each entry without alter it.
	 *        Use "&lt;%" and "%&gt;" by default
	 */
	public static Parameters of(final String... parameters) {
		final var p = new Parameters();
		p.addParameters(parameters);
		return p;
	}

	/**
	 * @param parameters add each entry without alter it.
	 *        Use "&lt;%" and "%&gt;" by default
	 */
	public static Parameters of(final Collection<String> parameters) {
		final var p = new Parameters();
		p.addParameters(parameters);
		return p;
	}

	/**
	 * @param parameters add each entry with addBulkParameters
	 *        Use "&lt;%" and "%&gt;" by default
	 */
	public static Parameters bulk(final String... bulkParameters) {
		final var p = new Parameters();
		Arrays.stream(bulkParameters).filter(Objects::nonNull).forEach(p::addBulkParameters);
		return p;
	}

	/**
	 * @param parameters add each entry with addBulkParameters
	 *        Use "&lt;%" and "%&gt;" by default
	 */
	public static Parameters bulk(final Collection<String> bulkParameters) {
		final var p = new Parameters();
		bulkParameters.forEach(p::addBulkParameters);
		return p;
	}

	public String tagVar(final String varName) {
		return startVarTag + varName + endVarTag;
	}

	public Parameters setVarTags(final String startVarTag, final String endVarTag) {
		this.startVarTag = Objects.requireNonNull(startVarTag, "\"startVarTag\" can't to be null");
		if (startVarTag.isEmpty()) {
			throw new IllegalArgumentException("\"startVarTag\" can't to be empty");
		}
		this.endVarTag = Objects.requireNonNull(endVarTag, "\"endVarTag\" can't to be null");
		if (endVarTag.isEmpty()) {
			throw new IllegalArgumentException("\"endVarTag\" can't to be empty");
		}
		return this;
	}

	/**
	 * Don't touch to current parameters, only parameterKeysStartsWith, startVarTag, endVarTag.
	 */
	public Parameters transfertThisConfigurationTo(final Parameters newInstance) {
		this.transfertThisConfigurationTo((SimpleParameters) newInstance);
		newInstance.setVarTags(startVarTag, endVarTag);
		return this;
	}

	/**
	 * @return like "%&gt;"
	 */
	public String getEndVarTag() {
		return endVarTag;
	}

	/**
	 * @return like "&lt;%"
	 */
	public String getStartVarTag() {
		return startVarTag;
	}

	/**
	 * @param param like
	 * @return true if like "&lt;%myvar%&gt;"
	 */
	public boolean isTaggedParameter(final String param) {
		Objects.requireNonNull(param, "\"param\" can't to be null");
		if (param.isEmpty() || param.contains(" ")) {
			return false;
		}
		return param.startsWith(startVarTag) && param.endsWith(endVarTag);
	}

	/**
	 * @param param like &lt;%myvar%&gt;
	 * @return like "myvar" or null if param is not a valid variable of if it's empty.
	 */
	public String extractVarNameFromTaggedParameter(final String param) {
		if (isTaggedParameter(param) == false) {
			return null;
		}
		if (param.length() == startVarTag.length() + endVarTag.length()) {
			return null;
		}
		return param.substring(startVarTag.length(), param.length() - endVarTag.length());
	}

	/**
	 * @return varName
	 */
	public String addVariable(final String varName) {
		addParameters(startVarTag + varName + endVarTag);
		return varName;
	}

	public Parameters duplicate() {
		final var newInstance = new Parameters();
		newInstance.setVarTags(startVarTag, endVarTag);
		newInstance.importParametersFrom(this);
		return newInstance;
	}

	/**
	 * @return true if the update is done
	 */
	public boolean injectParamsAroundVariable(final String varName,
											  final Collection<String> addBefore,
											  final Collection<String> addAfter) {
		Objects.requireNonNull(varName, "\"varName\" can't to be null");
		Objects.requireNonNull(addBefore, "\"addBefore\" can't to be null");
		Objects.requireNonNull(addAfter, "\"addAfter\" can't to be null");

		final var isDone = new AtomicBoolean(false);

		final var newParameters = getParameters().stream()
				.reduce(unmodifiableList(new ArrayList<String>()),
						(list, arg) -> {
							if (isTaggedParameter(arg)) {
								final var currentVarName = extractVarNameFromTaggedParameter(arg);
								if (currentVarName != null && currentVarName.equals(varName)) {
									isDone.set(true);
									return Stream.concat(list.stream(), Stream.concat(Stream.concat(addBefore.stream(),
											Stream
													.of(arg)), addAfter.stream())).toList();
								}
							}

							return Stream.concat(list.stream(), Stream.of(arg)).toList();
						},
						LIST_COMBINER);

		replaceParameters(newParameters);
		return isDone.get();
	}

	/**
	 * @param removeParamsIfNoVarToInject if true, for "-a -b ? -d" -&gt; "-a -d", else "-a -b -d"
	 * @return this
	 */
	public Parameters removeVariables(final boolean removeParamsIfNoVarToInject) {
		return injectVariables(Collections.emptyMap(), removeParamsIfNoVarToInject);
	}

	/**
	 * @param removeParamsIfNoVarToInject if true, for "-a -b ? -d" -&gt; "-a -d", else "-a -b -d"
	 * @return this
	 */
	public Parameters injectVariables(final Map<String, Parameters> varsToInject,
									  final boolean removeParamsIfNoVarToInject) {
		final List<String> newParameters;
		if (removeParamsIfNoVarToInject) {
			newParameters = getParameters().stream()
					.reduce(unmodifiableList(new ArrayList<String>()),
							(list, varName) -> {
								if (isTaggedParameter(varName)) {
									if (varsToInject.containsKey(varName)) {
										return Stream.concat(
												list.stream(),
												varsToInject.get(varName).getParameters().stream())
												.toList();
									} else {
										if (list.isEmpty()
											|| !isParameterArgIsAParametersKey(list.get(list.size() - 1))) {
											return list;
										} else {
											return list.stream().limit(list.size() - 1L).toList();
										}
									}
								} else {
									return Stream.concat(list.stream(), Stream.of(varName)).toList();
								}
							},
							LIST_COMBINER);
		} else {
			newParameters = computeInjectVariablesKeepParams(varsToInject);
		}

		replaceParameters(newParameters);
		return this;
	}

	private List<String> computeInjectVariablesKeepParams(final Map<String, Parameters> varsToInject) {
		final List<String> newParameters;
		newParameters = getParameters().stream()
				.flatMap(arg -> {
					if (isTaggedParameter(arg)) {
						if (varsToInject.containsKey(arg)) {
							return varsToInject.get(arg).getParameters().stream();
						} else {
							return Stream.empty();
						}
					} else {
						return Stream.of(arg);
					}
				})
				.toList();
		return newParameters;
	}

	@Override
	public int hashCode() {
		final var prime = 31;
		var result = super.hashCode();
		result = prime * result + Objects.hash(endVarTag, startVarTag);
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final var other = (Parameters) obj;
		return Objects.equals(endVarTag, other.endVarTag) && Objects.equals(startVarTag, other.startVarTag);
	}

}
