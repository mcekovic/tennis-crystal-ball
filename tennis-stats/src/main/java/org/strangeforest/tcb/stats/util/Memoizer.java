package org.strangeforest.tcb.stats.util;

import java.util.function.*;

public final class Memoizer<T> implements Supplier<T> {

	public static <T> Supplier<T> of(Supplier<T> calculation) {
		return new Memoizer<>(calculation);
	}

	private final Supplier<T> supplier;
	private boolean calculated;
	private T cachedResult;

	private Memoizer(Supplier<T> supplier) {
		this.supplier = supplier;
	}

	@Override public final T get() {
		if (!calculated) {
			cachedResult = supplier.get();
			calculated = true;
		}
		return cachedResult;
	}

	public void clear() {
		cachedResult = null;
		calculated = false;
	}
}
