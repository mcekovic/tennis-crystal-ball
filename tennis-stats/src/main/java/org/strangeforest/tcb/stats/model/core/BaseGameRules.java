package org.strangeforest.tcb.stats.model.core;

public abstract class BaseGameRules {

	private final int points;
	private final int pointsDiff;

	protected BaseGameRules(int points, int pointsDiff) {
		this.points = points;
		this.pointsDiff = pointsDiff;
	}

	public int getPoints() {
		return points;
	}

	public int getPointsDiff() {
		return pointsDiff;
	}
}
