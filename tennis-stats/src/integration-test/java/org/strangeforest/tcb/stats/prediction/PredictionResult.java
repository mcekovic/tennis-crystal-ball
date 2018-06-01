package org.strangeforest.tcb.stats.prediction;

import org.strangeforest.tcb.stats.model.prediction.*;

import static com.google.common.base.MoreObjects.*;
import static java.lang.Math.*;
import static java.lang.String.*;
import static org.strangeforest.tcb.stats.util.PercentageUtil.*;

public class PredictionResult {

	private final PredictionConfig config;

	private int total;
	private int predictable;
	private int predicted;
	private double p;
	private double pDelta2;
	private double pLog;
	private int withPrice;
	private int beatingPrice;
	private int profitable;
	private double stake;
	private double return_;

	private double predictablePct;
	private double predictionRate;
	private double brier;
	private double logLoss;
	private double score;
	private double calibration;
	private double withPricePct;
	private double beatingPricePct;
	private double profitablePct;
	private double profit;
	private double profitPct;

	public PredictionResult(PredictionConfig config) {
		this.config = config;
	}

	public PredictionConfig getConfig() {
		return config;
	}

	public void newMatch(boolean predictable, double winnerProbability, boolean predicted, boolean withPrice, boolean beatingPrice, boolean profitable, double stake, double return_) {
		++total;
		if (predictable) { // Predictor is kicked on
			++this.predictable;
			if (predicted) // Prediction was correct
				++this.predicted;
			double loserProbability = 1 - winnerProbability;
			p += predicted ? winnerProbability : loserProbability;
			pLog += winnerProbability > 0.0 ? log(winnerProbability) : -2.0 * E;
			pDelta2 += loserProbability * loserProbability;
			if (withPrice) { // Match has valid bookmaker price to compare prediction to
				++this.withPrice;
				if (beatingPrice) { // Prediction is outside of price margin spread, thus a candidate for betting
					++this.beatingPrice;
					this.stake += stake;
					if (profitable) { // Betting produced a return
						++this.profitable;
						this.return_ += return_;
					}
				}
			}
		}
	}

	public void complete() {
		predictablePct = pct(predictable, total);
		predictionRate = pct(predicted, predictable);
		brier = pDelta2 / predictable;
		logLoss = -pLog / predictable;
		score = predicted / (predictable * logLoss); // = Prediction Rate / Log-Loss
		calibration = p / predicted;
		withPricePct = pct(withPrice, predictable);
		beatingPricePct = pct(beatingPrice, withPrice);
		profitablePct = pct(profitable, beatingPrice);
		profit = return_ - stake;
		profitPct = stake > 0.0 ? return_ / stake : 0.0;
	}

	public int getTotal() {
		return total;
	}

	public int getPredictable() {
		return predictable;
	}

	public int getPredicted() {
		return predicted;
	}

	public double getBrier() {
		return brier;
	}

	public double getScore() {
		return score;
	}

	public int getWithPrice() {
		return withPrice;
	}

	public int getBeatingPrice() {
		return beatingPrice;
	}

	public int getProfitable() {
		return profitable;
	}

	public double getStake() {
		return stake;
	}

	public double getReturn() {
		return return_;
	}

	public double getPredictablePct() {
		return predictablePct;
	}

	public double getPredictionRate() {
		return predictionRate;
	}

	public double getWithPricePct() {
		return withPricePct;
	}

	public double getBeatingPricePct() {
		return beatingPricePct;
	}

	public double getProfitablePct() {
		return profitablePct;
	}

	public double getProfit() {
		return profit;
	}

	public double getProfitPct() {
		return profitPct;
	}


	// Object methods

	@Override public String toString() {
		ToStringHelper builder = toStringHelper(this)
			.add("rate", format("%1$.3f%%", predictionRate))
			.add("predictable", format("%1$.3f%%", predictablePct))
			.add("brier", format("%1$.5f", brier))
			.add("logLoss", format("%1$.5f", logLoss))
			.add("score", format("%1$.5f", score))
			.add("calibration", format("%1$.5f", calibration));
		if (withPrice > 0) {
			builder.add("profit", format("%1$.3f%%", profitPct))
				.add("profitable", format("%1$.3f%%", profitablePct))
				.add("beatingPrice", format("%1$.3f%%", beatingPricePct))
				.add("withPrice", format("%1$.3f%%", withPricePct));
		}
		return builder.add("matches", total).toString();
	}
}
