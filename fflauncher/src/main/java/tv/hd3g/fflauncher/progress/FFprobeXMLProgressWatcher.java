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
 * Copyright (C) hdsdi3g for hd3g.tv 2024
 *
 */
package tv.hd3g.fflauncher.progress;

import static java.util.Optional.empty;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;

import tv.hd3g.fflauncher.recipes.ContainerAnalyserBase;

public record FFprobeXMLProgressWatcher(Duration programDuration,
										Consumer<ContainerAnalyserBase<?, ?>> onStartCallback,
										Consumer<FFprobeXMLProgressEvent> progressCallback,
										Consumer<ContainerAnalyserBase<?, ?>> onEndCallback) {

	public static record FFprobeXMLProgressEvent(double progress, float speed, Object session) {
	}

	public <T extends ContainerAnalyserBase<?, ?>> FFprobeXMLProgressConsumer createProgress(final T session,
																							 final ThreadFactory threadFactory) {
		if (programDuration.isZero() || programDuration.isNegative()) {
			return t -> {
			};
		}
		return new FFProbeXMLProgressHandler(this, session, Optional.ofNullable(threadFactory));
	}

	public <T extends ContainerAnalyserBase<?, ?>> FFprobeXMLProgressConsumer createProgress(final T session) {
		return createProgress(session, null);
	}

	public <T extends ContainerAnalyserBase<?, ?>> Optional<FFProbeXMLProgressHandler> createHandler(final T session) {
		if (programDuration.isZero() || programDuration.isNegative()) {
			return empty();
		}
		return Optional.ofNullable(new FFProbeXMLProgressHandler(this, session, empty()));
	}

}
