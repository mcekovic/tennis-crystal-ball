package org.strangeforest.tcb.model;

public class GameOutcome extends DiffOutcome {

	public GameOutcome(double pServe) {
		super(4, 2, pointNo -> pServe);
	}
}
