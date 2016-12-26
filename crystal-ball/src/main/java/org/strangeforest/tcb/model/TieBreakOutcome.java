package org.strangeforest.tcb.model;

class TieBreakOutcome extends DiffOutcome {

	TieBreakOutcome(double pServe, double pReturn) {
		super(7, 2, pointNo -> (pointNo / 2) % 2 == 0 ? pServe : pReturn);
	}
}
