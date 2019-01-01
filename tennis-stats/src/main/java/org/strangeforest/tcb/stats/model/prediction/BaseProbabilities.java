package org.strangeforest.tcb.stats.model.prediction;

public class BaseProbabilities {

	private final double pWin;
	private final double pServe;
	private final double pReturn;

	public BaseProbabilities(double pWin, double pServe, double pReturn) {
		this.pWin = pWin;
		this.pServe = pServe;
		this.pReturn = pReturn;
	}

	public double getpWin() {
		return pWin;
	}

	public double getpServe() {
		return pServe;
	}

	public double getpReturn() {
		return pReturn;
	}

	@Override public String toString() {
		return "BaseProbabilities{" +
			"pWin=" + pWin +
			", pServe=" + pServe +
			", pReturn=" + pReturn +
		'}';
	}
}
