package org.strangeforest.tcb.stats.model.prediction;

import org.strangeforest.tcb.stats.model.core.*;

import static org.strangeforest.tcb.stats.model.core.TieBreakRules.*;

public class TieBreakOutcome extends DiffOutcome {

	private final double pServe;
	private final double pReturn;
	private final TieBreakRules rules;

	public TieBreakOutcome(double pServe, double pReturn) {
		this(pServe, pReturn, COMMON_TIE_BREAK);
	}

	public TieBreakOutcome(double pServe, double pReturn, TieBreakRules rules) {
		super(rules.getPoints(), rules.getPointsDiff());
		this.pServe = pServe;
		this.pReturn = pReturn;
		this.rules = rules;
	}

	@Override protected double pItemWin(int point) {
		return isServe(point) ? pServe : pReturn;
	}

	public double pWin(int points1, int points2, boolean serve) {
		return isServe(points1 + points2 + 1) == serve ? pWin(points1, points2) : 1.0 - invertedOutcome().pWin(points2, points1);
	}

	private boolean isServe(int point) {
		return (point >> 1) % 2 == 0;
	}

	public TieBreakOutcome invertedOutcome() {
		return new TieBreakOutcome(1.0 - pReturn, 1.0 - pServe, rules);
	}
}
