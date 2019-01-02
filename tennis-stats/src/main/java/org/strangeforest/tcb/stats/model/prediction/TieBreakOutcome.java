package org.strangeforest.tcb.stats.model.prediction;

import org.strangeforest.tcb.stats.model.core.*;

import static org.strangeforest.tcb.stats.model.core.TieBreakRules.*;

public class TieBreakOutcome extends DiffOutcome {

	public TieBreakOutcome(double pServe, double pReturn) {
		this(pServe, pReturn, COMMON_TIE_BREAK);
	}

	public TieBreakOutcome(double pServe, double pReturn, TieBreakRules rules) {
		super(rules.getPoints(), rules.getPointsDiff(), i -> i / 2 % 2 == 0 ? pServe : pReturn);
	}
}
