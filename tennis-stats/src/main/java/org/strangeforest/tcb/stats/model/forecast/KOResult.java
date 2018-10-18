package org.strangeforest.tcb.stats.model.forecast;

public enum KOResult {
	R128, R64, R32, R16, QF, SF, F, W;

	public boolean hasNext() {
		return this != W;
	}

	public KOResult next() {
		return KOResult.values()[ordinal() + 1];
	}

	public boolean hasPrev() {
		return ordinal() > 0;
	}

	public KOResult prev() {
		return KOResult.values()[ordinal() - 1];
	}

	public KOResult offset(int offset) {
		return KOResult.values()[ordinal() + offset];
	}
}
