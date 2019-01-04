package org.strangeforest.tcb.stats.model.prediction;

import org.strangeforest.tcb.stats.model.core.*;

import static org.strangeforest.tcb.stats.model.core.GameRules.*;

public class GameOutcome extends DiffOutcome {

	private final double pServe;

	public GameOutcome(double pServe) {
		this(pServe, COMMON_GAME);
	}

	public GameOutcome(double pServe, GameRules rules) {
		super(rules.getPoints(), rules.getPointsDiff());
		this.pServe = pServe;
	}

	@Override protected double pItemWin(int point) {
		return pServe;
	}
}
