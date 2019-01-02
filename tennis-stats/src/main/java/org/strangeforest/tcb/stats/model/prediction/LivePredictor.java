package org.strangeforest.tcb.stats.model.prediction;

import org.strangeforest.tcb.stats.model.core.*;

import static org.strangeforest.tcb.stats.util.PercentageUtil.*;

public abstract class LivePredictor {

	private static final double P_OFFSET = 0.0001;
	private static final double STEP = 0.05;

	public static BaseProbabilities normalize(BaseProbabilities probabilities, MatchRules rules) {
		double pWin = probabilities.getpWin();
		double pServe = probabilities.getpServe();
		double pReturn = probabilities.getpReturn();
		while (true) {
			double currentPWin = new MatchOutcome(pServe, pReturn, rules).pWin();
			if (Math.abs(currentPWin - pWin) < P_OFFSET)
				return new BaseProbabilities(pWin, pServe, pReturn);
			double relDiff = pctDiff(PCT * currentPWin, PCT * pWin) / PCT;
			pServe -= relDiff * pServe * STEP;
			pReturn -= relDiff * pReturn * STEP;
		}
	}
}
