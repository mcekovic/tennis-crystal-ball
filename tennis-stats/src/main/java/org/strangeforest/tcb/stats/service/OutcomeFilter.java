package org.strangeforest.tcb.stats.service;

import java.util.Objects;

import org.springframework.jdbc.core.namedparam.*;

import com.google.common.base.*;

import static com.google.common.base.Strings.*;
import static java.lang.String.*;
import static org.strangeforest.tcb.stats.service.FilterUtil.*;

public class OutcomeFilter {

	// Factory

	public static final OutcomeFilter ALL = new OutcomeFilter(null, false);

	public static OutcomeFilter forMatches(String outcome) {
		return outcome != null ? new OutcomeFilter(outcome, false) : ALL;
	}

	public static OutcomeFilter forStats(String outcome) {
		return outcome != null ? new OutcomeFilter(outcome, true) : ALL;
	}


	// Instance

	private final Boolean won;
	private final String outcome;
	private final boolean forStats;

	private static final String WON = "won";
	private static final String LOST = "lost";

	private static final String MATCHES_WON_CRITERION = " AND m.%1$s = :playerId";
	private static final String STATS_WON_CRITERION   = " AND %1$s = 1";
	private static final String OUTCOME_CRITERION = " AND outcome = :outcome::match_outcome";

	private OutcomeFilter(String outcome, boolean forStats) {
		if (!isNullOrEmpty(outcome)) {
			if (outcome.startsWith(WON)) {
				this.won = Boolean.TRUE;
				this.outcome = outcome.substring(WON.length());
			}
			else if (outcome.startsWith(LOST)) {
				this.won = Boolean.FALSE;
				this.outcome = outcome.substring(LOST.length());
			}
			else {
				this.won = null;
				this.outcome = outcome;
			}
		}
		else {
			this.won = null;
			this.outcome = null;
		}
		this.forStats = forStats;
	}

	void appendCriteria(StringBuilder criteria) {
		if (won != null) {
			if (forStats)
				criteria.append(format(STATS_WON_CRITERION, won ? "p_matches" : "o_matches"));
			else
				criteria.append(format(MATCHES_WON_CRITERION, won ? "winner_id" : "loser_id"));
		}
		if (!isNullOrEmpty(outcome))
			criteria.append(OUTCOME_CRITERION);
	}

	void addParams(MapSqlParameterSource params) {
		if (!isNullOrEmpty(outcome))
			params.addValue("outcome", outcome);
	}

	public boolean isEmpty() {
		return won == null && isNullOrEmpty(outcome);
	}


	// Object methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof OutcomeFilter)) return false;
		OutcomeFilter filter = (OutcomeFilter)o;
		return Objects.equals(won, filter.won) && stringsEqual(outcome, filter.outcome) && forStats == filter.forStats;
	}

	@Override public int hashCode() {
		return Objects.hash(won, outcome);
	}

	@Override public String toString() {
		return MoreObjects.toStringHelper(this).omitNullValues()
			.add("won", won)
			.add("outcome", outcome)
			.toString();
	}
}
