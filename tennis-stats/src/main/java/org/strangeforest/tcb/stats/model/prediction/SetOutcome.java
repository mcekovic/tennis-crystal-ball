package org.strangeforest.tcb.stats.model.prediction;

import java.util.function.*;

import org.strangeforest.tcb.stats.model.core.*;
import org.strangeforest.tcb.util.*;

import static org.strangeforest.tcb.stats.model.core.SetRules.*;

public class SetOutcome extends DiffOutcome {

	private final double pServe;
	private final double pReturn;
	private final SetRules rules;
	private final Supplier<Double> pServeWin = Memoizer.of(this::pServeWin);
	private final Supplier<Double> pReturnWin = Memoizer.of(this::pReturnWin);

	public SetOutcome(double pServe, double pReturn) {
		this(pServe, pReturn, COMMON_SET);
	}

	public SetOutcome(double pServe, double pReturn, SetRules rules) {
		super(rules.getGames(), rules.getGamesDiff());
		this.pServe = pServe;
		this.pReturn = pReturn;
		this.rules = rules;
	}

	@Override protected double pItemWin(int game) {
		return game % 2 == 0 ? pServeWin.get() : pReturnWin.get();
	}

	private double pServeWin() {
		return getGameOutcome(pServe).pWin();
	}

	private double pReturnWin() {
		return getGameOutcome(pReturn).pWin();
	}

	@Override protected double pDeuce(double p1, double p2, int games1, int games2) {
		if (rules.isTieBreak(games1, games2))
			return new TieBreakOutcome(pServe, pReturn, rules.getTieBreak()).pWin();
		else
			return super.pDeuce(p1, p2, games1, games2);
	}

	public double pWin(int games1, int games2, int points1, int points2) {
		if (rules.isTieBreak(games1, games2))
			return new TieBreakOutcome(pServe, pReturn, rules.getTieBreak()).pWin(points1, points2);
		else {
			int game = games1 + games2 + 1;
			double pGameWin = getGameOutcome(game % 2 == 0 ? pServe : pReturn).pWin(points1, points2);
			return pGameWin * pWin(games1 + 1, games2) + (1.0 - pGameWin) * pWin(games1, games2 + 1);
		}
	}

	private GameOutcome getGameOutcome(double pGame) {
		return new GameOutcome(pGame, rules.getGame());
	}
}
