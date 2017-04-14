package org.strangeforest.tcb.stats.model;

public enum KOResult {
	R128, R64, R32, R16, QF, SF, F, W;

	public boolean hasNext() {
		return this != W;
	}

	public KOResult next() {
		return KOResult.values()[ordinal() + 1];
	}

	public KOResult prev() {
		return KOResult.values()[ordinal() - 1];
	}
}
