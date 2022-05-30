package tv.hd3g.fflauncher.enums;

import java.io.IOException;
import java.io.UncheckedIOException;

public enum SourceNotFoundPolicy {
	ERROR,
	REMOVE_OUT_STREAM;

	public static class SourceNotFoundException extends UncheckedIOException {
		public SourceNotFoundException(final String message) {
			super(new IOException(message));
		}
	}
}
