package org.strangeforest.tcb.stats.service;

import java.util.*;
import java.util.Objects;

import com.google.common.base.*;

public class OpponentFilter {

	// Factory

	public static final OpponentFilter ALL = new OpponentFilter(null, null, null);

	public static OpponentFilter forMatches(String opponent, int playerId) {
		if (isSinglePlayer(opponent))
			return forMatches(null, playerId, extractPlayerId(opponent));
		else
			return forMatches(opponent, playerId, null);
	}

	private static OpponentFilter forMatches(String opponent, Integer playerId, Integer opponentId) {
		return new OpponentFilter(Opponent.forValue(opponent), playerId, opponentId);
	}

	public static OpponentFilter forStats(String opponent) {
		if (isSinglePlayer(opponent))
			return forStats(null, extractPlayerId(opponent));
		else
			return forStats(opponent, null);
	}

	public static OpponentFilter forStats(String opponent, Integer opponentId) {
		return new OpponentFilter(Opponent.forValue(opponent), null, opponentId);
	}

	private static boolean isSinglePlayer(String opponent) {
		return opponent != null && opponent.startsWith(OPPONENT_PREFIX);
	}

	private static int extractPlayerId(String opponent) {
		return Integer.parseInt(opponent.substring(OPPONENT_PREFIX.length()));
	}

	private static final String OPPONENT_PREFIX = "OPP_";


	// Instance

	private final Opponent opponent;
	private final Integer playerId;
	private final Integer opponentId;

	private static final String MATCHES_OPPONENT_CRITERION = " AND ((m.winner_id = ? AND m.loser_id = ?) OR (m.winner_id = ? AND m.loser_id = ?))";
	private static final String STATS_OPPONENT_CRITERION   = " AND opponent_id = ?";

	private OpponentFilter(Opponent opponent, Integer playerId, Integer opponentId) {
		this.opponent = opponent;
		this.playerId = playerId;
		this.opponentId = opponentId;
	}

	void appendCriteria(StringBuilder criteria) {
		if (opponent != null)
			criteria.append(isForMatches() ? opponent.getMatchesCriterion() : opponent.getStatsCriterion());
		if (opponentId != null)
			criteria.append(isForMatches() ? MATCHES_OPPONENT_CRITERION : STATS_OPPONENT_CRITERION);
	}

	void addParams(List<Object> params) {
		if (opponent != null && isForMatches()) {
			params.add(playerId);
			params.add(playerId);
		}
		if (opponentId != null) {
			if (isForMatches()) {
				params.add(playerId);
				params.add(opponentId);
				params.add(opponentId);
				params.add(playerId);
			}
			else
				params.add(opponentId);
		}
	}

	public boolean isEmpty() {
		return opponent == null && opponentId == null;
	}

	public boolean isOpponentRequired() {
		return opponent != null && opponent.isOpponentRequired();
	}

	private boolean isForMatches() {
		return playerId != null;
	}


	// Object methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof OpponentFilter)) return false;
		OpponentFilter filter = (OpponentFilter)o;
		return opponent == filter.opponent && Objects.equals(playerId, filter.playerId) && Objects.equals(opponentId, filter.opponentId);
	}

	@Override public int hashCode() {
		return Objects.hash(opponent, playerId, opponentId);
	}

	@Override public String toString() {
		return MoreObjects.toStringHelper(this).omitNullValues()
			.add("opponent", opponent)
			.add("playerId", playerId)
			.add("opponentId", opponentId)
			.toString();
	}
}
