package org.strangeforest.tcb.stats.model.core;

public class GameRules extends BaseGameRules {

	public static final GameRules COMMON_GAME = new GameRules(4, 2);

	public GameRules(int points, int pointsDiff) {
		super(points, pointsDiff);
	}
}
