package org.strangeforest.tcb.stats.model.prediction;

import java.util.stream.*;

public enum PredictionArea implements Weighted {

	RANKING(RankingPredictionItem.class, 10.0),
	RECENT_FORM(RecentFormPredictionItem.class, 2.0),
	H2H(H2HPredictionItem.class, 2.0),
	WINNING_PCT(WinningPctPredictionItem.class, 3.0);

	private final Class<? extends PredictionItem> itemClass;
	private volatile double weight;
	private volatile double itemAdjustmentWeight;

	PredictionArea(Class<? extends PredictionItem> itemClass, double weight) {
		this.itemClass = itemClass;
		for (PredictionItem item : itemClass.getEnumConstants())
			item.setArea(this);
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
		calculateItemAdjustmentWeight();
	}

	public void setWeights(double weight) {
		setWeights(weight, weight);
	}

	public void setWeights(double areaWeight, double itemWeight) {
		setWeight(areaWeight);
		for (PredictionItem item : getItems())
			item.setWeight(itemWeight);
	}

	void calculateItemAdjustmentWeight() {
		double itemWeights = Stream.of(getItems()).mapToDouble(PredictionItem::getWeight).sum();
		itemAdjustmentWeight = itemWeights > 0.0 ? weight / itemWeights : 0.0;
	}

	public double getItemAdjustmentWeight() {
		return itemAdjustmentWeight;
	}

	public boolean isEnabled() {
		return weight > 0.0 && itemAdjustmentWeight > 0.0;
	}

	public static boolean isAnyEnabled() {
		for (PredictionArea area : values()) {
			if (area.isEnabled())
				return true;
		}
		return false;
	}

	public static double getTotalAreaWeight() {
		return Stream.of(PredictionArea.values()).mapToDouble(PredictionArea::getWeight).sum();
	}
}
