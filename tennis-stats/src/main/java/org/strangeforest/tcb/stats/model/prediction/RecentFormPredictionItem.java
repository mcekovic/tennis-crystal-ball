package org.strangeforest.tcb.stats.model.prediction;

public enum RecentFormPredictionItem implements PredictionItem {

	OVERALL(5.0),
	SURFACE(6.0),
	LEVEL(9.0),
	ROUND(0.0),
	VS_RANK(0.0),
	VS_HAND(1.0),
	VS_BACKHAND(0.0);

	private volatile PredictionArea area;
	private volatile double weight;

	RecentFormPredictionItem(double weight) {
		this.weight = weight;
	}

	@Override public PredictionArea getArea() {
		return area;
	}

	@Override public void setArea(PredictionArea area) {
		this.area = area;
	}

	@Override public boolean isForSet() {
		return false;
	}

	@Override public double getWeight() {
		return weight;
	}

	@Override public void setWeight(double weight) {
		this.weight = weight;
		area.calculateItemAdjustmentWeight();
	}

	@Override public String toString() {
		return area + "[" + super.toString() + "]";
	}
}
