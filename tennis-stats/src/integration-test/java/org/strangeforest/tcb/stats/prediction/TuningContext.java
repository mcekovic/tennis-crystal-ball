package org.strangeforest.tcb.stats.prediction;

import java.util.*;

import org.strangeforest.tcb.stats.model.prediction.*;

import static org.strangeforest.tcb.stats.prediction.BasePredictionVerificationIT.*;

public class TuningContext {

	private final Comparator<PredictionResult> resultComparator;
	private final double minWeight;
	private final double maxWeight;
	private final double weightStep;
	private final Map<Properties, PredictionResult> results;
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

	public void initialResult(PredictionResult initialResult) {
		addResult(initialResult);
		bestResult = initialResult;
		System.out.println("***** Initial result: " + bestResult);
	}

	public void finish() {
		System.out.println("***** Best result: " + bestResult);
		PredictionConfig.set(bestResult.getConfig());
		printWeights();
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
			System.out.println("*** Tuning step " + step + " finished [results: " + results.size() + ", pending steps: " + candidateNextResults.size() + "]:" + bestStepResult);
		return bestStepResult;
	}

	public boolean stepUp(Weighted weighted) {
		PredictionConfig.set(baseStepResult.getConfig());
		if (tryStepUp(weighted)) {
			if (isNewConfig())
				return true;
			else {
				tryStepDown(weighted);
				return false;
			}
		}
		else
			return false;
	}

	public boolean stepDown(Weighted weighted) {
		PredictionConfig.set(baseStepResult.getConfig());
		if (tryStepDown(weighted)) {
			if (isNewConfig())
				return true;
			else {
				tryStepUp(weighted);
				return false;
			}
		}
		else
			return false;
	}

	private boolean tryStepUp(Weighted weighted) {
		return stepWeight(weighted, weightStep);
	}

	private boolean tryStepDown(Weighted weighted) {
		return stepWeight(weighted, -weightStep);
	}

	private boolean stepWeight(Weighted weighted, double step) {
		double weight = weighted.getWeight() + step;
		if (weight >= minWeight && weight <= maxWeight) {
			weighted.setWeight(weight);
			return true;
		}
		else
			return false;
	}

	private boolean isNewConfig() {
		return PredictionArea.isAnyEnabled() && !results.containsKey(PredictionConfig.get());
	}

	public void nextResult(PredictionResult result) {
		addResult(result);
		if (resultComparator.compare(result, bestResult) > 0) {
			bestResult = result;
			System.out.println("***** New best result: " + bestResult);
		}
		if (bestStepResult == null || resultComparator.compare(result, bestStepResult) > 0) {
			bestStepResult = result;
			System.out.println("*** New best step result: " + bestStepResult);
		}
	}

	private void addResult(PredictionResult result) {
		results.put(result.getConfig(), result);
		candidateNextResults.add(result);
	}
}
