package org.strangeforest.tcb.stats.service.prediction;

import java.sql.*;
import java.time.*;
import java.util.Date;
import java.util.*;
import java.util.function.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.prediction.*;

import static org.strangeforest.tcb.stats.model.prediction.H2HPredictionItem.*;
import static org.strangeforest.tcb.stats.service.ParamsUtil.*;
import static org.strangeforest.tcb.util.DateUtil.*;

@Component
public class H2HMatchPredictor implements MatchPredictor {

	@Autowired private NamedParameterJdbcTemplate jdbcTemplate;

	private static final Period RECENT_PERIOD = Period.ofYears(2);

	private static final String H2H_MATCHES_QUERY = //language=SQL
		"SELECT match_id, date, winner_id, loser_id, level, surface, round, w_sets, l_sets, outcome\n" +
		"FROM match_for_stats_v\n" +
		"WHERE ((winner_id = :playerId1 AND loser_id = :playerId2) OR (winner_id = :playerId2 AND loser_id = :playerId1))\n" +
		"AND date < :date";


	@Override public PredictionArea area() {
		return PredictionArea.H2H;
	}

	@Override public MatchPrediction predictMatch(int playerId1, int playerId2, Date date, Surface surface, TournamentLevel level, Round round, short bestOf) {
		MatchPrediction prediction = new MatchPrediction();
		List<H2HMatch> h2h = jdbcTemplate.query(
			H2H_MATCHES_QUERY,
			params("playerId1", playerId1).addValue("playerId2", playerId2).addValue("date", date),
			(rs, rowNum) -> h2hMatch(rs)
		);
		addItemProbabilities(prediction, H2H, ALWAYS_TRUE, h2h, playerId1, playerId2);
		addItemProbabilities(prediction, SURFACE_H2H, isSurface(surface.getCode()), h2h, playerId1, playerId2);
		addItemProbabilities(prediction, LEVEL_H2H, isLevel(level.getCode()), h2h, playerId1, playerId2);
		addItemProbabilities(prediction, ROUND_H2H, isRound(round.getCode()), h2h, playerId1, playerId2);
		addItemProbabilities(prediction, RECENT_H2H, isRecent(date, RECENT_PERIOD), h2h, playerId1, playerId2);
		addItemProbabilities(prediction, SURFACE_RECENT_H2H, isSurface(surface.getCode()).and(isRecent(date, RECENT_PERIOD)), h2h, playerId1, playerId2);
		addItemProbabilitiesForSets(prediction, SET_H2H, ALWAYS_TRUE, h2h, playerId1, playerId2, bestOf);
		addItemProbabilitiesForSets(prediction, SURFACE_SET_H2H, isSurface(surface.getCode()), h2h, playerId1, playerId2, bestOf);
		return prediction;
	}

	private static void addItemProbabilities(MatchPrediction prediction, H2HPredictionItem item, Predicate<H2HMatch> filter, List<H2HMatch> h2h, int playerId1, int playerId2) {
		long won1 = h2h.stream().filter(filter).filter(isWinner(playerId1)).count();
		long won2 = h2h.stream().filter(filter).filter(isWinner(playerId2)).count();
		long total = won1 + won2;
		if (total > 0) {
			double weight = item.weight() * weight(total);
			prediction.addItemProbability1(item, weight, 1.0 * won1 / total);
			prediction.addItemProbability2(item, weight, 1.0 * won2 / total);
		}
	}

	private static void addItemProbabilitiesForSets(MatchPrediction prediction, H2HPredictionItem item, Predicate<H2HMatch> filter, List<H2HMatch> h2h, int playerId1, int playerId2, short bestOf) {
		long won1 = h2h.stream().filter(filter).filter(isWinner(playerId1)).mapToInt(H2HMatch::getwSets).sum()
		          + h2h.stream().filter(filter).filter(isWinner(playerId2)).mapToInt(H2HMatch::getlSets).sum();
		long won2 = h2h.stream().filter(filter).filter(isWinner(playerId2)).mapToInt(H2HMatch::getwSets).sum()
		          + h2h.stream().filter(filter).filter(isWinner(playerId1)).mapToInt(H2HMatch::getlSets).sum();
		long total = won1 + won2;
		if (total > 0) {
			double weight = item.weight() * weight(total);
			prediction.addItemProbability1(item, weight, matchProbability(1.0 * won1 / total, bestOf));
			prediction.addItemProbability2(item, weight, matchProbability(1.0 * won2 / total, bestOf));
		}
	}

	private static double weight(long total) {
		return total > 10 ? 1.0 : total / 10.0;
	}

	private static double matchProbability(double setProbability, short bestOf) {
		switch (bestOf) {
			case 3: return bestOf3MatchProbability(setProbability);
			case 5: return bestOf5MatchProbability(setProbability);
			default: throw new IllegalArgumentException("Invalid bestOf: " + bestOf);
		}
	}

	private static double bestOf3MatchProbability(double setProbability) {
		return setProbability * setProbability * (3 - 2 * setProbability);
	}

	private static double bestOf5MatchProbability(double setProbability) {
		return setProbability * setProbability * setProbability * (10 - 15 * setProbability + 6 * setProbability * setProbability);
	}

	private static H2HMatch h2hMatch(ResultSet rs) throws SQLException {
		return new H2HMatch(
			rs.getLong("match_id"),
			rs.getDate("date"),
			rs.getInt("winner_id"),
			rs.getInt("loser_id"),
			rs.getString("level"),
			rs.getString("surface"),
			rs.getString("round"),
			rs.getInt("w_sets"),
			rs.getInt("l_sets"),
			rs.getString("outcome")
		);
	}

	private static final Predicate<H2HMatch> ALWAYS_TRUE = m -> Boolean.TRUE;

	private static Predicate<H2HMatch> isWinner(int playerId1) {
		return m -> m.getWinnerId() == playerId1;
	}

	private static Predicate<H2HMatch> isSurface(String surface) {
		return m -> Objects.equals(m.getSurface(), surface);
	}

	private static Predicate<H2HMatch> isLevel(String level) {
		return m -> Objects.equals(m.getLevel(), level);
	}

	private static Predicate<H2HMatch> isRound(String round) {
		return m -> Objects.equals(m.getRound(), round);
	}

	private static Predicate<H2HMatch> isRecent(Date date, Period period) {
		return m -> toLocalDate(m.getDate()).compareTo(toLocalDate(date).minus(period)) >= 0;
	}
}
