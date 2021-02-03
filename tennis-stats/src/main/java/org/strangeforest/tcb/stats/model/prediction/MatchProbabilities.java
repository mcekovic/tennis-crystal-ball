package org.strangeforest.tcb.stats.model.prediction;

public class MatchProbabilities extends SetProbabilities {

	public static final MatchProbabilities WON = new MatchProbabilities(SetProbabilities.SET_WON, 1.0);
	public static final MatchProbabilities LOST = new MatchProbabilities(SetProbabilities.SET_LOST, 0.0);

	private final double pMatch;

	public MatchProbabilities(SetProbabilities pSet, double pMatch) {
		super(pSet.getPGame(), pSet.getPSet());
		this.pMatch = pMatch;
	}

	public double getPMatch() {
		return pMatch;
	}
}
