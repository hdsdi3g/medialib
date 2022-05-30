package tv.hd3g.processlauncher.cmdline;

import java.util.List;

@FunctionalInterface
public interface ArgValueChoice {

	/**
	 * In key args collision, chose the value list content.
	 * You can filter/alterate the returned list content.
	 * @param actualValues [val1, val2, val3, ...] like -argKey val1 -argKey val2 -argKey val3 ...
	 * @param comparedValues [val1, val2, val3, ...] like -argKey val1 -argKey val2 -argKey val3 ...
	 * @return actualValue/comparedValue/other string
	 *         OR null for remove the argument
	 *         OR empty for convert argument to simple argument
	 */
	List<String> choose(String argKey, List<String> actualValues, List<String> comparedValues);

}
