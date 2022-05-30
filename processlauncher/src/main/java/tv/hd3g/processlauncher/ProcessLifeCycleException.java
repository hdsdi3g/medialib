package tv.hd3g.processlauncher;

public class ProcessLifeCycleException extends RuntimeException {

	public ProcessLifeCycleException(final String message) {
		super(message);
	}

	public ProcessLifeCycleException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
