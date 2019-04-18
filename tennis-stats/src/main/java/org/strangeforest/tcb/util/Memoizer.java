package org.strangeforest.tcb.util;

import java.util.function.*;

public final class Memoizer<T> implements Supplier<T> {

	public static <T> Memoizer<T> of(Supplier<T> calculation) {
		return new Memoizer<>(calculation, -1L);
	}
	public static <T> Memoizer<T> of(Supplier<T> calculation, long validity) {
		return new Memoizer<>(calculation, validity);
	}

	private final Supplier<T> supplier;
	private final long validityPeriod;
	private T cachedResult;
	private boolean calculated;
	private long calculatedAt;

	private Memoizer(Supplier<T> supplier, long validityPeriod) {
		this.supplier = supplier;
		this.validityPeriod = validityPeriod;
	}

	@Override public synchronized final T get() {
		if (!calculated || (validityPeriod >= 0L && System.currentTimeMillis() - calculatedAt > validityPeriod)) {
			cachedResult = supplier.get();
			calculated = true;
			if (validityPeriod >= 0L)
				calculatedAt = System.currentTimeMillis();
		}
		return cachedResult;
	}

	public synchronized void clear() {
		cachedResult = null;
		calculated = false;
		calculatedAt = 0L;
	}
}
