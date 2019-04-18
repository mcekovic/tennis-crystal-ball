package org.strangeforest.tcb.stats.model.prediction;

import org.strangeforest.tcb.stats.model.core.*;

import static org.strangeforest.tcb.stats.util.PercentageUtil.*;

public abstract class InMatchPredictor {

	private static final double P_OFFSET = 0.0001;
	private static final double STEP = 0.05;

	public static BaseProbabilities normalize(double pWin, BaseProbabilities probabilities, MatchRules rules) {
		double pServe = probabilities.getPServe();
		double pReturn = probabilities.getPReturn();
		while (true) {
			double currentPWin = new MatchOutcome(pServe, pReturn, rules).pWin();
			if (Math.abs(currentPWin - pWin) < P_OFFSET)
				return new BaseProbabilities(pServe, pReturn);
			double relDiff = pctDiff(PCT * currentPWin, PCT * pWin) / PCT;
			pServe -= relDiff * pServe * STEP;
			pReturn -= relDiff * pReturn * STEP;
		}
	}
}
