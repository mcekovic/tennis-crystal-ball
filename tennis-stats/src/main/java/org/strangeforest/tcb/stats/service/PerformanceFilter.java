package org.strangeforest.tcb.stats.service;

import java.util.Objects;

import org.springframework.jdbc.core.namedparam.*;

import com.google.common.base.*;

public class PerformanceFilter {

	// Factory

	public static final PerformanceFilter EMPTY = new PerformanceFilter(null, null);

	public static PerformanceFilter forSeason(Integer season) {
		return new PerformanceFilter(season, null);
	}


	// Instance

	private final Integer season;
	private final Integer opponentId;

	private static final String SEASON_CRITERION   = " AND season = :season";
	private static final String OPPONENT_CRITERION = " AND opponent_id = :opponentId";

	public PerformanceFilter(Integer season, Integer opponentId) {
		this.season = season;
		this.opponentId = opponentId;
	}

	public String getCriteria() {
		StringBuilder criteria = new StringBuilder();
		if (season != null)
			criteria.append(SEASON_CRITERION);
		if (opponentId != null)
			criteria.append(OPPONENT_CRITERION);
		return criteria.toString();
	}

	public void appendParams(MapSqlParameterSource params) {
		if (season != null)
			params.addValue("season", season);
		if (opponentId != null)
			params.addValue("opponentId", opponentId);
	}

	public boolean hasSeason() {
		return season != null;
	}

	public boolean hasOpponent() {
		return opponentId != null;
	}


	// Object methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof PerformanceFilter)) return false;
		PerformanceFilter filter = (PerformanceFilter)o;
		return Objects.equals(season, filter.season) && Objects.equals(opponentId, filter.opponentId);
	}

	@Override public int hashCode() {
		return Objects.hash(season, opponentId);
	}

	@Override public final String toString() {
		return MoreObjects.toStringHelper(this).omitNullValues()
			.add("season", season)
			.add("opponentId", opponentId).toString();
	}
}