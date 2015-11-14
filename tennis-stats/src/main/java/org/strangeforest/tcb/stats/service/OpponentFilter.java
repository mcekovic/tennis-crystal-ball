package org.strangeforest.tcb.stats.service;

import java.util.*;

import static com.google.common.base.Strings.*;

public class OpponentFilter {

	// Factory

	public static OpponentFilter forMatches(String opponent, int playerId) {
		return isNullOrEmpty(opponent) ? null : new OpponentFilter(Opponent.valueOf(opponent), playerId);
	}

	public static OpponentFilter forStats(String opponent) {
		return isNullOrEmpty(opponent) ? null : new OpponentFilter(Opponent.valueOf(opponent), 0);
	}



	// Instance

	private final Opponent opponent;
	private final int playerId;

	private OpponentFilter(Opponent opponent, int playerId) {
		this.opponent = opponent;
		this.playerId = playerId;
	}

	public Opponent getOpponent() {
		return opponent;
	}

	public boolean isForMatches() {
		return playerId != 0;
	}

	public String getCriterion() {
		return isForMatches() ? opponent.getMatchesCriterion() : opponent.getStatsCriterion();
	}

	public int getPlayerId() {
		return playerId;
	}


	// Object methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof OpponentFilter)) return false;
		OpponentFilter filter = (OpponentFilter)o;
		return opponent == filter.opponent && playerId == filter.playerId;
	}

	@Override public int hashCode() {
		return Objects.hash(opponent, playerId);
	}
}
