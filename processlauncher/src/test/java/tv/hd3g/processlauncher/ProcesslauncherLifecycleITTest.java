/*
 * This file is part of processlauncher.
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
 * Copyright (C) hdsdi3g for hd3g.tv 2019
 *
 */
package tv.hd3g.processlauncher;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tv.hd3g.processlauncher.EndStatus.TOO_LONG_EXECUTION_TIME;
import static tv.hd3g.processlauncher.demo.DemoExecIOText.ENV_KEY;
import static tv.hd3g.processlauncher.demo.DemoExecIOText.ENV_VALUE;
import static tv.hd3g.processlauncher.demo.DemoExecIOText.EXIT_OK;
import static tv.hd3g.processlauncher.demo.DemoExecIOText.EXPECTED_ERR;
import static tv.hd3g.processlauncher.demo.DemoExecIOText.EXPECTED_IN;
import static tv.hd3g.processlauncher.demo.DemoExecIOText.EXPECTED_OUT;
import static tv.hd3g.processlauncher.demo.DemoExecLongSleep.MAX_DURATION;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import org.apache.commons.io.IOUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;
import tv.hd3g.processlauncher.cmdline.CommandLine;
import tv.hd3g.processlauncher.cmdline.ExecutableFinder;
import tv.hd3g.processlauncher.cmdline.Parameters;
import tv.hd3g.processlauncher.demo.DemoExecExitCode;
import tv.hd3g.processlauncher.demo.DemoExecIOText;
import tv.hd3g.processlauncher.demo.DemoExecInteractive;
import tv.hd3g.processlauncher.demo.DemoExecLongSleep;
import tv.hd3g.processlauncher.demo.DemoExecLongStdOutErr;
import tv.hd3g.processlauncher.demo.DemoExecShortSleep;
import tv.hd3g.processlauncher.demo.DemoExecSimple;
import tv.hd3g.processlauncher.demo.DemoExecStdinInjection;
import tv.hd3g.processlauncher.demo.DemoExecSubProcess;
import tv.hd3g.processlauncher.demo.DemoExecWorkingdir;

@Slf4j
public class ProcesslauncherLifecycleITTest {// NOSONAR

	private final ExecutableFinder executableFinder;
	private final ScheduledThreadPoolExecutor scheduledThreadPool;

	public ProcesslauncherLifecycleITTest() {
		executableFinder = new ExecutableFinder();
		scheduledThreadPool = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());
	}

	public ProcesslauncherBuilder prepareBuilder(final Class<?> execClass) throws IOException {
		final var parameters = Parameters.of("-cp", System.getProperty("java.class.path"), execClass.getName());
		final var cmd = new CommandLine("java", parameters, executableFinder);
		return new ProcesslauncherBuilder(cmd);
	}

	private CapturedStdOutErrTextRetention textRetention;

	@BeforeEach
	void setUp() {
		textRetention = new CapturedStdOutErrTextRetention();
	}

	private ProcesslauncherLifecycle captureTextAndStart(final ProcesslauncherBuilder pb) throws IOException {
		pb.getSetCaptureStandardOutputAsOutputText(CapturedStreams.BOTH_STDOUT_STDERR).addObserver(textRetention);
		return pb.start();
	}

	@Test
	void testSimpleExec() throws IOException {
		final var result = captureTextAndStart(prepareBuilder(DemoExecSimple.class)).waitForEnd();
		assertEquals(DemoExecSimple.EXPECTED, textRetention.getStdouterr(true, ""));
		assertEquals(0, (int) result.getExitCode());
		assertEquals(EndStatus.CORRECTLY_DONE, result.getEndStatus());
	}

	@Test
	void testWorkingDirectory() throws IOException {
		final var ept = prepareBuilder(DemoExecWorkingdir.class);
		final var wd = new File(System.getProperty("user.dir")).getCanonicalFile();
		ept.setWorkingDirectory(wd);

		assertEquals(wd, ept.getWorkingDirectory());

		final var result = captureTextAndStart(ept).waitForEnd();
		assertEquals(wd, result.getLauncher().getProcessBuilder().directory());

		assertEquals(wd.getPath(), textRetention.getStdouterr(true, ""));
		assertEquals(EndStatus.CORRECTLY_DONE, result.getEndStatus());
	}

	@Test
	void testExecutionCallback() throws Exception {
		final var ept = prepareBuilder(DemoExecSimple.class);

		final var onEndExecutions = new LinkedBlockingQueue<ProcesslauncherLifecycle>();
		final var onPostStartupExecution = new LinkedBlockingQueue<ProcesslauncherLifecycle>();
		final var isAlive = new AtomicBoolean(false);
		ept.addExecutionCallbacker(new ExecutionCallbacker() {
			@Override
			public void onEndExecution(final ProcesslauncherLifecycle processlauncherLifecycle) {
				onEndExecutions.add(processlauncherLifecycle);
			}

			@Override
			public void postStartupExecution(final ProcesslauncherLifecycle processlauncherLifecycle) {
				isAlive.set(true);
				onPostStartupExecution.add(processlauncherLifecycle);
			}
		});

		final var p = ept.start().waitForEnd();
		assertEquals(p, onEndExecutions.poll(500, TimeUnit.MILLISECONDS));
		assertEquals(p, onPostStartupExecution.poll(500, TimeUnit.MILLISECONDS));
		assertTrue(isAlive.get());
	}

	@Test
	void testResultValues() throws Exception {
		final var parameters = Parameters.of("-cp", System.getProperty("java.class.path"),
				DemoExecIOText.class.getName());
		parameters.addParameters(EXPECTED_IN);
		final var cmd = new CommandLine("java", parameters, executableFinder);
		final var ept = new ProcesslauncherBuilder(cmd);

		ept.setExecCodeMustBeZero(false);
		ept.setEnvironmentVar(ENV_KEY, ENV_VALUE);

		final var p = captureTextAndStart(ept).waitForEnd();

		assertEquals(EXPECTED_OUT, textRetention.getStdout(false, ""));
		assertEquals(EXPECTED_ERR, textRetention.getStderr(false, ""));
		assertEquals(EXIT_OK, (int) p.getExitCode());
		assertEquals(EndStatus.CORRECTLY_DONE, p.getEndStatus());

		assertEquals(DemoExecIOText.EXIT_OK, (int) p.getExitCode());
	}

	@Test
	void testMaxExecTime() throws Exception {
		final var ept = prepareBuilder(DemoExecLongSleep.class);

		ept.setExecutionTimeLimiter(MAX_DURATION, MILLISECONDS, scheduledThreadPool);

		final var startTime = System.currentTimeMillis();
		final var result = ept.start().waitForEnd();

		final var duration = System.currentTimeMillis() - startTime;

		MatcherAssert.assertThat(duration, Matchers.lessThan(MAX_DURATION
															 + 1500)); /** 1500 is a "startup time bonus" */
		assertEquals(TOO_LONG_EXECUTION_TIME, result.getEndStatus());

		assertTrue(result.isTooLongTime());
		assertFalse(result.isCorrectlyDone());
		assertFalse(result.isKilled());
		assertFalse(result.isRunning());
	}

	@Test
	void testKill() throws Exception {
		final var ept = prepareBuilder(DemoExecLongSleep.class);

		final var startTime = System.currentTimeMillis();
		final var result = ept.start();

		scheduledThreadPool.schedule(() -> {
			result.kill();
		}, MAX_DURATION, MILLISECONDS);

		result.waitForEnd();

		final var duration = System.currentTimeMillis() - startTime;

		MatcherAssert.assertThat(duration, Matchers.lessThan(MAX_DURATION
															 + 1500)); /** 1500 is a "startup time bonus" */
		assertEquals(EndStatus.KILLED, result.getEndStatus());

		assertFalse(result.isTooLongTime());
		assertFalse(result.isCorrectlyDone());
		assertTrue(result.isKilled());
		assertFalse(result.isRunning());
	}

	@Test
	void testKillSubProcess() throws Exception {
		final var ept = prepareBuilder(DemoExecSubProcess.class);

		final var startTime = System.currentTimeMillis();
		final var result = ept.start();

		scheduledThreadPool.schedule(() -> {
			result.kill();
		}, DemoExecLongSleep.MAX_DURATION * 4, TimeUnit.MILLISECONDS);

		assertTrue(result.isRunning());
		Thread.sleep(DemoExecLongSleep.MAX_DURATION);// NOSONAR
		/**
		 * flacky on linux
		 * assertEquals(1, result.getProcess().children().count());
		 * assertEquals(1, result.getProcess().descendants().count());
		 */

		result.waitForEnd();

		final var duration = System.currentTimeMillis() - startTime;

		MatcherAssert.assertThat(duration, Matchers.lessThan(DemoExecLongSleep.MAX_DURATION * 16));
		assertEquals(EndStatus.KILLED, result.getEndStatus());

		assertFalse(result.isTooLongTime());
		assertFalse(result.isCorrectlyDone());
		assertTrue(result.isKilled());
		assertFalse(result.isRunning());

		assertEquals(0, result.getProcess().descendants().count());
	}

	@Test
	void testTimesAndProcessProps() throws Exception {
		final var ept = prepareBuilder(DemoExecSubProcess.class);

		ept.setExecutionTimeLimiter(DemoExecLongSleep.MAX_DURATION, TimeUnit.MILLISECONDS, scheduledThreadPool);

		final var startTime = System.currentTimeMillis();
		final var result = ept.start().waitForEnd();

		final var duration = System.currentTimeMillis() - startTime;
		MatcherAssert.assertThat(duration * 2, Matchers.greaterThanOrEqualTo(result.getUptime(TimeUnit.MILLISECONDS)));
	}

	@Test
	void testInteractiveHandler() throws Exception {
		final var parameters = Parameters.of("-cp", System.getProperty("java.class.path"),
				DemoExecInteractive.class.getName());
		parameters.addParameters("foo");
		final var cmd = new CommandLine("java", parameters, executableFinder);
		final var ept = new ProcesslauncherBuilder(cmd);

		ept.setExecutionTimeLimiter(500, TimeUnit.MILLISECONDS, new ScheduledThreadPoolExecutor(1));

		final var errors = new LinkedBlockingQueue<Exception>();

		final Function<LineEntry, String> interactive = lineEntry -> {
			final var line = lineEntry.line();
			if (lineEntry.stdErr()) {
				System.err.println("Process say: " + line);
				errors.add(new Exception("isStdErr is true"));
				return DemoExecInteractive.QUIT;
			} else if (line.equals("FOO")) {
				return "bar";
			} else if (line.equals("foo")) {
				errors.add(new Exception("foo is in lowercase"));
				return DemoExecInteractive.QUIT;
			} else if (line.equals("BAR")) {
				return DemoExecInteractive.QUIT;
			} else if (line.equals("bar")) {
				errors.add(new Exception("bar is in lowercase"));
				return DemoExecInteractive.QUIT;
			} else {
				errors.add(new Exception("Invalid line " + line));
				return null;
			}
		};

		ept.getSetCaptureStandardOutputAsOutputText(CapturedStreams.BOTH_STDOUT_STDERR)
				.addObserver(new CapturedStdOutErrTextInteractive(interactive));

		final var result = ept.start().waitForEnd();

		if (errors.isEmpty() == false) {
			errors.forEach(e -> {
				log.error("ProcesslauncherLifecycle error", e);
			});
			Assertions.fail();
		}

		assertEquals(EndStatus.CORRECTLY_DONE, result.getEndStatus());
		assertTrue(result.isCorrectlyDone());
	}

	@Test
	void testWaitForEnd() throws Exception {
		final var ept = prepareBuilder(DemoExecShortSleep.class);
		assertTrue(ept.start().waitForEnd(500, TimeUnit.MILLISECONDS).isCorrectlyDone());
	}

	@Test
	void testToString() throws IOException {
		assertNotNull(prepareBuilder(DemoExecSimple.class).start().toString());
	}

	@Test
	void testCheckExecutionOk() throws IOException {
		final var parameters = Parameters.of("-cp", System.getProperty("java.class.path"),
				DemoExecExitCode.class.getName());
		parameters.addParameters("0");
		final var cmd = new CommandLine("java", parameters, executableFinder);
		final var ept1 = new ProcesslauncherBuilder(cmd);

		ept1.start().waitForEnd().checkExecution();
	}

	@Test
	void testCheckExecutionError() throws IOException {
		final var parameters = Parameters.of("-cp", System.getProperty("java.class.path"),
				DemoExecExitCode.class.getName());
		parameters.addParameters("1");
		final var cmd = new CommandLine("java", parameters, executableFinder);
		final var result = new ProcesslauncherBuilder(cmd).start();

		try {
			result.waitForEnd().checkExecution();
			Assertions.fail("Missing exception");
		} catch (final Exception e) {
			assertEquals(1, (int) result.getExitCode());
		}
	}

	@Test
	void testStdInInjection() throws IOException, InterruptedException {
		final var ept = prepareBuilder(DemoExecStdinInjection.class);
		ept.setExecutionTimeLimiter(500, TimeUnit.MILLISECONDS, scheduledThreadPool);

		final var result = ept.start();
		result.getStdInInjection().println(DemoExecStdinInjection.QUIT);
		result.waitForEnd().checkExecution();
		Thread.sleep(10);// NOSONAR
		assertTrue(result.isCorrectlyDone());
	}

	@Test
	void testDirectStandardOutputStdErrRetention() throws IOException, InterruptedException {
		final var ept = prepareBuilder(DemoExecLongStdOutErr.class);
		ept.setExecutionTimeLimiter(500, TimeUnit.MILLISECONDS, scheduledThreadPool);

		final var lineCount = new AtomicInteger();
		final var endOk = new AtomicBoolean();
		final var errors = new AtomicReference<Exception>();

		final InputStreamConsumer stdOutConsumer = (processInputStream, source) -> {
			final var lines = IOUtils.readLines(processInputStream, StandardCharsets.UTF_8);
			lineCount.set((int) lines.stream().filter(l -> l.equals(DemoExecLongStdOutErr.STD_OUT)).count());
			endOk.set(lines.get(lines.size() - 1).equals(DemoExecLongStdOutErr.STD_OUT_END));
		};

		ept.setCaptureStandardOutput(new DirectStandardOutputStdErrRetention(textRetention, stdOutConsumer));
		final var result = ept.start();
		result.waitForEnd().checkExecution();
		Thread.sleep(10);// NOSONAR
		assertTrue(result.isCorrectlyDone());

		assertEquals(DemoExecLongStdOutErr.STD_ERR, textRetention.getStdouterr(true, ""));
		assertEquals(DemoExecLongStdOutErr.COUNT, lineCount.get());
		assertTrue(endOk.get());
		assertNull(errors.get());
	}

}
