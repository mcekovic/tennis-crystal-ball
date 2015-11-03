package org.strangeforest.tcb.stats.util;

import java.util.function.*;

class ExpirableMemoizer<T> extends Memoizer<T> {

	private final long expiryPeriod;
	private long timestamp;

	ExpirableMemoizer(Supplier<T> supplier, long expiryPeriod) {
		super(supplier);
		this.expiryPeriod = expiryPeriod;
	}

	@Override protected boolean isCalculated() {
		return super.isCalculated() && (timestamp + expiryPeriod >= System.currentTimeMillis());
	}

	@Override protected void calculate() {
		super.calculate();
		timestamp = System.currentTimeMillis();
	}

	@Override public void clear() {
		super.clear();
		timestamp = 0L;
	}
}
