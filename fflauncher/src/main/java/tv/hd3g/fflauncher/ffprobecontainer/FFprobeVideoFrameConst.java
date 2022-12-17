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
package tv.hd3g.fflauncher.ffprobecontainer;

import java.util.Objects;

public record FFprobeVideoFrameConst(FFprobeVideoFrame updatedWith,
									 int width,
									 int height,
									 String pixFmt,
									 String sampleAspectRatio,
									 int codedPictureNumber,
									 int displayPictureNumber,
									 boolean interlacedFrame,
									 boolean topFieldFirst,
									 String colorRange,
									 String colorSpace,
									 String colorPrimaries,
									 String colorTransfer) {

	public boolean valuesEquals(final FFprobeVideoFrameConst obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final var other = obj;
		return codedPictureNumber == other.codedPictureNumber
			   && Objects.equals(colorPrimaries, other.colorPrimaries)
			   && Objects.equals(colorRange, other.colorRange)
			   && Objects.equals(colorSpace, other.colorSpace)
			   && Objects.equals(colorTransfer, other.colorTransfer)
			   && displayPictureNumber == other.displayPictureNumber
			   && height == other.height
			   && interlacedFrame == other.interlacedFrame
			   && Objects.equals(pixFmt, other.pixFmt)
			   && Objects.equals(sampleAspectRatio, other.sampleAspectRatio)
			   && topFieldFirst == other.topFieldFirst
			   && width == other.width;
	}

}
