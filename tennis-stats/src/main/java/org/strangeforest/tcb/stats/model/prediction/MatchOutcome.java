package org.strangeforest.tcb.stats.model.prediction;

import org.strangeforest.tcb.stats.model.core.*;

public class MatchOutcome extends DiffOutcome {

	private final double pServe;
	private final double pReturn;
	private final MatchRules rules;

	public MatchOutcome(double pServe, double pReturn, MatchRules rules) {
		super(rules.getSets(), 1, i -> new SetOutcome(pServe, pReturn, rules.getSet()).pWin());
		this.pServe = pServe;
		this.pReturn = pReturn;
		this.rules = rules;
	}

	@Override protected double pDeuce(double p1, double p2, int items1, int items2) {
		if (rules.isFinalSet(items1, items2))
			return new SetOutcome(pServe, pReturn, rules.getFinalSet()).pWin();
		else
			return super.pDeuce(p1, p2, items1, items2);
	}
}
