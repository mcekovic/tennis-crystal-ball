package org.strangeforest.tcb.stats.model.forecast;

public class GameOutcome extends DiffOutcome {

	public GameOutcome(double pServe) {
		super(4, 2, i -> pServe);
	}
}
