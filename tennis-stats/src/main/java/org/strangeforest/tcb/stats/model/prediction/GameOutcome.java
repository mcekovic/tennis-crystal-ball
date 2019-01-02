package org.strangeforest.tcb.stats.model.prediction;

import org.strangeforest.tcb.stats.model.core.*;

import static org.strangeforest.tcb.stats.model.core.GameRules.*;

public class GameOutcome extends DiffOutcome {

	public GameOutcome(double pServe) {
		this(pServe, COMMON_GAME);
	}

	public GameOutcome(double pServe, GameRules rules) {
		super(rules.getPoints(), rules.getPointsDiff(), i -> pServe);
	}
}
