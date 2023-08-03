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

import static java.util.function.Predicate.not;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SimpleParameters {
	private static final String LOG_ADD_PARAMETERS = "Add parameters: {}";

	private static final String PARAMS_CAN_T_TO_BE_NULL = "\"params\" can't to be null";

	private static final Character QUOTE = '"';
	private static final Character SPACE = ' ';

	private final List<String> parameters;
	private String parameterKeysStartsWith = "-";

	SimpleParameters() {
		parameters = new ArrayList<>();
	}

	SimpleParameters(final String bulkParameters) {
		this();
		addBulkParameters(bulkParameters);
	}

	SimpleParameters(final Collection<String> parameters) {
		this();
		addParameters(parameters);
	}

	/**
	 * Don't touch to current parameters, only parameterKeysStartsWith
	 */
	public SimpleParameters transfertThisConfigurationTo(final SimpleParameters newInstance) {
		newInstance.parameterKeysStartsWith = parameterKeysStartsWith;
		return this;
	}

	/**
	 * Transfer (clone) current parameters and parameterKeysStartsWith
	 */
	public SimpleParameters importParametersFrom(final SimpleParameters previousInstance) {
		log.trace("Import from {}", previousInstance);

		parameterKeysStartsWith = previousInstance.parameterKeysStartsWith;
		parameters.clear();
		parameters.addAll(previousInstance.parameters);
		return this;
	}

	/**
	 * Don't touch to actual parameters, and clone from source.
	 */
	public void addAllFrom(final SimpleParameters source) {
		parameters.addAll(source.parameters);
	}

	private static void filterMapFirstEntry(final ArrayList<ParameterArg> list, final Character chr) {
		if (chr.equals(QUOTE)) {
			/**
			 * Start quote zone
			 */
			list.add(new ParameterArg(true));
		} else if (chr.equals(SPACE)) {
			/**
			 * Trailing space > ignore it
			 */
		} else {
			/**
			 * Start first "classic" ParameterArg
			 */
			list.add(new ParameterArg(false).add(chr));
		}
	}

	private final Function<String, Stream<ParameterArg>> filterAndTransformParameter = p -> p.trim()
			.chars()
			.mapToObj(i -> (char) i)// NOSONAR S1612
			.reduce(new ArrayList<ParameterArg>(), (list, chr) -> {
				if (list.isEmpty()) {
					/**
					 * First entry
					 */
					filterMapFirstEntry(list, chr);
				} else {
					/**
					 * Get current entry
					 */
					final var lastPos = list.size() - 1;
					final var lastEntry = list.get(lastPos);

					if (chr.equals(QUOTE)) {
						if (lastEntry.isInQuotes()) {
							/**
							 * Switch off quote zone
							 */
							list.add(new ParameterArg(false));
						} else {
							/**
							 * Switch on quote zone
							 */
							if (lastEntry.isEmpty()) {
								/**
								 * Remove previous empty ParameterArg
								 */
								list.remove(lastPos);
							}
							list.add(new ParameterArg(true));
						}
					} else if (!chr.equals(SPACE) || lastEntry.isInQuotes()) {
						/**
						 * Add space in quotes
						 */
						lastEntry.add(chr);
					} else {
						if (lastEntry.isEmpty() == false) {
							/**
							 * New space -&gt; new ParameterArg (and ignore space)
							 */
							list.add(new ParameterArg(false));
						} else {
							/**
							 * Space between ParameterArgs -&gt; ignore it
							 */
						}
					}
				}
				return list;
			}, (list1, list2) -> {
				final var parameterArgs = new ArrayList<>(list1);
				parameterArgs.addAll(list2);
				return parameterArgs;
			}).stream();

	public SimpleParameters clear() {
		log.trace("Clear all");
		parameters.clear();
		return this;
	}

	/**
	 * @param params (anyMatch) ; params can have "-" or not (it will be added).
	 */
	public boolean hasParameters(final String... params) {
		Objects.requireNonNull(params, PARAMS_CAN_T_TO_BE_NULL);

		return Arrays.stream(params)
				.filter(Objects::nonNull)
				.anyMatch(parameter -> parameters.contains(conformParameterKey(parameter)));
	}

	/**
	 * See SimpleParameters#hasParameters()
	 */
	public SimpleParameters ifHasNotParameter(final Runnable toDoIfMissing, final String... inParameters) {
		Objects.requireNonNull(toDoIfMissing, "\"toDoIfMissing\" can't to be null");
		if (hasParameters(inParameters) == false) {
			toDoIfMissing.run();
		}
		return this;
	}

	/**
	 * @return internal list, never null
	 */
	public List<String> getParameters() {
		return parameters;
	}

	public SimpleParameters replaceParameters(final Collection<? extends String> newParameters) {
		parameters.clear();
		parameters.addAll(newParameters);
		return this;
	}

	/**
	 * @param params don't alter params
	 *        Remove null and empty
	 */
	public SimpleParameters addParameters(final String... params) {
		Objects.requireNonNull(params, PARAMS_CAN_T_TO_BE_NULL);
		addParameters(Arrays.stream(params)
				.filter(Objects::nonNull)
				.filter(not(String::isEmpty))
				.toList());
		return this;
	}

	/**
	 * @param params don't alter params
	 *        Remove null and empty
	 */
	public SimpleParameters addParameters(final Collection<String> params) {
		Objects.requireNonNull(params, PARAMS_CAN_T_TO_BE_NULL);

		final var subList = params.stream()
				.filter(Objects::nonNull)
				.filter(not(String::isEmpty))
				.toList();
		parameters.addAll(subList);

		log.trace(LOG_ADD_PARAMETERS, subList);
		return this;
	}

	/**
	 * @param params transform spaces in each param to new params: "a b c d" -&gt; ["a", "b", "c", "d"], and it manage " but not tabs.
	 *        Remove empty
	 */
	public SimpleParameters addBulkParameters(final String params) {
		Objects.requireNonNull(params, PARAMS_CAN_T_TO_BE_NULL);

		final var subList = filterAndTransformParameter.apply(params)
				.map(ParameterArg::toString)
				.filter(not(String::isEmpty))
				.toList();

		parameters.addAll(subList);

		log.trace(LOG_ADD_PARAMETERS, subList);
		return this;
	}

	/**
	 * @param params don't alter params
	 */
	public SimpleParameters prependParameters(final Collection<String> params) {
		Objects.requireNonNull(params, PARAMS_CAN_T_TO_BE_NULL);

		final var newList = Stream.concat(
				params.stream().filter(Objects::nonNull),
				parameters.stream())
				.toList();
		replaceParameters(newList);

		log.trace("Prepend parameters: {}", params);
		return this;
	}

	/**
	 * @param params add all in front of command line, don't alter params
	 */
	public SimpleParameters prependParameters(final String... params) {
		Objects.requireNonNull(params, PARAMS_CAN_T_TO_BE_NULL);

		prependParameters(Arrays.stream(params)
				.filter(Objects::nonNull)
				.toList());
		return this;
	}

	/**
	 * @param params params add all in front of command line, transform spaces in each param to new params: "a b c d" -&gt; ["a", "b", "c", "d"], and it manage " but not tabs.
	 */
	public SimpleParameters prependBulkParameters(final String params) {
		Objects.requireNonNull(params, PARAMS_CAN_T_TO_BE_NULL);

		prependParameters(
				filterAndTransformParameter.apply(params)
						.map(ParameterArg::toString)
						.toList());
		return this;
	}

	@Override
	public String toString() {
		return parameters.stream().collect(Collectors.joining(" "));
	}

	public static final List<Character> MUST_ESCAPE = List.of('\\', '$', '"');
	public static final List<Character> MUST_SURROUND_QUOTE = List.of(' ', '+', ';', '&', '\'', '#', '|',
			'(', ')', '[', ']', '{', '}', '*', '?', '/', '.', '<', '>');

	/**
	 * Mostly in Linux/Bash mode.
	 * @return with automatic escape
	 */
	public String exportToExternalCommandLine(final String processExecFile) {
		return processExecFile + " " + parameters.stream()
				.map(arg -> {
					var escapedChr = arg;
					for (final var chr : MUST_ESCAPE) {
						escapedChr = escapedChr.replace("" + chr, "\\" + chr);
					}
					return escapedChr;
				})
				.map(arg -> {
					if (MUST_SURROUND_QUOTE.stream()
							.anyMatch(chr -> arg.indexOf(chr) > -1)) {
						return "\"" + arg + "\"";
					}
					return arg;
				})
				.collect(Collectors.joining(" "));
	}

	/**
	 * @param parameterKeysStartsWith "-" by default
	 */
	public SimpleParameters setParametersKeysStartsWith(final String parameterKeysStartsWith) {
		this.parameterKeysStartsWith = parameterKeysStartsWith;
		log.debug("Set parameters key start with: {}", parameterKeysStartsWith);
		return this;
	}

	/**
	 * @return "-" by default
	 */
	public String getParametersKeysStartsWith() {
		return parameterKeysStartsWith;
	}

	boolean isParameterArgIsAParametersKey(final String arg) {
		return arg.startsWith(parameterKeysStartsWith);
	}

	/**
	 * @param parameterKey add "-" in front of paramKey if needed
	 */
	protected String conformParameterKey(final String parameterKey) {
		if (isParameterArgIsAParametersKey(parameterKey) == false) {
			return parameterKeysStartsWith + parameterKey;
		}
		return parameterKey;
	}

	/**
	 * @param parameterKey can have "-" or not (it will be added).
	 * @return For "-param val1 -param val2 -param val3" -&gt; val1, val2, val3 ; null if parameterKey can't be found, empty if not values for param
	 */
	public List<String> getValues(final String parameterKey) {
		Objects.requireNonNull(parameterKey, "\"parameterKey\" can't to be null");

		final var param = conformParameterKey(parameterKey);

		final var result = new ArrayList<String>();

		var has = false;
		for (var pos = 0; pos < parameters.size(); pos++) {
			final var current = parameters.get(pos);
			if (current.equals(param)) {
				has = true;
				if (parameters.size() > pos + 1) {
					final var next = parameters.get(pos + 1);
					if (isParameterArgIsAParametersKey(next) == false) {
						result.add(next);
					}
				}
			}
		}

		if (has) {
			return Collections.unmodifiableList(result);
		} else {
			return null;// NOSONAR
		}
	}

	/**
	 * Search a remove all parameters with paramKey as name, even associated values.
	 * @param parametersKey can have "-" or not (it will be added).
	 */
	public boolean removeParameter(final String parametersKey, final int paramAsThisKeyPos) {
		Objects.requireNonNull(parametersKey, "\"parametersKey\" can't to be null");

		final var param = conformParameterKey(parametersKey);

		var toSkip = paramAsThisKeyPos + 1;

		for (var pos = 0; pos < parameters.size(); pos++) {
			final var current = parameters.get(pos);
			if (current.equals(param)) {
				toSkip--;
				if (toSkip == 0) {
					if (parameters.size() > pos + 1) {
						final var next = parameters.get(pos + 1);
						if (isParameterArgIsAParametersKey(next) == false) {
							parameters.remove(pos + 1);
						}
					}
					log.trace("Remove parameter: {}", parameters.remove(pos));// NOSONAR
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * @param parameterKey can have "-" or not (it will be added).
	 * @return true if done
	 */
	public boolean alterParameter(final String parameterKey, final String newValue, final int paramAsThisKeyPos) {
		Objects.requireNonNull(parameterKey, "\"parameterKey\" can't to be null");
		Objects.requireNonNull(newValue, "\"newValue\" can't to be null");

		final var param = conformParameterKey(parameterKey);

		var toSkip = paramAsThisKeyPos + 1;

		for (var pos = 0; pos < parameters.size(); pos++) {
			final var current = parameters.get(pos);
			if (current.equals(param)) {
				toSkip--;
				if (toSkip == 0) {
					if (parameters.size() > pos + 1) {
						final var next = parameters.get(pos + 1);
						if (isParameterArgIsAParametersKey(next) == false) {
							parameters.set(pos + 1, newValue);
						} else {
							parameters.add(pos + 1, newValue);
						}
					} else {
						parameters.add(newValue);
					}
					log.trace("Add parameter: {}", newValue);
					return true;
				}
			}
		}

		return false;
	}

	public int count() {
		return parameters.size();
	}

	public boolean isEmpty() {
		return parameters.isEmpty();
	}

	public Map<String, List<String>> getAllArgKeyValues() {
		final var result = new HashMap<String, List<String>>();
		for (var pos = 0; pos < parameters.size(); pos++) {
			final var actualArg = parameters.get(pos);
			if (isParameterArgIsAParametersKey(actualArg) == false) {
				continue;
			}
			final var values = result.computeIfAbsent(actualArg, a -> new ArrayList<>());
			if (parameters.size() > pos + 1) {
				final var nextArg = parameters.get(pos + 1);
				if (isParameterArgIsAParametersKey(nextArg) == false) {
					values.add(nextArg);
					pos++;// NOSONAR S127
				}
			}
		}

		/**
		 * Strongify to unmodifiable Lists / Map
		 */
		final var result2 = new HashMap<String, List<String>>(result.size());
		result.forEach((k, v) -> result2.put(k, Collections.unmodifiableList(v)));
		return Collections.unmodifiableMap(result2);
	}

	/**
	 * @param argValueChoice policy to apply if some args keys a in common with actual and toCompare
	 * @param removeActualMissing remove all current args not founded in toCompare args list
	 * @param addComparedMissing add all args from toCompare args list for some not found in actual args list
	 * @see getAllArgKeyValues()
	 */
	public void compareAndAlter(final SimpleParameters toCompare,
								final ArgValueChoice argValueChoice,
								final boolean removeActualMissing,
								final boolean addComparedMissing) {
		final var allCurrentArgsKeyValues = getAllArgKeyValues();
		final var allComparedArgsKeyValues = toCompare.getAllArgKeyValues();

		final var newParameters = new ArrayList<String>();
		final var computedKeys = new HashSet<String>();

		for (var pos = 0; pos < parameters.size(); pos++) {
			final var actualArg = parameters.get(pos);
			if (isParameterArgIsAParametersKey(actualArg) == false) {
				newParameters.add(actualArg);
				continue;
			}

			List<String> selectedValues = null;
			if (computedKeys.contains(actualArg) == false) {
				if (allComparedArgsKeyValues.containsKey(actualArg)) {
					selectedValues = argValueChoice.choose(actualArg,
							allCurrentArgsKeyValues.get(actualArg),
							allComparedArgsKeyValues.get(actualArg));
				} else if (removeActualMissing == false) {
					selectedValues = allCurrentArgsKeyValues.get(actualArg);
				}
				computedKeys.add(actualArg);
			}

			if (selectedValues != null) {
				if (selectedValues.isEmpty()) {
					newParameters.add(actualArg);
				} else {
					selectedValues.forEach(v -> {
						newParameters.add(actualArg);
						newParameters.add(v);
					});
				}
			}

			if (parameters.size() > pos + 1 && isParameterArgIsAParametersKey(parameters.get(pos + 1)) == false) {
				/**
				 * Next arg is a regular value: this was previsouly added, skip to the next.
				 */
				pos++;// NOSONAR S127
			}
		}

		if (addComparedMissing) {
			allComparedArgsKeyValues.entrySet().stream()
					.filter(entry -> computedKeys.contains(entry.getKey()) == false)
					.forEach(entry -> {
						if (entry.getValue().isEmpty()) {
							newParameters.add(entry.getKey());
						} else {
							entry.getValue().forEach(v -> {
								newParameters.add(entry.getKey());
								newParameters.add(v);
							});
						}
					});
		}

		replaceParameters(newParameters);
	}

	@Override
	public int hashCode() {
		return Objects.hash(parameterKeysStartsWith, parameters);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final var other = (SimpleParameters) obj;
		return Objects.equals(parameterKeysStartsWith, other.parameterKeysStartsWith)
			   && Objects.equals(parameters, other.parameters);
	}

}
