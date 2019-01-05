package org.strangeforest.tcb.stats.model.prediction;

import java.util.function.*;

import org.strangeforest.tcb.stats.model.core.*;
import org.strangeforest.tcb.util.*;

public class MatchOutcome extends DiffOutcome {

	private final double pServe;
	private final double pReturn;
	private final MatchRules rules;
	private final Supplier<Double> pSetWin = Memoizer.of(this::pSetWin);

	public MatchOutcome(double pServe, double pReturn, MatchRules rules) {
		super(rules.getSets(), 1);
		this.pServe = pServe;
		this.pReturn = pReturn;
		this.rules = rules;
	}

	@Override protected double pItemWin(int set) {
		if (rules.isDecidingSet(set) && rules.hasDecidingSetSpecificRules())
			return getSetOutcome(rules.getDecidingSet()).pWin();
		else
			return pSetWin.get();
	}

	private double pSetWin() {
		return getSetOutcome(rules.getSet()).pWin();
	}

	@Override protected double pDeuce(double p1, double p2, int sets1, int sets2) {
		throw new IllegalStateException();
	}

	public double pWin(int sets1, int sets2, int games1, int games2, int points1, int points2) {
		if (rules.isDecidingSet(sets1 + sets2))
			return getSetOutcome(rules.getDecidingSet()).pWin(games1, games2, points1, points2);
		else {
			double pSetWin = getSetOutcome(rules.getSet()).pWin(games1, games2, points1, points2);
			return pSetWin * pWin(sets1 + 1, sets2) + (1.0 - pSetWin) * pWin(sets1, sets2 + 1);
		}
	}

	private SetOutcome getSetOutcome(SetRules set) {
		return new SetOutcome(pServe, pReturn, set);
	}
}
