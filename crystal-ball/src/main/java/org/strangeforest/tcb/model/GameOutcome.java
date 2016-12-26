package org.strangeforest.tcb.model;

class GameOutcome extends DiffOutcome {

	GameOutcome(double pServe) {
		super(4, 2, pointNo -> pServe);
	}
}
