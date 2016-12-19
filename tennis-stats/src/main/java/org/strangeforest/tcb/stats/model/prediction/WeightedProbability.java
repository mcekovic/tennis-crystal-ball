package org.strangeforest.tcb.stats.model.prediction;

import static com.google.common.base.MoreObjects.*;

public class WeightedProbability {

	private final PredictionArea area;
	private final PredictionItem item;
	private final double weight;
	private final double probability;

	public WeightedProbability(PredictionArea area, PredictionItem item, double weight, double probability) {
		this.area = area;
		this.item = item;
		this.weight = weight;
		this.probability = probability;
	}

	public PredictionArea getArea() {
		return area;
	}

	public PredictionItem getItem() {
		return item;
	}

	public double getWeight() {
		return weight;
	}

	public double getProbability() {
		return probability;
	}

	public WeightedProbability weighted(double weight) {
		return new WeightedProbability(area, item, this.weight * weight, probability);
	}

	@Override public String toString() {
		return toStringHelper(this)
	       .add("area", area)
	       .add("item", item)
	       .add("weight", weight)
	       .add("probability", probability)
	       .toString();
	}
}
