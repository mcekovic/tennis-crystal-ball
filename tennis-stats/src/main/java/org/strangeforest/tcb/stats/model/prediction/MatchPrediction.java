package org.strangeforest.tcb.stats.model.prediction;

import java.util.*;
import java.util.stream.*;

import static java.util.function.Function.*;
import static java.util.stream.Collectors.*;
import static org.strangeforest.tcb.stats.model.prediction.PriceUtil.*;

public final class MatchPrediction {

	// Factory

	public static final MatchPrediction TIE = prediction(0.5);

	private static MatchPrediction prediction(double winProbability1) {
		MatchPrediction prediction = new MatchPrediction();
		prediction.addItemProbability1(PredictionArea.RANKING, RankingPredictionItem.RANK, 1.0, winProbability1);
		prediction.addItemProbability2(PredictionArea.RANKING, RankingPredictionItem.RANK, 1.0, 1.0 - winProbability1);
		return prediction;
	}


	// Instance

	private final List<WeightedProbability> itemProbabilities1 = new ArrayList<>();
	private final List<WeightedProbability> itemProbabilities2 = new ArrayList<>();
	private Double winProbability1 = 0.5;
	private Double winProbability2 = 0.5;
	private RankingData rankingData1;
	private RankingData rankingData2;

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

	public double getPredictability1() {
		return getItemProbabilitiesWeight1() / PredictionArea.getTotalAreaWeight();
	}

	public double getPredictability2() {
		return getItemProbabilitiesWeight2() / PredictionArea.getTotalAreaWeight();
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
			double weight = area.getItemAdjustmentWeight();
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
}
