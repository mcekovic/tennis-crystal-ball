package org.strangeforest.tcb.stats.service;

import java.util.Objects;

import org.springframework.jdbc.core.namedparam.*;

import com.google.common.base.*;
import com.google.common.base.MoreObjects.*;

import static com.google.common.base.Strings.*;

public class ScoreFilter {

	// Factory

	public static final ScoreFilter ALL = new ScoreFilter(null, false);

	public static ScoreFilter forMatches(String score) {
		return new ScoreFilter(score, false);
	}

	public static ScoreFilter forStats(String score) {
		return new ScoreFilter(score, true);
	}


	// Instance

	private final int wSets, lSets;
	private final boolean all;
	private final boolean forStats;

	private static final String MATCHES_SCORE_CRITERION = " AND w_sets = :wSets AND l_sets = :lSets";
	private static final String STATS_SCORE_CRITERION = " AND ((p_matches = 1 AND p_sets = :wSets AND o_sets = :lSets) OR (o_matches = 1 AND p_sets = :lSets AND o_sets = :wSets))";

	private ScoreFilter(String score, boolean forStats) {
		this.forStats = forStats;
		if (!isNullOrEmpty(score)) {
			int pos = score.indexOf(':');
			if (pos >= 0) {
				wSets = pos > 0 ? Integer.parseInt(score.substring(0, pos)) : 0;
				lSets = pos < score.length() - 1 ? Integer.parseInt(score.substring(pos + 1)) : 0;
				all = false;
			}
			else
				throw new IllegalArgumentException("Invalid score filter: " + score);
		}
		else {
			wSets = 0;
			lSets = 0;
			all = true;
		}
	}

	void appendCriteria(StringBuilder criteria) {
		if (!all)
			criteria.append(forStats ? STATS_SCORE_CRITERION : MATCHES_SCORE_CRITERION);
	}

	void addParams(MapSqlParameterSource params) {
		if (!all)
			params.addValue("wSets", wSets);
			params.addValue("lSets", lSets);
	}

	public boolean isEmpty() {
		return all;
	}


	// Object methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ScoreFilter)) return false;
		ScoreFilter filter = (ScoreFilter)o;
		return wSets == filter.wSets && lSets == filter.lSets && all == filter.all && forStats == filter.forStats;
	}

	@Override public int hashCode() {
		return Objects.hash(wSets, lSets, all);
	}

	@Override public String toString() {
		ToStringHelper helper = MoreObjects.toStringHelper(this);
		if (!all) {
			helper.add("wSets", wSets);
			helper.add("lSets", lSets);
		}
		return helper.toString();
	}
}
