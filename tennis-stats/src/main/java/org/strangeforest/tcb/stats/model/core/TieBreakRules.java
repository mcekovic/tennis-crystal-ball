package org.strangeforest.tcb.stats.model.core;

public class TieBreakRules {

	public static final TieBreakRules NO_TIE_BREAK = new TieBreakRules(null, 0);
	public static final TieBreakRules COMMON_TIE_BREAK = new TieBreakRules(6, 7);
	public static final TieBreakRules SUPER_TIE_BREAK = new TieBreakRules(6, 10);
	public static final TieBreakRules WB_TIE_BREAK = new TieBreakRules(12, 7);

	private final Integer tieBreakAt;
	private final int tieBreakPoints;

	public TieBreakRules(Integer tieBreakAt, int tieBreakPoints) {
		this.tieBreakAt = tieBreakAt;
		this.tieBreakPoints = tieBreakPoints;
	}

	public Integer getTieBreakAt() {
		return tieBreakAt;
	}

	public int getTieBreakPoints() {
		return tieBreakPoints;
	}

	public boolean isTieBreakFor(int games) {
		return tieBreakAt != null && games == tieBreakAt;
	}
}
