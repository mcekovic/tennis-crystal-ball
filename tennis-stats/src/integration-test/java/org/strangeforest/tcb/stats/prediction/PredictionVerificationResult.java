package org.strangeforest.tcb.stats.prediction;

import java.util.*;

import org.strangeforest.tcb.stats.model.core.*;
import org.strangeforest.tcb.stats.model.prediction.*;

import com.google.common.collect.*;

import static java.util.Comparator.*;
import static org.strangeforest.tcb.stats.model.prediction.MatchDataUtil.*;
import static org.strangeforest.tcb.util.CompareUtil.*;

public class PredictionVerificationResult {

	private final PredictionConfig config;
	private final PredictionResult result;
	private final Map<Double, PredictionResult> probabilityRangeResults;
	private final Map<Surface, PredictionResult> surfaceResults;
	private final Map<TournamentLevel, PredictionResult> levelResults;
	private final Map<Range<Integer>, PredictionResult> rankRangeResults;

	private static final int PROBABILITY_RANGES = 10;
	private static final int PROBABILITY_RANGES_2 = 2 * PROBABILITY_RANGES;

	public PredictionVerificationResult(PredictionConfig config) {
		this.config = config;
		result = new PredictionResult(config);
		probabilityRangeResults = new HashMap<>();
		surfaceResults = new HashMap<>();
		levelResults = new HashMap<>();
		rankRangeResults = new HashMap<>();
	}

	public synchronized void newMatch(MatchForVerification match, boolean predictable, double winnerProbability, boolean withPrice, boolean beatingPrice, boolean profitable, double stake, double return_) {
		result.newMatch(predictable, winnerProbability, withPrice, beatingPrice, profitable, stake, return_);
		if (predictable) {
			winnerProbability = winnerProbability >= 0.5 ? winnerProbability : 1.0 - winnerProbability;
			winnerProbability = Math.round(winnerProbability * PROBABILITY_RANGES_2) / (double)PROBABILITY_RANGES_2;
			probabilityRangeResults.computeIfAbsent(winnerProbability, s -> new PredictionResult(config)).newMatch(predictable, winnerProbability, withPrice, beatingPrice, profitable, stake, return_);
		}
		Surface surface = match.surface;
		if (surface != null)
			surfaceResults.computeIfAbsent(surface, s -> new PredictionResult(config)).newMatch(predictable, winnerProbability, withPrice, beatingPrice, profitable, stake, return_);
		TournamentLevel level = match.level;
		if (level != null)
			levelResults.computeIfAbsent(level, s -> new PredictionResult(config)).newMatch(predictable, winnerProbability, withPrice, beatingPrice, profitable, stake, return_);
		Range<Integer> rankRange = rankRange(nullsLastMin(match.winnerRank, match.loserRank));
		if (rankRange != null)
			rankRangeResults.computeIfAbsent(rankRange, s -> new PredictionResult(config)).newMatch(predictable, winnerProbability, withPrice, beatingPrice, profitable, stake, return_);
	}

	public synchronized void complete() {
		result.complete();
		for (PredictionResult result : probabilityRangeResults.values())
			result.complete();
		for (PredictionResult result : surfaceResults.values())
			result.complete();
		for (PredictionResult result : levelResults.values())
			result.complete();
		for (PredictionResult result : rankRangeResults.values())
			result.complete();
	}

	public PredictionResult getResult() {
		return result;
	}

	public Map<Double, PredictionResult> getProbabilityRangeResults() {
		return new TreeMap<>(probabilityRangeResults);
	}

	public Map<Surface, PredictionResult> getSurfaceResults() {
		return new TreeMap<>(surfaceResults);
	}

	public Map<TournamentLevel, PredictionResult> getLevelResults() {
		return new TreeMap<>(levelResults);
	}

	public Map<Range<Integer>, PredictionResult> getRankRangeResults() {
		TreeMap<Range<Integer>, PredictionResult> sorted = new TreeMap<>(comparingInt(Range::lowerEndpoint));
		sorted.putAll(rankRangeResults);
		return sorted;
	}
}
