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
	private static final String PLAYED = "played";
	private static final String FINISHED = "finished";
	private static final String NOT_FINISHED = "notFinished";
	private static final String NOT_PLAYED = "notPlayed";
	private static final String NOT_ABANDONED = "notAbandoned";

	private static final String MATCHES_WON_CRITERION = " AND m.%1$s = :playerId";
	private static final String STATS_WON_CRITERION   = " AND m.%1$s = 1";
	private static final String OUTCOME_CRITERION = " AND m.outcome = :outcome::match_outcome";
	private static final String PLAYED_CRITERION = " AND (m.outcome IS NULL OR m.outcome IN ('RET', 'DEF'))";
	private static final String FINISHED_CRITERION = " AND m.outcome IS NULL";
	private static final String NOT_FINISHED_CRITERION = " AND m.outcome IN ('RET', 'W/O', 'DEF')";
	private static final String NOT_PLAYED_CRITERION = " AND m.outcome IN ('W/O', 'ABD')";
	private static final String NOT_ABANDONED_CRITERION = " AND (m.outcome IS NULL OR m.outcome <> 'ABD')";

	private OutcomeFilter(String outcome, boolean forStats) {
		if (!isNullOrEmpty(outcome)) {
			if (outcome.startsWith(WON)) {
				this.won = Boolean.TRUE;
				String out = outcome.substring(WON.length());
				this.outcome = out.isEmpty() ? NOT_ABANDONED : out;
			}
			else if (outcome.startsWith(LOST)) {
				this.won = Boolean.FALSE;
				String out = outcome.substring(LOST.length());
				this.outcome = out.isEmpty() ? NOT_ABANDONED : out;
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
		if (!isNullOrEmpty(outcome)) {
			switch (outcome) {
				case PLAYED:
					criteria.append(PLAYED_CRITERION);
					break;
				case FINISHED:
					criteria.append(FINISHED_CRITERION);
					break;
				case NOT_FINISHED:
					criteria.append(NOT_FINISHED_CRITERION);
					break;
				case NOT_PLAYED:
					criteria.append(NOT_PLAYED_CRITERION);
					break;
				case NOT_ABANDONED:
					criteria.append(NOT_ABANDONED_CRITERION);
					break;
				default:
					criteria.append(OUTCOME_CRITERION);
					break;
			}
		}
	}

	void addParams(MapSqlParameterSource params) {
		if (!(isNullOrEmpty(outcome) || outcome.equals(PLAYED) || outcome.equals(NOT_PLAYED) || outcome.equals(NOT_ABANDONED)))
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
		return Objects.equals(won, filter.won) && stringsEqual(outcome, filter.outcome);
	}

	@Override public int hashCode() {
		return Objects.hash(won, emptyToNull(outcome));
	}

	@Override public String toString() {
		return MoreObjects.toStringHelper(this).omitNullValues()
			.add("won", won)
			.add("outcome", emptyToNull(outcome))
			.toString();
	}
}
