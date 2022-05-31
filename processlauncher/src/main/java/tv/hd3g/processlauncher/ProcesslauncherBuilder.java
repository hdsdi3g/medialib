package tv.hd3g.processlauncher;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import tv.hd3g.processlauncher.cmdline.CommandLine;
import tv.hd3g.processlauncher.cmdline.ExecutableFinder;

public class ProcesslauncherBuilder {

	private final File executable;
	private final List<String> parameters;
	private final LinkedHashMap<String, String> environment;
	private File workingDirectory;

	private boolean execCodeMustBeZero;
	private final List<ExecutionCallbacker> executionCallbackers;
	private Optional<ExecutionTimeLimiter> executionTimeLimiter;
	private Optional<CaptureStandardOutput> captureStandardOutput;
	private Optional<ExternalProcessStartup> externalProcessStartup;

	public ProcesslauncherBuilder(final File executable, final Collection<String> parameters,
	                              final ExecutableFinder execFinder) {
		this.executable = Objects.requireNonNull(executable, "\"executable\" can't to be null");
		this.parameters = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(parameters,
		        "\"parameters\" can't to be null")));

		environment = new LinkedHashMap<>();

		environment.putAll(System.getenv());
		if (environment.containsKey("LANG") == false) {
			environment.put("LANG",
			        Locale.getDefault().getLanguage() + "_" + Locale.getDefault().getCountry() + "." + UTF_8);
		}
		if (execFinder != null) {
			environment.put("PATH", execFinder.getFullPathToString());
		} else {
			environment.put("PATH", System.getenv("PATH"));
		}
		execCodeMustBeZero = true;
		executionCallbackers = new ArrayList<>();
		executionTimeLimiter = Optional.empty();
		captureStandardOutput = Optional.empty();
		externalProcessStartup = Optional.empty();
	}

	public ProcesslauncherBuilder(final File executable, final Collection<String> parameters) {
		this(executable, parameters, null);
	}

	public ProcesslauncherBuilder(final CommandLine commandLine) {
		this(commandLine.getExecutable(), commandLine.getParameters().getParameters(), commandLine.getExecutableFinder()
		        .orElseGet(ExecutableFinder::new));
	}

	/**
	 * @return null if not found
	 */
	public String getEnvironmentVar(final String key) {
		return environment.get(key);
	}

	public ProcesslauncherBuilder setEnvironmentVar(final String key, final String value) {
		if (key.equalsIgnoreCase("path")
		    && Optional.ofNullable(System.getProperty("os.name")).orElse("").toLowerCase().indexOf("win") >= 0) {
			environment.put("PATH", value);
			environment.put("Path", value);
		} else {
			environment.put(key, value);
		}
		return this;
	}

	public ProcesslauncherBuilder setEnvironmentVarIfNotFound(final String key, final String value) {
		if (environment.containsKey(key)) {
			return this;
		}
		return setEnvironmentVar(key, value);
	}

	public void forEachEnvironmentVar(final BiConsumer<String, String> action) {
		environment.forEach(action);
	}

	public File getWorkingDirectory() {
		return workingDirectory;
	}

	public ProcesslauncherBuilder setWorkingDirectory(final File workingDirectory) throws IOException {
		Objects.requireNonNull(workingDirectory, "\"workingDirectory\" can't to be null");

		if (workingDirectory.exists() == false) {
			throw new FileNotFoundException("\"" + workingDirectory.getPath() + "\" in filesytem");
		} else if (workingDirectory.canRead() == false) {
			throw new IOException("Can't read workingDirectory \"" + workingDirectory.getPath() + "\"");
		} else if (workingDirectory.isDirectory() == false) {
			throw new FileNotFoundException("\"" + workingDirectory.getPath() + "\" is not a directory");
		}
		this.workingDirectory = workingDirectory;
		return this;
	}

	/**
	 * Default, yes.
	 */
	public ProcesslauncherBuilder setExecCodeMustBeZero(final boolean execCodeMustBeZero) {
		this.execCodeMustBeZero = execCodeMustBeZero;
		return this;
	}

	/**
	 * Default, yes.
	 */
	public boolean isExecCodeMustBeZero() {
		return execCodeMustBeZero;
	}

	/**
	 * @return unmodifiableList
	 */
	public List<ExecutionCallbacker> getExecutionCallbackers() {
		synchronized (executionCallbackers) {
			return Collections.unmodifiableList(executionCallbackers);
		}
	}

	public ProcesslauncherBuilder addExecutionCallbacker(final ExecutionCallbacker executionCallbacker) {
		Objects.requireNonNull(executionCallbacker, "\"endExecutionCallbacker\" can't to be null");
		synchronized (executionCallbackers) {
			executionCallbackers.add(executionCallbacker);
		}
		return this;
	}

	public ProcesslauncherBuilder removeExecutionCallbacker(final ExecutionCallbacker executionCallbacker) {
		Objects.requireNonNull(executionCallbacker, "\"endExecutionCallbacker\" can't to be null");
		synchronized (executionCallbackers) {
			executionCallbackers.remove(executionCallbacker);
		}
		return this;
	}

	public Optional<ExecutionTimeLimiter> getExecutionTimeLimiter() {
		return executionTimeLimiter;
	}

	public ProcesslauncherBuilder setExecutionTimeLimiter(final ExecutionTimeLimiter executionTimeLimiter) {
		this.executionTimeLimiter = Optional.ofNullable(executionTimeLimiter);
		return this;
	}

	public Optional<ExternalProcessStartup> getExternalProcessStartup() {
		return externalProcessStartup;
	}

	public ProcesslauncherBuilder setExternalProcessStartup(final ExternalProcessStartup externalProcessStartup) {
		this.externalProcessStartup = Optional.ofNullable(externalProcessStartup);
		return this;
	}

	public ProcesslauncherBuilder setCaptureStandardOutput(final CaptureStandardOutput captureStandardOutput) {
		this.captureStandardOutput = Optional.ofNullable(captureStandardOutput);
		return this;
	}

	public Optional<CaptureStandardOutput> getCaptureStandardOutput() {
		return captureStandardOutput;
	}

	public ProcessBuilder makeProcessBuilder() {
		final List<String> fullCommandLine = new ArrayList<>();
		fullCommandLine.add(executable.getPath());
		fullCommandLine.addAll(parameters);

		final var processBuilder = new ProcessBuilder(fullCommandLine);
		processBuilder.environment().putAll(environment);

		if (workingDirectory != null && workingDirectory.exists() && workingDirectory.isDirectory()) {
			processBuilder.directory(workingDirectory);
		} else {
			processBuilder.directory(new File(System.getProperty("user.dir", new File(".").getAbsolutePath())));
		}
		return processBuilder;
	}

	static final UnaryOperator<String> addQuotesIfSpaces = s -> {
		if (s.contains(" ")) {
			return "\"" + s + "\"";
		} else {
			return s;
		}
	};

	public String getFullCommandLine() {
		final var sb = new StringBuilder();
		sb.append(addQuotesIfSpaces.apply(executable.getPath()));
		sb.append(" ");
		sb.append(parameters.stream().map(addQuotesIfSpaces).collect(Collectors.joining(" ")));
		return sb.toString().trim();
	}

	public String getExecutableName() {
		return executable.getName();
	}

	/**
	 * @return getFullCommandLine()
	 */
	@Override
	public String toString() {
		return getFullCommandLine();
	}

	/**
	 * @return new Processlauncher(this)
	 */
	public Processlauncher toProcesslauncher() {
		return new Processlauncher(this);
	}

	/**
	 * Shortcut for CaptureStandardOutputText. Set if missing or not a CaptureStandardOutputText.
	 */
	public CaptureStandardOutputText getSetCaptureStandardOutputAsOutputText(final CapturedStreams defaultCaptureOutStreamsBehavior) {
		final var csot = getCaptureStandardOutput()
		        .filter(CaptureStandardOutputText.class::isInstance)
		        .map(CaptureStandardOutputText.class::cast)
		        .orElseGet(() -> new CaptureStandardOutputText(defaultCaptureOutStreamsBehavior));

		setCaptureStandardOutput(csot);
		return csot;
	}

	/**
	 * Shortcut for CaptureStandardOutputText. Set if missing or not a CaptureStandardOutputText.
	 */
	public CaptureStandardOutputText getSetCaptureStandardOutputAsOutputText() {
		return getSetCaptureStandardOutputAsOutputText(CapturedStreams.BOTH_STDOUT_STDERR);
	}

	/**
	 * @return toProcesslauncher().start()
	 */
	public ProcesslauncherLifecycle start() throws IOException {
		return toProcesslauncher().start();
	}

	/**
	 * Shortcut for setExecutionTimeLimiter
	 */
	public ProcesslauncherBuilder setExecutionTimeLimiter(final long maxExecTime,
	                                                      final TimeUnit unit,
	                                                      final ScheduledExecutorService maxExecTimeScheduler) {
		return setExecutionTimeLimiter(new ExecutionTimeLimiter(maxExecTime, unit, maxExecTimeScheduler));
	}

}
