package org.strangeforest.tcb.stats.model;

public class TournamentGOATPointsRow {

	private final String level;
	private final String result;
	private final int goatPoints;
	private final boolean additive;

	public TournamentGOATPointsRow(String level, String result, int goatPoints, boolean additive) {
		this.level = level;
		this.result = result;
		this.goatPoints = goatPoints;
		this.additive = additive;
	}

	public String getLevel() {
		return level;
	}

	public String getResult() {
		return result;
	}

	public int getGoatPoints() {
		return goatPoints;
	}

	public boolean isAdditive() {
		return additive;
	}
}
