package org.strangeforest.tcb.stats.service;

import java.util.*;
import java.util.Objects;

import org.springframework.jdbc.core.namedparam.*;

import com.google.common.base.*;

public class OpponentFilter {

	// Factory

	public static final OpponentFilter ALL = new OpponentFilter(null, null, null, false);
	public static final String OPPONENT_PREFIX = "OPP_";

	public static OpponentFilter forMatches(String opponent, Collection<String> countryIds) {
		if (isSinglePlayer(opponent))
			return forMatches(null, extractOpponentId(opponent), countryIds);
		else
			return forMatches(opponent, null, countryIds);
	}

	private static OpponentFilter forMatches(String opponent, Integer opponentId, Collection<String> countryIds) {
		return new OpponentFilter(Opponent.forValue(opponent), opponentId, countryIds, false);
	}

	public static OpponentFilter forStats(String opponent, Collection<String> countryIds) {
		if (isSinglePlayer(opponent))
			return forStats(null, extractOpponentId(opponent), countryIds);
		else
			return forStats(opponent, null, countryIds);
	}

	private static OpponentFilter forStats(String opponent, Integer opponentId, Collection<String> countryIds) {
		return new OpponentFilter(Opponent.forValue(opponent), opponentId, countryIds, true);
	}

	public static OpponentFilter forStats(Integer opponentId) {
		return new OpponentFilter(null, opponentId, null, true);
	}

	private static boolean isSinglePlayer(String opponent) {
		return opponent != null && opponent.startsWith(OPPONENT_PREFIX);
	}

	private static int extractOpponentId(String opponent) {
		return Integer.parseInt(opponent.substring(OPPONENT_PREFIX.length()));
	}


	// Instance

	private final Opponent opponent;
	private final Integer opponentId;
	private final Collection<String> countryIds;
	private final boolean forStats;

	private static final String MATCHES_OPPONENT_CRITERION = " AND ((m.winner_id = :playerId AND m.loser_id = :opponentId) OR (m.winner_id = :opponentId AND m.loser_id = :playerId))";
	private static final String MATCHES_COUNTRY_CRITERION = " AND ((m.winner_id = :playerId AND m.loser_country_id IN (:countryIds)) OR (m.winner_country_id IN (:countryIds) AND m.loser_id = :playerId))";

	private static final String STATS_OPPONENT_CRITERION = " AND opponent_id = :opponentId";
	private static final String STATS_COUNTRY_CRITERION = " AND opponent_country_id IN (:countryIds)";

	private OpponentFilter(Opponent opponent, Integer opponentId, Collection<String> countryIds, boolean forStats) {
		this.opponent = opponent;
		this.opponentId = opponentId;
		this.countryIds = countryIds != null ? countryIds : Collections.emptyList();
		this.forStats = forStats;
	}

	void appendCriteria(StringBuilder criteria) {
		if (opponent != null)
			criteria.append(forStats ? opponent.getStatsCriterion() : opponent.getMatchesCriterion());
		if (opponentId != null)
			criteria.append(forStats ? STATS_OPPONENT_CRITERION : MATCHES_OPPONENT_CRITERION);
		if (!countryIds.isEmpty())
			criteria.append(forStats ? STATS_COUNTRY_CRITERION : MATCHES_COUNTRY_CRITERION);
	}

	void addParams(MapSqlParameterSource params) {
		if (opponentId != null)
			params.addValue("opponentId", opponentId);
		if (!countryIds.isEmpty())
			params.addValue("countryIds", countryIds);
	}

	public Opponent getOpponent() {
		return opponent;
	}

	public boolean hasOpponent() {
		return opponent != null;
	}

	public boolean hasCountries() {
		return !countryIds.isEmpty();
	}

	public boolean isEmpty() {
		return opponent == null && opponentId == null && countryIds.isEmpty();
	}

	public boolean isOpponentRequired() {
		return opponent != null && opponent.isOpponentRequired();
	}


	// Object methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof OpponentFilter)) return false;
		OpponentFilter filter = (OpponentFilter)o;
		return opponent == filter.opponent && Objects.equals(opponentId, filter.opponentId) && countryIds.equals(filter.countryIds);
	}

	@Override public int hashCode() {
		return Objects.hash(opponent, opponentId, countryIds);
	}

	@Override public String toString() {
		return MoreObjects.toStringHelper(this).omitNullValues()
			.add("opponent", opponent)
			.add("opponentId", opponentId)
			.add("countryIds", countryIds.isEmpty() ? null : countryIds)
			.toString();
	}
}
