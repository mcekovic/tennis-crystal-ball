package org.strangeforest.tcb.stats.model.prediction;

public class GameOutcome extends DiffOutcome {

	public GameOutcome(double pServe) {
		super(4, 2, i -> pServe);
	}
}
