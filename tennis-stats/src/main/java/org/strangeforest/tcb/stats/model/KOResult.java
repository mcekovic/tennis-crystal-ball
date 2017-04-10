package org.strangeforest.tcb.stats.model;

public enum KOResult {
	R128, R64, R32, R16, QF, SF, F, W;

	public KOResult next() {
		return KOResult.values()[ordinal() + 1];
	}
}
