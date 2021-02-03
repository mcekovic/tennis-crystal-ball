package org.strangeforest.tcb.util;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

public class LockManager<T> {

	private final Map<T, EntryLock> locks = new HashMap<>();

	public <R> R withLock(T key, Callable<R> callback) throws Exception {
		getLock(key).lock();
		try {
			return callback.call();
		}
		finally {
			unlock(key);
		}
	}

	public <R> R withLock(T key1, T key2, Callable<R> callback) throws Exception {
		getLock(key1).lock();
		try {
			getLock(key2).lock();
			try {
				return callback.call();
			}
			finally {
				unlock(key2);
			}
		}
		finally {
			unlock(key1);
		}
	}

	public void runLocked(T key1, T key2, Runnable callback)  {
		getLock(key1).lock();
		try {
			getLock(key2).lock();
			try {
				callback.run();
			}
			finally {
				unlock(key2);
			}
		}
		finally {
			unlock(key1);
		}
	}

	private synchronized EntryLock getLock(T key) {
		var lock = locks.get(key);
		if (lock == null) {
			lock = new EntryLock();
			locks.put(key, lock);
		}
		lock.incRefCount();
		return lock;
	}

	private synchronized void unlock(T key) {
		var lock = locks.get(key);
		if (lock == null || !lock.isLocked())
			throw new IllegalStateException("Key not locked: " + key);
		if (lock.decRefCount() <= 0)
			locks.remove(key);
		lock.unlock();
	}

	private static final class EntryLock extends ReentrantLock {

		private int refCount;

		void incRefCount() {
			++refCount;
		}

		int decRefCount() {
			return --refCount;
		}
	}
}
