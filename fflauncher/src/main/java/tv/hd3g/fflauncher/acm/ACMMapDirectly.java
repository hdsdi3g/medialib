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

class ACMMapDirectly implements ACMExportableMapReference, ACMLinkableOutStreamReference {
	private final OutputAudioStream outputAudioStream;
	private final InputAudioStream inputAudioStream;

	ACMMapDirectly(final InputAudioStream inputAudioStream, final OutputAudioStream outputAudioStream) {
		this.inputAudioStream = inputAudioStream;
		this.outputAudioStream = outputAudioStream;
	}

	@Override
	public OutputAudioStream getLinkableOutStreamReference() {
		return outputAudioStream;
	}

	@Override
	public String toMapReferenceAsInput() {
		return inputAudioStream.toMapReferenceAsInput();
	}
}