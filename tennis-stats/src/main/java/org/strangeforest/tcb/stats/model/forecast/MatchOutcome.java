package org.strangeforest.tcb.stats.model.forecast;

import org.strangeforest.tcb.stats.model.core.*;

import static org.strangeforest.tcb.stats.model.core.TieBreakRules.*;

public class MatchOutcome extends DiffOutcome {

	private final double pServe;
	private final double pReturn;
	private final int sets;
	private final int setGames;
	private final TieBreakRules finalSetTieBreakRules;

	public MatchOutcome(double pServe, double pReturn, int sets) {
		this(pServe, pReturn, sets, COMMON_TIE_BREAK);
	}

	public MatchOutcome(double pServe, double pReturn, int sets, TieBreakRules tieBreakRules) {
		this(pServe, pReturn, sets, tieBreakRules, tieBreakRules);
	}

	public MatchOutcome(double pServe, double pReturn, int sets, TieBreakRules tieBreakRules, TieBreakRules finalSetTieBreakRules) {
		this(pServe, pReturn, sets, 6, tieBreakRules, finalSetTieBreakRules);
	}

	public MatchOutcome(double pServe, double pReturn, int sets, int setGames, TieBreakRules tieBreakRules, TieBreakRules finalSetTieBreakRules) {
		super(sets, 1, i -> new SetOutcome(pServe, pReturn, setGames, tieBreakRules).pWin());
		this.pServe = pServe;
		this.pReturn = pReturn;
		this.sets = sets;
		this.setGames = setGames;
		this.finalSetTieBreakRules = finalSetTieBreakRules;
	}

	@Override protected double pDeuce(double p1, double p2, int items1, int items2) {
		if (items1 == items2 && items1 == sets - 1)
			return new SetOutcome(pServe, pReturn, setGames, finalSetTieBreakRules).pWin();
		else
			return super.pDeuce(p1, p2, items1, items2);
	}
}
