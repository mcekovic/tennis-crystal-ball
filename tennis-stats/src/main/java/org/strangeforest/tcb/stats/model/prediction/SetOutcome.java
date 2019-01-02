package org.strangeforest.tcb.stats.model.prediction;

import org.strangeforest.tcb.stats.model.core.*;

import static org.strangeforest.tcb.stats.model.core.SetRules.*;

public class SetOutcome extends DiffOutcome {

	private final double pServe;
	private final double pReturn;
	private final SetRules rules;

	public SetOutcome(double pServe, double pReturn) {
		this(pServe, pReturn, COMMON_SET);
	}

	public SetOutcome(double pServe, double pReturn, SetRules rules) {
		super(rules.getGames(), rules.getGamesDiff(), i -> i % 2 == 0 ? new GameOutcome(pServe).pWin() : new GameOutcome(pReturn).pWin());
		this.pServe = pServe;
		this.pReturn = pReturn;
		this.rules = rules;
	}

	@Override protected double pDeuce(double p1, double p2, int items1, int items2) {
		if (rules.isTieBreak(items1, items2))
			return new TieBreakOutcome(pServe, pReturn, rules.getTieBreak()).pWin();
		else
			return super.pDeuce(p1, p2, items1, items2);
	}
}
