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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ExecutionTimeLimiterTest {

	@Test
	void test() {

		final var fakeSES = new FakesScheduledExecutorService();
		final var etl = new ExecutionTimeLimiter(1, TimeUnit.SECONDS, fakeSES);

		assertEquals(1000, etl.getMaxExecTime(TimeUnit.MILLISECONDS));

		final var toCallBack = Mockito.mock(ProcesslauncherLifecycle.class);
		final var process = Mockito.mock(Process.class);
		Mockito.when(process.onExit()).thenReturn(CompletableFuture.completedFuture(null));

		etl.addTimesUp(toCallBack, process);
		Mockito.verify(toCallBack).runningTakesTooLongTimeStopIt();

		assertEquals(1000, fakeSES.delay);
		assertEquals(TimeUnit.MILLISECONDS, fakeSES.unit);
		assertTrue(fakeSES.returned.hasCancel);
	}

	static class ReturnedScheduledFuture implements ScheduledFuture<Void> {
		private boolean hasCancel;

		ReturnedScheduledFuture() {
			hasCancel = false;
		}

		@Override
		public long getDelay(final TimeUnit unit) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int compareTo(final Delayed o) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean cancel(final boolean mayInterruptIfRunning) {
			return hasCancel = true;
		}

		@Override
		public boolean isCancelled() {
			return hasCancel;
		}

		@Override
		public boolean isDone() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Void get() throws InterruptedException, ExecutionException {
			throw new UnsupportedOperationException();
		}

		@Override
		public Void get(final long timeout,
		                final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
			throw new UnsupportedOperationException();
		}
	}

	static class FakesScheduledExecutorService implements ScheduledExecutorService {

		private long delay;
		private TimeUnit unit;
		private ReturnedScheduledFuture returned;

		@Override
		public ScheduledFuture<?> schedule(final Runnable command, final long delay, final TimeUnit unit) {
			command.run();
			this.delay = delay;
			this.unit = unit;
			returned = new ReturnedScheduledFuture();
			return returned;
		}

		@Override
		public void shutdown() {
		}

		@Override
		public List<Runnable> shutdownNow() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isShutdown() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isTerminated() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException {
			throw new UnsupportedOperationException();
		}

		@Override
		public <T> Future<T> submit(final Callable<T> task) {
			throw new UnsupportedOperationException();
		}

		@Override
		public <T> Future<T> submit(final Runnable task, final T result) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Future<?> submit(final Runnable task) {
			throw new UnsupportedOperationException();
		}

		@Override
		public <T> List<Future<T>> invokeAll(final Collection<? extends Callable<T>> tasks) throws InterruptedException {
			throw new UnsupportedOperationException();
		}

		@Override
		public <T> List<Future<T>> invokeAll(final Collection<? extends Callable<T>> tasks,
		                                     final long timeout,
		                                     final TimeUnit unit) throws InterruptedException {
			throw new UnsupportedOperationException();
		}

		@Override
		public <T> T invokeAny(final Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
			throw new UnsupportedOperationException();
		}

		@Override
		public <T> T invokeAny(final Collection<? extends Callable<T>> tasks,
		                       final long timeout,
		                       final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void execute(final Runnable command) {
			command.run();
		}

		@Override
		public <V> ScheduledFuture<V> schedule(final Callable<V> callable, final long delay, final TimeUnit unit) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ScheduledFuture<?> scheduleAtFixedRate(final Runnable command,
		                                              final long initialDelay,
		                                              final long period,
		                                              final TimeUnit unit) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ScheduledFuture<?> scheduleWithFixedDelay(final Runnable command,
		                                                 final long initialDelay,
		                                                 final long delay,
		                                                 final TimeUnit unit) {
			throw new UnsupportedOperationException();
		}

	}

}
