package org.strangeforest.tcb.stats.model;

public class BigWinMatchFactor {

	private final String level;
	private final String round;
	private final int matchFactor;

	public BigWinMatchFactor(String level, String round, int matchFactor) {
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

	public BigWinMatchFactor applyFactor(int factor) {
		return factor == 1 ? this : new BigWinMatchFactor(level, round, matchFactor * factor);
	}
}
