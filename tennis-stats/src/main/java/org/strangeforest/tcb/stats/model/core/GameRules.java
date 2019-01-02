package org.strangeforest.tcb.stats.model.core;

public class GameRules {

	public static final GameRules COMMON_GAME = new GameRules(4, 2);

	private final int points;
	private final int pointsDiff;

	public GameRules(int points, int pointsDiff) {
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
