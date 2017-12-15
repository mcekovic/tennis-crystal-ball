package org.strangeforest.tcb.stats.model;

import static org.strangeforest.tcb.stats.model.core.TournamentLevel.*;

public class TournamentGOATPoints {

	private final String level;
	private final String result;
	private int goatPoints;
	private final boolean additive;

	public TournamentGOATPoints(String level, String result, int goatPoints, boolean additive) {
		this.level = level;
		this.result = result;
		this.goatPoints = goatPoints;
		this.additive = additive;
	}

	public String getLevel() {
		return level;
	}

	public String getResult() {
		return mapResult(level, result);
	}

	public int getGoatPoints() {
		return goatPoints;
	}

	public boolean isAdditive() {
		return additive;
	}

	public void applyConfig(GOATListConfig config) {
		goatPoints *= config.getLevelTotalFactor(level) * config.getResultTotalFactor(result);
	}
}
