package org.strangeforest.tcb.stats.model;

public class BigWinRoundFactorRow {

	private final String level;
	private final String round;
	private final int roundFactor;

	public BigWinRoundFactorRow(String level, String round, int roundFactor) {
		this.level = level;
		this.round = round;
		this.roundFactor = roundFactor;
	}

	public String getLevel() {
		return level;
	}

	public String getRound() {
		return round;
	}

	public int getRoundFactor() {
		return roundFactor;
	}
}
