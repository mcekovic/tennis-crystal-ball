package org.strangeforest.tcb.stats.model.prediction;

import java.time.*;
import java.util.*;
import java.util.function.*;

import org.strangeforest.tcb.stats.model.core.*;

import com.google.common.collect.*;

import static java.lang.Math.*;
import static java.util.Arrays.*;
import static org.strangeforest.tcb.stats.model.core.TournamentLevel.*;

public abstract class MatchDataUtil {

	public static double weight(long total) {
		return total > 10 ? 1.0 : total / 10.0;
	}

	public static double weight(long total1, long total2) {
		return sqrt(weight(total1) * weight(total2));
	}

	public static DoubleUnaryOperator probabilityTransformer(boolean forSet, short bestOf) {
		return forSet ? matchProbability(bestOf) : DoubleUnaryOperator.identity();
	}

	public static DoubleUnaryOperator matchProbability(short bestOf) {
		switch (bestOf) {
			case 3: return MatchDataUtil::bestOf3MatchProbability;
			case 5: return MatchDataUtil::bestOf5MatchProbability;
			default: throw new IllegalArgumentException("Invalid bestOf: " + bestOf);
		}
	}

	public static double bestOf3MatchProbability(double setProbability) {
		return setProbability * setProbability * (3 - 2 * setProbability);
	}

	public static double bestOf5MatchProbability(double setProbability) {
		return setProbability * setProbability * setProbability * (10 - 15 * setProbability + 6 * setProbability * setProbability);
	}

	public static short defaultBestOf(TournamentLevel level, Short bestOf) {
		if (bestOf != null)
			return bestOf;
		else if (level != null)
			return level.getBestOf();
		else
			return 3;
	}

	public static Short bestOf(TournamentLevel level, Short bestOf) {
		if (bestOf != null)
			return bestOf;
		else if (level != null)
			return level.getBestOf();
		else
			return null;
	}

	public static final Predicate<MatchData> ALWAYS_TRUE = m -> Boolean.TRUE;

	public static Predicate<MatchData> isRecent(LocalDate date, Period period) {
		LocalDate afterDate = date.minus(period);
		return match -> match.getDate().compareTo(afterDate) >= 0;
	}

	public static Predicate<MatchData> isSurface(Surface surface) {
		return match -> nonNullEquals(match.getSurface(), surface);
	}

	public static Predicate<MatchData> isLevel(TournamentLevel level) {
		return match -> nonNullEquals(levelGroup(match.getLevel()), levelGroup(level));
	}

	private static TournamentLevel levelGroup(TournamentLevel level) {
		if (level == null)
			return OTHERS;
		switch (level) {
			case GRAND_SLAM: return GRAND_SLAM;
			case TOUR_FINALS:
			case ALT_FINALS: return TOUR_FINALS;
			case MASTERS:
			case OLYMPICS: return MASTERS;
			case ATP_500:
			case ATP_250: return ATP_500;
			case DAVIS_CUP:
			case OTHERS_TEAM: return DAVIS_CUP;
			default: return OTHERS;
		}
	}

	public static Predicate<MatchData> isTournament(Integer tournamentId) {
		return match -> nonNullEquals(match.getTournamentId(), tournamentId);
	}

	public static Predicate<MatchData> isRound(Round round) {
		return match -> nonNullEquals(roundGroup(match.getRound()), roundGroup(round));
	}

	private static Round roundGroup(Round round) {
		if (round == null)
			return null;
		return round.compareTo(Round.QF) <= 0 ? round : null;
	}

	public static Predicate<MatchData> isOpponent(int playerId) {
		return match -> match.getOpponentId() == playerId;
	}

	public static Predicate<MatchData> isOpponentRankInRange(Range<Integer> rankRange) {
		return match -> {
			if (rankRange != null) {
				Integer opponentRank = match.getOpponentRank();
				return opponentRank != null && rankRange.contains(opponentRank);
			}
			else
				return false;
		};
	}

	private static final List<Range<Integer>> RANK_RANGES = asList(
		Range.closed(1, 5),
		Range.openClosed(5, 10),
		Range.openClosed(10, 20),
		Range.openClosed(20, 50),
		Range.openClosed(50, 100),
		Range.greaterThan(100)
	);

	public static Range<Integer> rankRange(Integer rank) {
		return rank != null ? RANK_RANGES.stream().filter(range -> range.contains(rank)).findFirst().orElse(null) : null;
	}

	public static Predicate<MatchData> isOpponentHand(String hand) {
		return match -> nonNullEquals(match.getOpponentHand(), hand);
	}

	public static Predicate<MatchData> isOpponentBackhand(String backhand) {
		return match -> nonNullEquals(match.getOpponentBackhand(), backhand);
	}

	public static Predicate<MatchData> isOpponentQualifier() {
		return match -> "Q".equals(match.getOpponentEntry());
	}

	private static boolean nonNullEquals(Object o1, Object o2) {
		return o1 != null && o2 != null && o1.equals(o2);
	}
}
