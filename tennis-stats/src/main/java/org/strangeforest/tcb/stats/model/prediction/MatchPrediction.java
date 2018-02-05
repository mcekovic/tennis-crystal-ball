package org.strangeforest.tcb.stats.model.prediction;

import java.util.*;
import java.util.stream.*;

import org.strangeforest.tcb.stats.model.price.*;

import static java.util.function.Function.*;
import static java.util.stream.Collectors.*;
import static org.strangeforest.tcb.stats.model.price.PriceUtil.*;

public final class MatchPrediction {

	// Factory

	public static final MatchPrediction TIE = prediction(0.5);

	private static MatchPrediction prediction(double winProbability1) {
		MatchPrediction prediction = new MatchPrediction(1.0);
		prediction.addItemProbability1(RankingPredictionItem.RANK, 1.0, winProbability1);
		prediction.addItemProbability2(RankingPredictionItem.RANK, 1.0, 1.0 - winProbability1);
		return prediction;
	}


	// Instance

	private final double totalAreaWeight;
	private List<WeightedProbability> itemProbabilities1 = new ArrayList<>();
	private List<WeightedProbability> itemProbabilities2 = new ArrayList<>();
	private Double winProbability1 = 0.5;
	private Double winProbability2 = 0.5;
	private RankingData rankingData1;
	private RankingData rankingData2;

	public MatchPrediction(double totalAreaWeight) {
		this.totalAreaWeight = totalAreaWeight;
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

	public Map<PriceFormat, String> getWinPrices1() {
		return getPriceMap(getWinProbability1());
	}

	public Map<PriceFormat, String> getWinPrices2() {
		return getPriceMap(getWinProbability2());
	}

	private Map<PriceFormat, String> getPriceMap(double probability) {
		return Stream.of(PriceFormat.values()).collect(toMap(identity(), format -> format.format(toPrice(probability))));
	}

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

	public List<PredictionItem> getItems() {
		return itemProbabilities1.stream().map(WeightedProbability::getItem).collect(toList());
	}

	public List<WeightedProbability> getItemProbabilities1() {
		return itemProbabilities1;
	}

	public List<WeightedProbability> getItemProbabilities2() {
		return itemProbabilities2;
	}

	public WeightedProbability getItemProbability1(PredictionItem item) {
		return getWeightedProbability(itemProbabilities1, item);
	}

	public WeightedProbability getItemProbability2(PredictionItem item) {
		return getWeightedProbability(itemProbabilities2, item);
	}

	private static WeightedProbability getWeightedProbability(List<WeightedProbability> itemProbabilities, PredictionItem item) {
		return itemProbabilities.stream().filter(p -> p.getItem() == item).findFirst().orElse(new WeightedProbability(item, 0.0, 0.0));
	}

	public double getItemProbabilitiesWeight1() {
		return getItemProbabilitiesWeight(itemProbabilities1);
	}

	public double getItemProbabilitiesWeight2() {
		return getItemProbabilitiesWeight(itemProbabilities2);
	}

	private static double getItemProbabilitiesWeight(List<WeightedProbability> itemProbabilities) {
		return itemProbabilities.stream().mapToDouble(WeightedProbability::getWeight).sum();
	}

	public double getPredictability1() {
		return getItemProbabilitiesWeight1() / totalAreaWeight;
	}

	public double getPredictability2() {
		return getItemProbabilitiesWeight2() / totalAreaWeight;
	}

	public boolean isEmpty() {
		return itemProbabilities1.isEmpty() && itemProbabilities2.isEmpty();
	}

	public void addItemProbability1(PredictionItem item, double weight, double probability) {
		if (weight > 0.0) {
			itemProbabilities1.add(new WeightedProbability(item, weight, probability));
			winProbability1 = null;
		}
	}

	public void addItemProbability2(PredictionItem item, double weight, double probability) {
		if (weight > 0.0) {
			itemProbabilities2.add(new WeightedProbability(item, weight, probability));
			winProbability2 = null;
		}
	}

	public void addAreaProbabilities(MatchPrediction prediction, double areaAdjustedWeight) {
		if (areaAdjustedWeight > 0.0) {
			itemProbabilities1.addAll(prediction.getItemProbabilities1(areaAdjustedWeight));
			itemProbabilities2.addAll(prediction.getItemProbabilities2(areaAdjustedWeight));
			winProbability1 = null;
			winProbability2 = null;
		}
	}

	private List<WeightedProbability> getItemProbabilities1(double weight) {
		return getItemProbabilities(itemProbabilities1, weight);
	}

	private List<WeightedProbability> getItemProbabilities2(double weight) {
		return getItemProbabilities(itemProbabilities2, weight);
	}

	private static List<WeightedProbability> getItemProbabilities(List<WeightedProbability> itemProbabilities, double weight) {
		return itemProbabilities.stream().map(p -> p.weighted(weight)).collect(toList());
	}

	public RankingData getRankingData1() {
		return rankingData1;
	}

	public void setRankingData1(RankingData rankingData1) {
		this.rankingData1 = rankingData1;
	}

	public RankingData getRankingData2() {
		return rankingData2;
	}

	public void setRankingData2(RankingData rankingData2) {
		this.rankingData2 = rankingData2;
	}

	public MatchPrediction swap() {
		MatchPrediction swapped = new MatchPrediction(totalAreaWeight);
		swapped.itemProbabilities1 = new ArrayList<>(itemProbabilities2);
		swapped.itemProbabilities2 = new ArrayList<>(itemProbabilities1);
		swapped.winProbability1 = winProbability2;
		swapped.winProbability2 = winProbability1;
		swapped.rankingData1 = rankingData2;
		swapped.rankingData2 = rankingData1;
		return swapped;
	}
}
