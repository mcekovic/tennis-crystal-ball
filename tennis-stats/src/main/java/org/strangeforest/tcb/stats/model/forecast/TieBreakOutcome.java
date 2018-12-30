package org.strangeforest.tcb.stats.model.forecast;

public class TieBreakOutcome extends DiffOutcome {

	public TieBreakOutcome(double pServe, double pReturn) {
		this(pServe, pReturn, 7);
	}

	public TieBreakOutcome(double pServe, double pReturn, int points) {
		super(points, 2, i -> i / 2 % 2 == 0 ? pServe : pReturn);
	}
}
