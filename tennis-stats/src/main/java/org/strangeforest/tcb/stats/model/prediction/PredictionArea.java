package org.strangeforest.tcb.stats.model.prediction;

import java.util.stream.*;

public enum PredictionArea implements PredictionItem {

	ELO(1.0, EloPredictionItem.class),
	H2H(1.0, MatchPredictionItem.class),
	WINNING_PCT(1.0, MatchPredictionItem.class);

	private final double weight;
	private final Class<? extends PredictionItem> itemClass;
	private final double itemAdjustmentWeight;

	PredictionArea(double weight, Class<? extends PredictionItem> itemClass) {
		this.weight = weight;
		this.itemClass = itemClass;
		itemAdjustmentWeight = calculateItemAdjustmentWeight();
	}

	@Override public double weight() {
		return weight;
	}

	public double itemAdjustmentWeight() {
		return itemAdjustmentWeight;
	}

	private double calculateItemAdjustmentWeight() {
		return weight / Stream.of(itemClass.getEnumConstants()).mapToDouble(PredictionItem::weight).sum();
	}
}
