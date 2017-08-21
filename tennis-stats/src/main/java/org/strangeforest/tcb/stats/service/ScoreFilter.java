package org.strangeforest.tcb.stats.service;

import java.util.Objects;

import org.springframework.jdbc.core.namedparam.*;

import com.google.common.base.*;
import com.google.common.base.MoreObjects.*;

import static com.google.common.base.Strings.*;
import static java.lang.String.*;
import static org.strangeforest.tcb.stats.service.FilterUtil.*;

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
	private final boolean after;
	private final boolean all;
	private final boolean forStats;

	private static final String MATCHES_SCORE_CRITERION = " AND ((winner_id = :playerId AND w_sets = :wSets AND l_sets = :lSets) OR (loser_id = :playerId AND w_sets = :lSets AND l_sets = :wSets))";
	private static final String STATS_SCORE_CRITERION = " AND p_sets = :wSets AND o_sets = :lSets";
	private static final String FINISHED_CRITERION = " AND outcome IS NULL";
	private static final String MATCHES_WON_CRITERION = " AND winner_id = :playerId";
	private static final String MATCHES_LOST_CRITERION = " AND loser_id = :playerId";
	private static final String BEST_OF_5 = " AND best_of = 5";
	private static final String STATS_WON_CRITERION = " AND p_matches = 1";
	private static final String STATS_LOST_CRITERION = " AND p_matches = 0";
	private static final String SET_SCORE_CRITERION = " AND EXISTS (SELECT s.set FROM set_score s WHERE s.match_id = m.match_id AND s.set = %1$d AND s.w_games %2$s s.l_games)";

	private ScoreFilter(String score, boolean forStats) {
		this.forStats = forStats;
		if (!isNullOrEmpty(score)) {
			after = score.endsWith("+");
			int pos = score.indexOf(':');
			if (pos >= 0) {
				wSets = pos > 0 ? Integer.parseInt(score.substring(0, pos)) : 0;
				lSets = pos < score.length() - 1 ? Integer.parseInt(score.substring(pos + 1, score.length() - (after ? 1 : 0))) : 0;
				all = false;
			}
			else
				throw new IllegalArgumentException("Invalid score filter: " + score);
		}
		else {
			wSets = 0;
			lSets = 0;
			after = false;
			all = true;
		}
	}

	void appendCriteria(StringBuilder criteria) {
		if (!all) {
			if (after) {
				if (wSets == 1 && lSets == 0) {
					criteria.append(or(
						won() + setScore(1, ">"),
						lost() + setScore(1, "<")
					));
				}
				else if (wSets == 0 && lSets == 1) {
					criteria.append(or(
						won() + setScore(1, "<"),
						lost() + setScore(1, ">")
					));
				}
				else if (wSets == 1 && lSets == 1) {
					criteria.append(or(
						setScore(1, ">") + setScore(2, "<"),
						setScore(1, "<") + setScore(2, ">")
					));
				}
				else if (wSets == 2 && lSets == 0) {
					criteria.append(BEST_OF_5);
					criteria.append(or(
						won() + setScore(1, ">") + setScore(2, ">"),
						lost() +	setScore(1, "<") + setScore(2, "<")
					));
				}
				else if (wSets == 0 && lSets == 2) {
					criteria.append(BEST_OF_5);
					criteria.append(or(
						won() + setScore(1, "<") + setScore(2, "<"),
						lost() +	setScore(1, ">") + setScore(2, ">")
					));
				}
				else if (wSets == 2 && lSets == 1) {
					criteria.append(BEST_OF_5);
					criteria.append(or(
						won() + setScore(1, ">") + setScore(2, ">") + setScore(3, "<"),
						won() + setScore(1, ">") + setScore(2, "<") + setScore(3, ">"),
						won() + setScore(1, "<") + setScore(2, ">") + setScore(3, ">"),
						lost() +	setScore(1, "<") + setScore(2, "<") + setScore(3, ">"),
						lost() +	setScore(1, "<") + setScore(2, ">") + setScore(3, "<"),
						lost() +	setScore(1, ">") + setScore(2, "<") + setScore(3, "<")
					));
				}
				else if (wSets == 1 && lSets == 2) {
					criteria.append(BEST_OF_5);
					criteria.append(or(
						won() + setScore(1, ">") + setScore(2, "<") + setScore(3, "<"),
						won() + setScore(1, "<") + setScore(2, ">") + setScore(3, "<"),
						won() + setScore(1, "<") + setScore(2, "<") + setScore(3, ">"),
						lost() +	setScore(1, "<") + setScore(2, ">") + setScore(3, ">"),
						lost() +	setScore(1, ">") + setScore(2, "<") + setScore(3, ">"),
						lost() +	setScore(1, ">") + setScore(2, ">") + setScore(3, "<")
					));
				}
				else if (wSets == 2 && lSets == 2) {
					criteria.append(BEST_OF_5);
					criteria.append(or(
						setScore(1, ">") + setScore(2, ">") + setScore(3, "<") + setScore(4, "<"),
						setScore(1, ">") + setScore(2, "<") + setScore(3, ">") + setScore(4, "<"),
						setScore(1, ">") + setScore(2, "<") + setScore(3, "<") + setScore(4, ">"),
						setScore(1, "<") + setScore(2, ">") + setScore(3, ">") + setScore(4, "<"),
						setScore(1, "<") + setScore(2, ">") + setScore(3, "<") + setScore(4, ">"),
						setScore(1, "<") + setScore(2, "<") + setScore(3, ">") + setScore(4, ">")
					));
				}
			}
			else {
				criteria.append(forStats ? STATS_SCORE_CRITERION : MATCHES_SCORE_CRITERION);
				criteria.append(FINISHED_CRITERION);
			}
		}
	}

	private String won() {
		return forStats ? STATS_WON_CRITERION : MATCHES_WON_CRITERION;
	}

	private String lost() {
		return forStats ? STATS_LOST_CRITERION : MATCHES_LOST_CRITERION;
	}

	private static String setScore(int set, String operator) {
		return format(SET_SCORE_CRITERION, set, operator);
	}

	void addParams(MapSqlParameterSource params) {
		if (!(all || after)) {
			params.addValue("wSets", wSets);
			params.addValue("lSets", lSets);
		}
	}

	public boolean isEmpty() {
		return all;
	}


	// Object methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ScoreFilter)) return false;
		ScoreFilter filter = (ScoreFilter)o;
		return wSets == filter.wSets && lSets == filter.lSets && after == filter.after && all == filter.all;
	}

	@Override public int hashCode() {
		return Objects.hash(wSets, lSets, after, all);
	}

	@Override public String toString() {
		ToStringHelper helper = MoreObjects.toStringHelper(this);
		if (!all) {
			if (after)
				helper.add("after", after);
			helper.add("wSets", wSets);
			helper.add("lSets", lSets);
		}
		return helper.toString();
	}
}
