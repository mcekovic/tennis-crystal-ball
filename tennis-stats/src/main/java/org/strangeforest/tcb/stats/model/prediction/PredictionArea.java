package org.strangeforest.tcb.stats.model.prediction;

import java.util.stream.*;

import static java.lang.String.*;

public enum PredictionArea implements Weighted {

	RANKING(RankingPredictionItem.class),
	RECENT_FORM(RecentFormPredictionItem.class),
	H2H(H2HPredictionItem.class),
	WINNING_PCT(WinningPctPredictionItem.class);

	private final Class<? extends PredictionItem> itemClass;

	PredictionArea(Class<? extends PredictionItem> itemClass) {
		this.itemClass = itemClass;
		for (PredictionItem item : itemClass.getEnumConstants())
			item.setArea(this);
	}

	public PredictionItem[] getItems() {
		return itemClass.getEnumConstants();
	}

	public PredictionItem getItem(String itemName) {
		return Stream.of(getItems()).filter(item -> item.name().equals(itemName)).findFirst().orElseThrow(
			() -> new IllegalArgumentException(format("Unknown prediction item %1$s for prediction area %2$s.", itemName, name()))
		);
	}

	@Override public double getWeight(PredictionConfig config) {
		return config.getAreaWeight(this);
	}

	@Override public PredictionConfig setWeight(PredictionConfig config, double weight) {
		return new PredictionConfig(config, this, weight);
	}
}
