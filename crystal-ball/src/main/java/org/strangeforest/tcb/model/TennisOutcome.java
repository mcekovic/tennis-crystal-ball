package org.strangeforest.tcb.model;

import static com.google.common.base.Preconditions.*;

public abstract class TennisOutcome {

	private final double pServe;

	public TennisOutcome(double pServe) {
		this.pServe = pServe;
		checkArgument(pServe >= 0.0, "pServe should be greater or equal to zero.");
		checkArgument(pServe <= 1.0, "pServe should be less or equal to one.");
	}

	public double pServeWin() {
		return pServeWin(0, 0);
	}

	private double pServeWin(int servePoints, int returnPoints) {
		if (servePoints == 3 && returnPoints < 3)
			return  pServe;
		else if (returnPoints == 3 && servePoints < 3)
			return 1 - pServe;
		else if (returnPoints == 3 && servePoints == 3)
			return pServe * pServeWin(3, 2) + (1 - pServe) * pServeWin(2, 3);
		else
			return pServe * pServeWin(servePoints + 1, returnPoints) + (1 - pServe) * pServeWin(servePoints, returnPoints + 1);
	}
}
