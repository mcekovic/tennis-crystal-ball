package org.strangeforest.tcb.stats.service;

import java.util.Objects;

import org.springframework.jdbc.core.namedparam.*;

import com.google.common.base.*;
import com.google.common.base.MoreObjects.*;

import static com.google.common.base.Strings.*;
import static java.lang.String.*;
import static org.strangeforest.tcb.stats.service.FilterUtil.*;
import static org.strangeforest.tcb.util.ObjectUtil.*;

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
	private final String misc;
	private final boolean all;
	private final boolean forStats;

	private static final String MATCHES_CRITERION_TEMPLATE = " AND ((m.winner_id = :playerId%1$s) OR (m.loser_id = :playerId%2$s))";
	private static final String STATS_CRITERION_TEMPLATE = " AND ((m.p_matches > 0%1$s) OR (m.o_matches > 0%2$s))";
	private static final String MATCHES_SCORE_CRITERION = " AND ((m.winner_id = :playerId AND w_sets = :wSets AND l_sets = :lSets) OR (m.loser_id = :playerId AND w_sets = :lSets AND l_sets = :wSets))";
	private static final String STATS_SCORE_CRITERION = " AND p_sets = :wSets AND o_sets = :lSets";
	private static final String FINISHED_CRITERION = " AND m.outcome IS NULL";
	private static final String MATCHES_WON_CRITERION = " AND m.winner_id = :playerId";
	private static final String MATCHES_LOST_CRITERION = " AND m.loser_id = :playerId";
	private static final String BEST_OF_3 = " AND m.best_of = 3";
	private static final String BEST_OF_5 = " AND m.best_of = 5";
	private static final String STATS_WON_CRITERION = " AND m.p_matches = 1";
	private static final String STATS_LOST_CRITERION = " AND m.p_matches = 0";
	private static final String SET_SCORE_CRITERION = " AND EXISTS (SELECT s.set FROM set_score s WHERE s.match_id = m.match_id AND s.set = %1$d AND s.w_games %2$s s.l_games)";
	private static final String MATCHES_DECIDING_SET_CRITERION = " AND m.w_sets + m.l_sets = m.best_of";
	private static final String STATS_DECIDING_SET_CRITERION = " AND m.p_sets + m.o_sets = m.best_of";
	private static final String TIE_BREAK_CRITERION = " AND EXISTS (SELECT s.set FROM set_score s WHERE s.match_id = m.match_id%1$s)";
	private static final String TIE_BREAK_PLAYED_CRITERION = " AND (s.w_tb_pt > 0 OR s.l_tb_pt > 0 OR (s.w_games = 7 AND s.l_games = 6) OR (s.w_games = 6 AND s.l_games = 7))";
	private static final String TIE_BREAK_WON_CRITERION = " AND (s.w_tb_pt > s.l_tb_pt OR (s.w_games = 7 AND s.l_games = 6))";
	private static final String TIE_BREAK_LOST_CRITERION = " AND (s.w_tb_pt < s.l_tb_pt OR (s.w_games = 6 AND s.l_games = 7))";
	private static final String SET_TIE_BREAK_CRITERION = " AND EXISTS (SELECT s.set FROM set_score s WHERE s.match_id = m.match_id AND s.set = %1$d" + TIE_BREAK_PLAYED_CRITERION + ")";
	private static final String SET_GAMES_CRITERION = " AND EXISTS (SELECT s.set FROM set_score s WHERE s.match_id = m.match_id AND (s.w_games = %1$d  AND s.l_games = %2$d))";

	private ScoreFilter(String score, boolean forStats) {
		this.forStats = forStats;
		if (!isNullOrEmpty(score)) {
			if (score.startsWith("*")) {
				misc = score.substring(1);
				wSets = lSets = 0;
				after = false;
				all = false;
			}
			else {
				after = score.endsWith("+");
				int pos = score.indexOf(':');
				if (pos >= 0) {
					wSets = pos > 0 ? Integer.parseInt(score.substring(0, pos)) : 0;
					lSets = pos < score.length() - 1 ? Integer.parseInt(score.substring(pos + 1, score.length() - (after ? 1 : 0))) : 0;
					misc = null;
					all = false;
				}
				else
					throw new IllegalArgumentException("Invalid score filter: " + score);
			}
		}
		else {
			wSets = lSets = 0;
			after = false;
			misc = null;
			all = true;
		}
	}

	void appendCriteria(StringBuilder criteria) {
		if (all)
			return;
		if (after) {
			if (wSets == 1 && lSets == 0)
				criteria.append(after_1_0());
			else if (wSets == 0 && lSets == 1)
				criteria.append(after_0_1());
			else if (wSets == 1 && lSets == 1)
				criteria.append(after_1_1());
			else if (wSets == 2 && lSets == 0)
				criteria.append(after_2_0());
			else if (wSets == 0 && lSets == 2)
				criteria.append(after_0_2());
			else if (wSets == 2 && lSets == 1)
				criteria.append(after_2_1());
			else if (wSets == 1 && lSets == 2)
				criteria.append(after_1_2());
			else if (wSets == 2 && lSets == 2)
				criteria.append(after_2_2());
		}
		else if (isNullOrEmpty(misc)) {
			criteria.append(forStats ? STATS_SCORE_CRITERION : MATCHES_SCORE_CRITERION);
			criteria.append(FINISHED_CRITERION);
		}
		else if (misc.equals("DS"))
			criteria.append(forStats ? STATS_DECIDING_SET_CRITERION : MATCHES_DECIDING_SET_CRITERION);
		else if (misc.equals("TB"))
			criteria.append(format(TIE_BREAK_CRITERION, TIE_BREAK_PLAYED_CRITERION));
		else if (misc.equals("TBW"))
			criteria.append(format(forStats ? STATS_CRITERION_TEMPLATE : MATCHES_CRITERION_TEMPLATE, format(TIE_BREAK_CRITERION, TIE_BREAK_WON_CRITERION), format(TIE_BREAK_CRITERION, TIE_BREAK_LOST_CRITERION)));
		else if (misc.equals("TBL"))
			criteria.append(format(forStats ? STATS_CRITERION_TEMPLATE : MATCHES_CRITERION_TEMPLATE, format(TIE_BREAK_CRITERION, TIE_BREAK_LOST_CRITERION), format(TIE_BREAK_CRITERION, TIE_BREAK_WON_CRITERION)));
		else if (misc.equals("DSTB")) {
			criteria.append(or(
				BEST_OF_3 + format(SET_TIE_BREAK_CRITERION, 3),
				BEST_OF_5 + format(SET_TIE_BREAK_CRITERION, 5)
			));
		}
		else if (misc.length() == 3 && misc.charAt(1) == ':') {
			int wGames = Integer.parseInt(misc.substring(0, 1));
			int lGames = Integer.parseInt(misc.substring(2));
			criteria.append(format(forStats ? STATS_CRITERION_TEMPLATE : MATCHES_CRITERION_TEMPLATE, format(SET_GAMES_CRITERION, wGames, lGames), format(SET_GAMES_CRITERION, lGames, wGames)));
		}
	}

	private String after_1_0() {
		return or(
			won() + setScore(1, ">"),
			lost() + setScore(1, "<")
		);
	}

	private String after_0_1() {
		return or(
			won() + setScore(1, "<"),
			lost() + setScore(1, ">")
		);
	}

	private String after_1_1() {
		return or(
			setScore(1, ">") + setScore(2, "<"),
			setScore(1, "<") + setScore(2, ">")
		);
	}

	private String after_0_2() {
		return BEST_OF_5 + or(
			won() + setScore(1, "<") + setScore(2, "<"),
			lost() +	setScore(1, ">") + setScore(2, ">")
		);
	}

	private String after_2_0() {
		return BEST_OF_5 + or(
			won() + setScore(1, ">") + setScore(2, ">"),
			lost() +	setScore(1, "<") + setScore(2, "<")
		);
	}

	private String after_2_1() {
		return BEST_OF_5 + or(
			won() + setScore(1, ">") + setScore(2, ">") + setScore(3, "<"),
			won() + setScore(1, ">") + setScore(2, "<") + setScore(3, ">"),
			won() + setScore(1, "<") + setScore(2, ">") + setScore(3, ">"),
			lost() +	setScore(1, "<") + setScore(2, "<") + setScore(3, ">"),
			lost() +	setScore(1, "<") + setScore(2, ">") + setScore(3, "<"),
			lost() +	setScore(1, ">") + setScore(2, "<") + setScore(3, "<")
		);
	}

	private String after_1_2() {
		return BEST_OF_5 + or(
			won() + setScore(1, ">") + setScore(2, "<") + setScore(3, "<"),
			won() + setScore(1, "<") + setScore(2, ">") + setScore(3, "<"),
			won() + setScore(1, "<") + setScore(2, "<") + setScore(3, ">"),
			lost() +	setScore(1, "<") + setScore(2, ">") + setScore(3, ">"),
			lost() +	setScore(1, ">") + setScore(2, "<") + setScore(3, ">"),
			lost() +	setScore(1, ">") + setScore(2, ">") + setScore(3, "<")
		);
	}

	private String after_2_2() {
		return BEST_OF_5 + or(
			setScore(1, ">") + setScore(2, ">") + setScore(3, "<") + setScore(4, "<"),
			setScore(1, ">") + setScore(2, "<") + setScore(3, ">") + setScore(4, "<"),
			setScore(1, ">") + setScore(2, "<") + setScore(3, "<") + setScore(4, ">"),
			setScore(1, "<") + setScore(2, ">") + setScore(3, ">") + setScore(4, "<"),
			setScore(1, "<") + setScore(2, ">") + setScore(3, "<") + setScore(4, ">"),
			setScore(1, "<") + setScore(2, "<") + setScore(3, ">") + setScore(4, ">")
		);
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
		return wSets == filter.wSets && lSets == filter.lSets && after == filter.after && stringsEqual(misc, filter.misc) && all == filter.all;
	}

	@Override public int hashCode() {
		return Objects.hash(wSets, lSets, after, emptyToNull(misc), all);
	}

	@Override public String toString() {
		ToStringHelper helper = MoreObjects.toStringHelper(this).omitNullValues();
		if (!all) {
			if (isNullOrEmpty(misc)) {
				helper.add("after", nullIf(after, true));
				helper.add("wSets", wSets);
				helper.add("lSets", lSets);
			}
			else
				helper.add("misc", misc);
		}
		return helper.toString();
	}
}
