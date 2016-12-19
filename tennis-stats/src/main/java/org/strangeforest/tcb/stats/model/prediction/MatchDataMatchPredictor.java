package org.strangeforest.tcb.stats.model.prediction;

import java.time.*;
import java.util.*;
import java.util.function.*;

import org.strangeforest.tcb.stats.model.*;

import static java.lang.Math.*;
import static org.strangeforest.tcb.stats.model.prediction.MatchPredictionItem.*;
import static org.strangeforest.tcb.util.DateUtil.*;

public abstract class MatchDataMatchPredictor implements MatchPredictor {

	private static final Period RECENT_PERIOD = Period.ofYears(2);
	protected final List<MatchData> matchData1;
	protected final List<MatchData> matchData2;
	protected final int playerId1;
	protected final int playerId2;
	private final Date date;
	private final Surface surface;
	private final TournamentLevel level;
	private final Round round;
	private final short bestOf;

	public MatchDataMatchPredictor(short bestOf, TournamentLevel level, List<MatchData> matchData2, int playerId2, Date date, List<MatchData> matchData1, int playerId1, Round round, Surface surface) {
		this.bestOf = bestOf;
		this.level = level;
		this.matchData2 = matchData2;
		this.playerId2 = playerId2;
		this.date = date;
		this.matchData1 = matchData1;
		this.playerId1 = playerId1;
		this.round = round;
		this.surface = surface;
	}

	@Override public MatchPrediction predictMatch() {
		MatchPrediction prediction = new MatchPrediction();
		addItemProbabilities(prediction, OVERALL, ALWAYS_TRUE);
		addItemProbabilities(prediction, SURFACE, isSurface(surface.getCode()));
		addItemProbabilities(prediction, LEVEL, isLevel(level.getCode()));
		addItemProbabilities(prediction, ROUND, isRound(round.getCode()));
		addItemProbabilities(prediction, RECENT, isRecent(date, RECENT_PERIOD));
		addItemProbabilities(prediction, SURFACE_RECENT, isSurface(surface.getCode()).and(isRecent(date, RECENT_PERIOD)));
		addItemProbabilities(prediction, SET, ALWAYS_TRUE);
		addItemProbabilities(prediction, SURFACE_SET, isSurface(surface.getCode()));
		return prediction;
	}

	protected abstract void addItemProbabilities(MatchPrediction prediction, MatchPredictionItem item, Predicate<MatchData> filter);

	protected static double weight(long total) {
		return total > 10 ? 1.0 : total / 10.0;
	}

	protected static double weight(long total1, long total2) {
		return sqrt(weight(total1) * weight(total2));
	}

	protected DoubleUnaryOperator probabilityTransformer(MatchPredictionItem item) {
		return item.forSet() ? matchProbability() : DoubleUnaryOperator.identity();
	}

	private DoubleUnaryOperator matchProbability() {
		switch (bestOf) {
			case 3: return MatchDataMatchPredictor::bestOf3MatchProbability;
			case 5: return MatchDataMatchPredictor::bestOf5MatchProbability;
			default: throw new IllegalArgumentException("Invalid bestOf: " + bestOf);
		}
	}

	private static double bestOf3MatchProbability(double setProbability) {
		return setProbability * setProbability * (3 - 2 * setProbability);
	}

	private static double bestOf5MatchProbability(double setProbability) {
		return setProbability * setProbability * setProbability * (10 - 15 * setProbability + 6 * setProbability * setProbability);
	}

	private static final Predicate<MatchData> ALWAYS_TRUE = m -> Boolean.TRUE;

	private static Predicate<MatchData> isSurface(String surface) {
		return m -> Objects.equals(m.getSurface(), surface);
	}

	private static Predicate<MatchData> isLevel(String level) {
		return m -> Objects.equals(m.getLevel(), level);
	}

	private static Predicate<MatchData> isRound(String round) {
		return m -> Objects.equals(m.getRound(), round);
	}

	private static Predicate<MatchData> isRecent(Date date, Period period) {
		return m -> toLocalDate(m.getDate()).compareTo(toLocalDate(date).minus(period)) >= 0;
	}
}
