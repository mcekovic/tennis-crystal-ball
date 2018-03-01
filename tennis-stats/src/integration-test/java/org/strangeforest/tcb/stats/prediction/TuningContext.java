package org.strangeforest.tcb.stats.prediction;

import java.util.*;

import org.strangeforest.tcb.stats.model.prediction.*;

import static org.strangeforest.tcb.stats.prediction.BasePredictionVerificationIT.*;

//TODO Automatically add equivalent config results
public class TuningContext {

	private final Comparator<PredictionResult> resultComparator;
	private final double minWeight;
	private final double maxWeight;
	private final double weightStep;
	private final Map<PredictionConfig, PredictionResult> results;
	private final PriorityQueue<PredictionResult> candidateNextResults;
	private PredictionResult bestResult;
	private PredictionResult baseStepResult;
	private PredictionResult bestStepResult;
	private int step;

	public TuningContext(Comparator<PredictionResult> resultComparator, double minWeight, double maxWeight, double weightStep) {
		this.resultComparator = resultComparator;
		this.minWeight = minWeight;
		this.maxWeight = maxWeight;
		this.weightStep = weightStep;
		results = new HashMap<>();
		candidateNextResults = new PriorityQueue<>(resultComparator.reversed());
	}

	public void initialResult(PredictionVerificationResult verificationResult) {
		PredictionResult result = verificationResult.getResult();
		addResult(result);
		bestResult = result;
		System.out.println("***** Initial result: " + bestResult);
		printWeights(result.getConfig(), false);
		printResultDistribution(verificationResult);
	}

	public void finish() {
		System.out.println("***** Best result: " + bestResult);
		printWeights(bestResult.getConfig(), false);
	}

	public PredictionResult startStep() {
		bestStepResult = null;
		step++;
		baseStepResult = candidateNextResults.poll();
		if (baseStepResult != null)
			System.out.println("*** Tuning step " + step + " starting: " + baseStepResult);
		return baseStepResult;
	}

	public PredictionResult endStep() {
		if (bestStepResult != null) 
			System.out.println("*** Tuning step " + step + " finished [results: " + results.size() + ", pending steps: " + candidateNextResults.size() + "]: " + bestStepResult);
		return bestStepResult;
	}

	public PredictionConfig stepUp(Weighted weighted) {
		return stepWeight(baseStepResult.getConfig(), weighted, weightStep);
	}

	public PredictionConfig stepDown(Weighted weighted) {
		return stepWeight(baseStepResult.getConfig(), weighted, -weightStep);
	}

	private PredictionConfig stepWeight(PredictionConfig config, Weighted weighted, double step) {
		double weight = weighted.getWeight(config) + step;
		if (weight >= minWeight && weight <= maxWeight) {
			PredictionConfig newConfig = weighted.setWeight(config, weight);
			if (newConfig.isAnyAreaEnabled() && !results.containsKey(newConfig))
				return newConfig;
		}
		return null;
	}

	/**
	 * Processes next prediction verification result
	 * @param verificationResult prediction verification result
	 * @return true if new result is the best one
	 */
	public boolean nextResult(PredictionVerificationResult verificationResult) {
		PredictionResult result = verificationResult.getResult();
		addResult(result);
		boolean best = false;
		if (resultComparator.compare(result, bestResult) > 0) {
			bestResult = result;
			best = true;
			System.out.println("***** New best result: " + bestResult);
		}
		if (bestStepResult == null || resultComparator.compare(result, bestStepResult) > 0) {
			bestStepResult = result;
			System.out.println("*** New best step result: " + bestStepResult);
		}
		return best;
	}

	private void addResult(PredictionResult result) {
		results.put(result.getConfig(), result);
		candidateNextResults.add(result);
	}
}
