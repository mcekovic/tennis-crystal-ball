package org.strangeforest.tcb.model;

public class MatchOutcome extends DiffOutcome {

	MatchOutcome(double pServe, double pReturn, int bestOf) {
		this(pServe, pReturn, bestOf, finalSetTieBreaker(bestOf));
	}

	MatchOutcome(double pServe, double pReturn, int bestOf, boolean finalSetTieBreaker) {
		super(maxSets(bestOf), 1, setNo -> new SetOutcome(pServe, pReturn, finalSetTieBreaker || setNo < maxSets(bestOf)).pWin());
	}

	private static int maxSets(int bestOf) {
		switch (bestOf) {
			case 3: return 2;
			case 5: return 3;
			default: throw new IllegalStateException();
		}
	}

	private static boolean finalSetTieBreaker(int bestOf) {
		switch (bestOf) {
			case 3: return true;
			case 5: return false;
			default: throw new IllegalStateException();
		}
	}
}
