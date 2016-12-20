package org.strangeforest.tcb.stats.model.prediction;

import java.time.*;
import java.util.*;
import java.util.function.*;

import org.strangeforest.tcb.stats.model.*;

import static java.lang.Math.*;
import static org.strangeforest.tcb.util.DateUtil.*;

public abstract class MatchDataMatchPredictor implements MatchPredictor {

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
	protected abstract Period recentPeriod();

	@Override public MatchPrediction predictMatch() {
		MatchPrediction prediction = new MatchPrediction();
		addItemProbabilities(prediction, "MATCH", ALWAYS_TRUE);
		addItemProbabilities(prediction, "SURFACE", isSurface(surface));
		addItemProbabilities(prediction, "LEVEL", isLevel(level));
		addItemProbabilities(prediction, "ROUND", isRound(round));
		addItemProbabilities(prediction, "RECENT", isRecent(date, recentPeriod()));
		addItemProbabilities(prediction, "SURFACE_RECENT", isSurface(surface).and(isRecent(date, recentPeriod())));
		addItemProbabilities(prediction, "LEVEL_RECENT", isLevel(level).and(isRecent(date, recentPeriod())));
		addItemProbabilities(prediction, "ROUND_RECENT", isRound(round).and(isRecent(date, recentPeriod())));
		addItemProbabilities(prediction, "SET", ALWAYS_TRUE);
		addItemProbabilities(prediction, "SURFACE_SET", isSurface(surface));
		addItemProbabilities(prediction, "LEVEL_SET", isLevel(level));
		addItemProbabilities(prediction, "ROUND_SET", isRound(round));
		addItemProbabilities(prediction, "RECENT_SET", isRecent(date, recentPeriod()));
		addItemProbabilities(prediction, "SURFACE_RECENT_SET", isSurface(surface).and(isRecent(date, recentPeriod())));
		addItemProbabilities(prediction, "LEVEL_RECENT_SET", isLevel(level).and(isRecent(date, recentPeriod())));
		addItemProbabilities(prediction, "ROUND_RECENT_SET", isRound(round).and(isRecent(date, recentPeriod())));
		return prediction;
	}

	protected abstract void addItemProbabilities(MatchPrediction prediction, String itemName, Predicate<MatchData> filter);

	protected static double weight(long total) {
		return total > 10 ? 1.0 : total / 10.0;
	}

	protected static double weight(long total1, long total2) {
		return sqrt(weight(total1) * weight(total2));
	}

	protected DoubleUnaryOperator probabilityTransformer(boolean forSet) {
		return forSet ? matchProbability() : DoubleUnaryOperator.identity();
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

	private static Predicate<MatchData> isSurface(Surface surface) {
		return match -> Objects.equals(match.getSurface(), surface != null ? surface.getCode() : null);
	}

	private static Predicate<MatchData> isLevel(TournamentLevel level) {
		return match -> Objects.equals(match.getLevel(), level.getCode());
	}

	private static Predicate<MatchData> isRound(Round round) {
		return match -> Objects.equals(match.getRound(), round.getCode());
	}

	private static Predicate<MatchData> isRecent(Date date, Period period) {
		return match -> toLocalDate(match.getDate()).compareTo(toLocalDate(date).minus(period)) >= 0;
	}
}
