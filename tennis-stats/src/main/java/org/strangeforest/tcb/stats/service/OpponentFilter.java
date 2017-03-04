package org.strangeforest.tcb.stats.service;

import java.util.Objects;

import org.springframework.jdbc.core.namedparam.*;

import com.google.common.base.*;

import static com.google.common.base.Strings.*;
import static org.strangeforest.tcb.stats.service.FilterUtil.*;

public class OpponentFilter {

	// Factory

	public static final OpponentFilter ALL = new OpponentFilter(null, null, null, false);

	public static OpponentFilter forMatches(String opponent, String countryId) {
		if (isSinglePlayer(opponent))
			return forMatches(null, extractOpponentId(opponent), countryId);
		else
			return forMatches(opponent, null, countryId);
	}

	private static OpponentFilter forMatches(String opponent, Integer opponentId, String countryId) {
		return new OpponentFilter(Opponent.forValue(opponent), opponentId, countryId, false);
	}

	public static OpponentFilter forStats(String opponent, String countryId) {
		if (isSinglePlayer(opponent))
			return forStats(null, extractOpponentId(opponent), countryId);
		else
			return forStats(opponent, null, countryId);
	}

	private static OpponentFilter forStats(String opponent, Integer opponentId, String countryId) {
		return new OpponentFilter(Opponent.forValue(opponent), opponentId, countryId, true);
	}

	static OpponentFilter forStats(Integer opponentId) {
		return new OpponentFilter(null, opponentId, null, true);
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
	private final String countryId;
	private final boolean forStats;

	private static final String MATCHES_OPPONENT_CRITERION = " AND ((m.winner_id = :playerId AND m.loser_id = :opponentId) OR (m.winner_id = :opponentId AND m.loser_id = :playerId))";
	private static final String MATCHES_COUNTRY_CRITERION = " AND ((m.winner_id = :playerId AND m.loser_country_id = :countryId) OR (m.winner_country_id = :countryId AND m.loser_id = :playerId))";

	private static final String STATS_OPPONENT_CRITERION   = " AND opponent_id = :opponentId";
	private static final String STATS_COUNTRY_CRITERION   = " AND opponent_country_id = :countryId";

	private OpponentFilter(Opponent opponent, Integer opponentId, String countryId, boolean forStats) {
		this.opponent = opponent;
		this.opponentId = opponentId;
		this.countryId = countryId;
		this.forStats = forStats;
	}

	void appendCriteria(StringBuilder criteria) {
		if (opponent != null)
			criteria.append(forStats ? opponent.getStatsCriterion() : opponent.getMatchesCriterion());
		if (opponentId != null)
			criteria.append(forStats ? STATS_OPPONENT_CRITERION : MATCHES_OPPONENT_CRITERION);
		if (!isNullOrEmpty(countryId))
			criteria.append(forStats ? STATS_COUNTRY_CRITERION : MATCHES_COUNTRY_CRITERION);
	}

	void addParams(MapSqlParameterSource params) {
		if (opponentId != null)
			params.addValue("opponentId", opponentId);
		if (!isNullOrEmpty(countryId))
			params.addValue("countryId", countryId);
	}

	public boolean isEmpty() {
		return opponent == null && opponentId == null && isNullOrEmpty(countryId);
	}

	public boolean isOpponentRequired() {
		return opponent != null && opponent.isOpponentRequired();
	}


	// Object methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof OpponentFilter)) return false;
		OpponentFilter filter = (OpponentFilter)o;
		return opponent == filter.opponent && Objects.equals(opponentId, filter.opponentId) && stringsEqual(countryId, filter.countryId) && forStats == filter.forStats;
	}

	@Override public int hashCode() {
		return Objects.hash(opponent, opponentId, emptyToNull(countryId));
	}

	@Override public String toString() {
		return MoreObjects.toStringHelper(this).omitNullValues()
			.add("opponent", opponent)
			.add("opponentId", opponentId)
			.add("countryId", countryId)
			.toString();
	}
}
