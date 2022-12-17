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
package tv.hd3g.fflauncher.progress;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class AtomicLatchReference<T> {

	private static final String CAN_T_WAIT_THE_AVAILABILITY_OF_OBJECT_REFERENCE = "Can't wait the availability of object reference";
	private final AtomicReference<T> reference;
	private final CountDownLatch latchReady;

	public AtomicLatchReference() {
		reference = new AtomicReference<>();
		latchReady = new CountDownLatch(1);
	}

	public void set(final T object) {
		reference.set(object);
		latchReady.countDown();
	}

	public T get(final long timeout, final TimeUnit unit) {
		try {
			if (latchReady.await(timeout, unit) == false) {
				throw new IllegalStateException(CAN_T_WAIT_THE_AVAILABILITY_OF_OBJECT_REFERENCE);
			}
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(CAN_T_WAIT_THE_AVAILABILITY_OF_OBJECT_REFERENCE, e);
		}
		return reference.get();
	}

	public <E extends Throwable> void get(final long timeout,
										  final TimeUnit unit,
										  final ConsumerWithException<T, E> ifAvailable) throws E {
		try {
			if (latchReady.await(timeout, unit) == false) {
				return;
			}
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(CAN_T_WAIT_THE_AVAILABILITY_OF_OBJECT_REFERENCE, e);
		}
		ifAvailable.accept(reference.get());
	}

	public interface ConsumerWithException<T, E extends Throwable> {
		void accept(T t) throws E;
	}

}
