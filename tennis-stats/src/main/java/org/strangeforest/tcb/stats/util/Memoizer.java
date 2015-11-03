package org.strangeforest.tcb.stats.util;

import java.util.function.*;

public class Memoizer<T> implements Supplier<T> {

	public static <T> Supplier<T> of(Supplier<T> calculation) {
		return new Memoizer<>(calculation);
	}

	public static <T> Supplier<T> of(Supplier<T> calculation, long expiryPeriod) {
		return expiryPeriod > 0 ? new ExpirableMemoizer<>(calculation, expiryPeriod) : new Memoizer<>(calculation);
	}

	private final Supplier<T> supplier;
	private boolean calculated;
	private T cachedResult;

	protected Memoizer(Supplier<T> supplier) {
		this.supplier = supplier;
	}

	@Override public final T get() {
		if (!isCalculated())
			calculate();
		return cachedResult;
	}

	protected boolean isCalculated() {
		return calculated;
	}

	protected void calculate() {
		cachedResult = supplier.get();
		calculated = true;
	}

	public void clear() {
		cachedResult = null;
		calculated = false;
	}
}
