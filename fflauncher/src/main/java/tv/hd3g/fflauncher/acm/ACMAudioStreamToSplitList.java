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
 * Copyright (C) hdsdi3g for hd3g.tv 2020
 *
 */
package tv.hd3g.fflauncher.acm;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

import tv.hd3g.fflauncher.acm.ACMSplitInStreamDefinitionFilter.SplittedOut;
import tv.hd3g.fflauncher.acm.OutputAudioStream.OutputAudioChannel;

class ACMAudioStreamToSplitList extends ArrayList<ACMSplitInStreamDefinitionFilter> {

	Optional<ACMSplitInStreamDefinitionFilter> findFirst(final InputAudioStream item) {
		return findAll(item).findFirst();
	}

	Stream<ACMSplitInStreamDefinitionFilter> findAll(final InputAudioStream item) {
		final var condition = containCondition();
		return stream().filter(t -> condition.test(t, item));
	}

	protected BiPredicate<ACMSplitInStreamDefinitionFilter, InputAudioStream> containCondition() {
		return (split, intStream) -> split.getInputAudioStream().equals(intStream);
	}

	Optional<SplittedOut> search(final OutputAudioChannel outputAudioChannel) {
		return findAll(outputAudioChannel.getInputAudioStream())
		        .filter(splitIn -> {
			        final var contain = splitIn.getSplittedOut().containsKey(outputAudioChannel.getChInIndex());
			        if (contain == false) {
				        return false;
			        }
			        final var split = splitIn.getSplittedOut().get(outputAudioChannel.getChInIndex());
			        return split.getOutputAudioChannel().equals(outputAudioChannel);
		        }).map(splitIn -> splitIn.getSplittedOut().get(outputAudioChannel.getChInIndex()))
		        .findFirst();
	}
}