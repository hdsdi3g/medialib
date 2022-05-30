package tv.hd3g.fflauncher.filtering.parser;

import java.util.List;

import tv.hd3g.fflauncher.filtering.FilterArgument;

public interface FilterParserDefinition {

	void setSourceBlocks(List<String> sourceBlocks);

	void setDestBlocks(List<String> destBlocks);

	void setFilterName(String filterName);

	void setArguments(List<FilterArgument> arguments);

}
