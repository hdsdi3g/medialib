package tv.hd3g.fflauncher.filtering.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

class FilterParserGraphBranch {

	private final List<FilterParserChars> branchFilterChain;

	FilterParserGraphBranch(final List<FilterParserChars> branchFilterChain) {
		this.branchFilterChain = branchFilterChain;
	}

	List<FilterParserChain> getRawChains() {

		final var result = new ArrayList<FilterParserChain>();
		List<FilterParserChars> chain = new ArrayList<>();

		for (var pos = 0; pos < branchFilterChain.size(); pos++) {
			final var current = branchFilterChain.get(pos);

			if (current.isComma()) {
				if (chain.isEmpty() == false) {
					result.add(new FilterParserChain(chain));
					chain = new ArrayList<>();
				}
			} else {
				chain.add(current);
			}
		}

		if (chain.isEmpty() == false) {
			result.add(new FilterParserChain(chain));
		}
		return Collections.unmodifiableList(result);
	}

	@Override
	public String toString() {
		return branchFilterChain.stream()
		        .map(FilterParserChars::toString)
		        .collect(Collectors.joining());
	}

}
