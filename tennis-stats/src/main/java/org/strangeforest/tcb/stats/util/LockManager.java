package org.strangeforest.tcb.stats.util;

import java.util.*;
import java.util.concurrent.locks.*;
import java.util.function.*;

public class LockManager<T> {

	private final Map<T, EntryLock> locks = new HashMap<>();

	public <R> R withLock(T key, Supplier<R> callback) {
		getLock(key).lock();
		try {
			return callback.get();
		}
		finally {
			unlock(key);
		}
	}

	private synchronized EntryLock getLock(T key) {
		EntryLock lock = locks.get(key);
		if (lock == null) {
			lock = new EntryLock();
			locks.put(key, lock);
		}
		lock.incRefCount();
		return lock;
	}

	private synchronized void unlock(T key) {
		EntryLock lock = locks.get(key);
		if (lock == null || !lock.isLocked())
			throw new IllegalStateException("Key not locked: " + key);
		if (lock.decRefCount() <= 0)
			locks.remove(key);
		lock.unlock();
	}

	private static final class EntryLock extends ReentrantLock {

		private int refCount;

		public void incRefCount() {
			++refCount;
		}

		public int decRefCount() {
			return --refCount;
		}
	}
}
