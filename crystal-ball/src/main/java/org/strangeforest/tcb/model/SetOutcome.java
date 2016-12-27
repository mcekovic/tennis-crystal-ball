package org.strangeforest.tcb.model;

public class SetOutcome extends DiffOutcome {

	private final double pServe;
	private final double pReturn;
	private final boolean tieBreak;

	public SetOutcome(double pServe, double pReturn) {
		this(pServe, pReturn, true);
	}

	public SetOutcome(double pServe, double pReturn, boolean tieBreak) {
		super(6, 2, gameNo -> gameNo % 2 == 0 ? new GameOutcome(pServe).pWin() : new GameOutcome(pReturn).pWin());
		this.pServe = pServe;
		this.pReturn = pReturn;
		this.tieBreak = tieBreak;
	}

	@Override protected double pDeuce(double p1, double p2) {
		if (tieBreak)
			return new TieBreakOutcome(pServe, pReturn).pWin();
		else
			return super.pDeuce(p1, p2);
	}
}