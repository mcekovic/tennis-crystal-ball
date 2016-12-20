package org.strangeforest.tcb.stats.model.prediction;

import java.util.*;

import static java.util.stream.Collectors.*;

public final class MatchPrediction {

	private final List<WeightedProbability> itemProbabilities1 = new ArrayList<>();
	private final List<WeightedProbability> itemProbabilities2 = new ArrayList<>();
	private Double winProbability1 = 0.5;
	private Double winProbability2 = 0.5;

	private double weightAverage(List<WeightedProbability> weightedProbabilities) {
		double weightProbabilitySum = 0.0;
		double weightSum = 0.0;
		for (WeightedProbability itemProbability : weightedProbabilities) {
			double weight = itemProbability.getWeight();
			weightProbabilitySum += weight * itemProbability.getProbability();
			weightSum += weight;
		}
		return weightProbabilitySum / weightSum;
	}

	public double getWinProbability1() {
		if (winProbability1 == null)
			winProbability1 = weightAverage(itemProbabilities1);
		return winProbability1;
	}

	public double getWinProbability2() {
		if (winProbability2 == null)
			winProbability2 = weightAverage(itemProbabilities2);
		return winProbability2;
	}

	public List<WeightedProbability> getItemProbabilities1() {
		return itemProbabilities1;
	}

	public List<WeightedProbability> getItemProbabilities2() {
		return itemProbabilities2;
	}

	public double getItemProbabilitiesWeight1() {
		return itemProbabilities1.stream().mapToDouble(WeightedProbability::getWeight).sum();
	}

	public double getItemProbabilitiesWeight2() {
		return itemProbabilities2.stream().mapToDouble(WeightedProbability::getWeight).sum();
	}

	public boolean isEmpty() {
		return itemProbabilities1.isEmpty() && itemProbabilities2.isEmpty();
	}

	public void addItemProbability1(PredictionArea area, PredictionItem item, double weight, double probability) {
		if (weight > 0.0) {
			itemProbabilities1.add(new WeightedProbability(area, item, weight, probability));
			winProbability1 = null;
		}
	}

	public void addItemProbability2(PredictionArea area, PredictionItem item, double weight, double probability) {
		if (weight > 0.0) {
			itemProbabilities2.add(new WeightedProbability(area, item, weight, probability));
			winProbability2 = null;
		}
	}

	public void addAreaProbabilities(PredictionArea area, MatchPrediction prediction) {
		if (!prediction.isEmpty() && area.getWeight() > 0.0) {
			double weight = area.itemAdjustmentWeight();
			if (weight > 0.0) {
				itemProbabilities1.addAll(prediction.getItemProbabilities1(weight));
				itemProbabilities2.addAll(prediction.getItemProbabilities2(weight));
				winProbability1 = null;
				winProbability2 = null;
			}
		}
	}

	private List<WeightedProbability> getItemProbabilities1(double weight) {
		return itemProbabilities1.stream().map(p -> p.weighted(weight)).collect(toList());
	}

	private List<WeightedProbability> getItemProbabilities2(double weight) {
		return itemProbabilities2.stream().map(p -> p.weighted(weight)).collect(toList());
	}
}
