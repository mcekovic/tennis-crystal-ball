package org.strangeforest.tcb.stats.model.prediction;

public class SetProbabilities {

	public static final SetProbabilities SET_WON = new SetProbabilities(1.0, 1.0);
	public static final SetProbabilities SET_LOST = new SetProbabilities(0.0, 0.0);

	private final double pGame;
	private final double pSet;

	public SetProbabilities(double pGame, double pSet) {
		this.pGame = pGame;
		this.pSet = pSet;
	}

	public double getPGame() {
		return pGame;
	}

	public double getPSet() {
		return pSet;
	}
}
