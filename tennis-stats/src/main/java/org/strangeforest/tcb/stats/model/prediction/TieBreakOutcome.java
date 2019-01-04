package org.strangeforest.tcb.stats.model.prediction;

import org.strangeforest.tcb.stats.model.core.*;

import static org.strangeforest.tcb.stats.model.core.TieBreakRules.*;

public class TieBreakOutcome extends DiffOutcome {

	private final double pServe;
	private final double pReturn;

	public TieBreakOutcome(double pServe, double pReturn) {
		this(pServe, pReturn, COMMON_TIE_BREAK);
	}

	public TieBreakOutcome(double pServe, double pReturn, TieBreakRules rules) {
		super(rules.getPoints(), rules.getPointsDiff());
		this.pServe = pServe;
		this.pReturn = pReturn;
	}

	@Override protected double pItemWin(int point) {
		return (point >> 1) % 2 == 0 ? pServe : pReturn;
	}
}
