package org.strangeforest.tcb.stats.model.forecast;

import org.strangeforest.tcb.stats.model.core.*;

public class SetOutcome extends DiffOutcome {

	private final double pServe;
	private final double pReturn;
	private final TieBreakRules tieBreakRules;

	public SetOutcome(double pServe, double pReturn) {
		this(pServe, pReturn, TieBreakRules.COMMON_TIE_BREAK);
	}

	public SetOutcome(double pServe, double pReturn, TieBreakRules tieBreakRules) {
		this(pServe, pReturn, 6, tieBreakRules);
	}

	public SetOutcome(double pServe, double pReturn, int games, TieBreakRules tieBreakRules) {
		super(games, 2, i -> i % 2 == 0 ? new GameOutcome(pServe).pWin() : new GameOutcome(pReturn).pWin());
		this.pServe = pServe;
		this.pReturn = pReturn;
		this.tieBreakRules = tieBreakRules;
	}

	@Override protected double pDeuce(double p1, double p2, int items1, int items2) {
		if (items1 == items2 && tieBreakRules.isTieBreakFor(items1))
			return new TieBreakOutcome(pServe, pReturn, tieBreakRules.getTieBreakPoints()).pWin();
		else
			return super.pDeuce(p1, p2, items1, items2);
	}
}
