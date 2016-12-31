package org.strangeforest.tcb.stats.service;

import java.util.Objects;

import org.springframework.jdbc.core.namedparam.*;

import com.google.common.base.*;

public class OpponentFilter {

	// Factory

	public static final OpponentFilter ALL = new OpponentFilter(null, null, false);

	public static OpponentFilter forMatches(String opponent) {
		if (isSinglePlayer(opponent))
			return forMatches(null, extractOpponentId(opponent));
		else
			return forMatches(opponent, null);
	}

	private static OpponentFilter forMatches(String opponent, Integer opponentId) {
		return new OpponentFilter(Opponent.forValue(opponent), opponentId, false);
	}

	public static OpponentFilter forStats(String opponent) {
		if (isSinglePlayer(opponent))
			return forStats(null, extractOpponentId(opponent));
		else
			return forStats(opponent, null);
	}

	public static OpponentFilter forStats(String opponent, Integer opponentId) {
		return new OpponentFilter(Opponent.forValue(opponent), opponentId, true);
	}

	public static OpponentFilter forStats(Integer opponentId) {
		return new OpponentFilter(null, opponentId, true);
	}

	private static boolean isSinglePlayer(String opponent) {
		return opponent != null && opponent.startsWith(OPPONENT_PREFIX);
	}

	private static int extractOpponentId(String opponent) {
		return Integer.parseInt(opponent.substring(OPPONENT_PREFIX.length()));
	}

	private static final String OPPONENT_PREFIX = "OPP_";


	// Instance

	private final Opponent opponent;
	private final Integer opponentId;
	private final boolean forStats;

	private static final String MATCHES_OPPONENT_CRITERION = " AND ((m.winner_id = :playerId AND m.loser_id = :opponentId) OR (m.winner_id = :opponentId AND m.loser_id = :playerId))";
	private static final String STATS_OPPONENT_CRITERION   = " AND opponent_id = :opponentId";

	private OpponentFilter(Opponent opponent, Integer opponentId, boolean forStats) {
		this.opponent = opponent;
		this.opponentId = opponentId;
		this.forStats = forStats;
	}

	void appendCriteria(StringBuilder criteria) {
		if (opponent != null)
			criteria.append(forStats ? opponent.getStatsCriterion() : opponent.getMatchesCriterion());
		if (opponentId != null)
			criteria.append(forStats ? STATS_OPPONENT_CRITERION : MATCHES_OPPONENT_CRITERION);
	}

	void addParams(MapSqlParameterSource params) {
		if (opponentId != null)
			params.addValue("opponentId", opponentId);
	}

	public boolean isEmpty() {
		return opponent == null && opponentId == null;
	}

	public boolean isOpponentRequired() {
		return opponent != null && opponent.isOpponentRequired();
	}


	// Object methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof OpponentFilter)) return false;
		OpponentFilter filter = (OpponentFilter)o;
		return opponent == filter.opponent && Objects.equals(opponentId, filter.opponentId) && forStats == filter.forStats;
	}

	@Override public int hashCode() {
		return Objects.hash(opponent, opponentId);
	}

	@Override public String toString() {
		return MoreObjects.toStringHelper(this).omitNullValues()
			.add("opponent", opponent)
			.add("opponentId", opponentId)
			.toString();
	}
}
