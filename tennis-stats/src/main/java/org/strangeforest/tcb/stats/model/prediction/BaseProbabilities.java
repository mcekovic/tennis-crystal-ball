package org.strangeforest.tcb.stats.model.prediction;

public class BaseProbabilities {

	public static final BaseProbabilities DEFAULT = new BaseProbabilities(0.63, 0.37);
	public static final BaseProbabilities UNKNOWN = new BaseProbabilities(-1.0, -1.0);

	private final double pServe;
	private final double pReturn;

	public BaseProbabilities(double pServe, double pReturn) {
		this.pServe = pServe;
		this.pReturn = pReturn;
	}

	public double getPServe() {
		return pServe;
	}

	public double getPReturn() {
		return pReturn;
	}

	public boolean isUnknown() {
		return pServe < 0.0;
	}

	public BaseProbabilities swap() {
		return isUnknown() ? this : new BaseProbabilities(1.0 - pReturn, 1.0 - pServe);
	}

	public BaseProbabilities combine(BaseProbabilities baseProbs) {
		if (isUnknown())
			return baseProbs.isUnknown() ? UNKNOWN : baseProbs;
		else
			return baseProbs.isUnknown() ? this : new BaseProbabilities((pServe + baseProbs.pServe) / 2.0, (pReturn + baseProbs.pReturn) / 2.0);
	}

	public BaseProbabilities defaultIfUnknown() {
		return isUnknown() ? DEFAULT : this;
	}

	@Override public String toString() {
		return "[pServe=" + pServe + ", pReturn=" + pReturn + ']';
	}
}
