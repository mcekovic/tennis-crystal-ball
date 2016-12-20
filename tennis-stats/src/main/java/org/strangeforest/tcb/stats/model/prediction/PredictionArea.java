package org.strangeforest.tcb.stats.model.prediction;

import java.util.stream.*;

public enum PredictionArea implements Weighted {

	ELO(EloPredictionItem.class, 1.0),
	H2H(H2HPredictionItem.class, 1.0),
	WINNING_PCT(WinningPctPredictionItem.class, 1.0);

	private final Class<? extends PredictionItem> itemClass;
	private volatile double weight;
	private volatile double itemAdjustmentWeight;

	PredictionArea(Class<? extends PredictionItem> itemClass, double weight) {
		this.itemClass = itemClass;
		setWeight(weight);
	}

	public PredictionItem[] getItems() {
		return itemClass.getEnumConstants();
	}

	@Override public double getWeight() {
		return weight;
	}

	@Override public void setWeight(double weight) {
		this.weight = weight;
		itemAdjustmentWeight = weight / Stream.of(getItems()).mapToDouble(PredictionItem::getWeight).sum();
	}

	public double itemAdjustmentWeight() {
		return itemAdjustmentWeight;
	}

}
