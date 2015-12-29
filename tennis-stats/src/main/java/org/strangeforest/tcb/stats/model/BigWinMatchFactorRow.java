package org.strangeforest.tcb.stats.model;

public class BigWinMatchFactorRow {

	private final String level;
	private final String round;
	private final int matchFactor;

	public BigWinMatchFactorRow(String level, String round, int matchFactor) {
		this.level = level;
		this.round = round;
		this.matchFactor = matchFactor;
	}

	public String getLevel() {
		return level;
	}

	public String getRound() {
		return round;
	}

	public int getMatchFactor() {
		return matchFactor;
	}
}
