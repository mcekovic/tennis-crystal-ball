package org.strangeforest.tcb.stats.service;

import java.util.*;
import java.util.Objects;

import com.google.common.base.*;

import static java.lang.String.*;

public class WonFilter {

	// Factory

	public static final WonFilter ALL = new WonFilter(null, null);

	public static WonFilter forMatches(Boolean won, int playerId) {
		return won != null ? new WonFilter(won, playerId) : ALL;
	}

	public static WonFilter forStats(Boolean won) {
		return won != null ? new WonFilter(won, null) : ALL;
	}


	// Instance

	private final Boolean won;
	private final Integer playerId;

	private static final String MATCHES_WON_CRITERION = " AND m.%1$s = ?";
	private static final String STATS_WON_CRITERION   = " AND %1$s = 1";

	private WonFilter(Boolean won, Integer playerId) {
		this.won = won;
		this.playerId = playerId;
	}

	void appendCriteria(StringBuilder criteria) {
		if (won != null) {
			if (isForMatches())
				criteria.append(format(MATCHES_WON_CRITERION, won ? "winner_id" : "loser_id"));
			else
				criteria.append(format(STATS_WON_CRITERION, won ? "p_matches" : "o_matches"));
		}
	}

	void addParams(List<Object> params) {
		if (won != null && isForMatches())
			params.add(playerId);
	}

	public boolean isEmpty() {
		return won == null;
	}

	private boolean isForMatches() {
		return playerId != null;
	}


	// Object methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof WonFilter)) return false;
		WonFilter filter = (WonFilter)o;
		return Objects.equals(won, filter.won) && Objects.equals(playerId, filter.playerId);
	}

	@Override public int hashCode() {
		return Objects.hash(won, playerId);
	}

	@Override public String toString() {
		return MoreObjects.toStringHelper(this).omitNullValues()
			.add("won", won)
			.add("playerId", playerId)
			.toString();
	}
}
