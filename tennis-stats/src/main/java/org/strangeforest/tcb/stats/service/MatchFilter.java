package org.strangeforest.tcb.stats.service;

import java.time.*;
import java.util.*;

import org.springframework.jdbc.core.namedparam.*;
import org.strangeforest.tcb.stats.model.core.*;

import com.google.common.base.MoreObjects.*;
import com.google.common.collect.*;

import static com.google.common.base.Strings.*;
import static java.lang.String.*;
import static org.strangeforest.tcb.stats.service.FilterUtil.*;
import static org.strangeforest.tcb.util.ObjectUtil.*;

public class MatchFilter extends TournamentEventResultFilter {

	// Factory

	public static final MatchFilter ALL = new MatchFilter(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, false, null);

	public static MatchFilter forTournamentEvent(int tournamentEventId) {
		return new MatchFilter(null, null, null, null, null, null, null, null, null, null, tournamentEventId, null, null, null, null, false, null);
	}

	public static MatchFilter forOpponent(int opponentId) {
		return new MatchFilter(null, null, null, null, null, null, null, null, null, null, null, OpponentFilter.forStats(opponentId), null, null, null, false, null);
	}

	public static MatchFilter forOpponent(int opponentId, String level, String surface, Boolean indoor, String round) {
		return new MatchFilter(null, null, level, null, surface, indoor, null, round, null, null, null, OpponentFilter.forStats(opponentId), null, null, null, false, null);
	}

	public static MatchFilter forOpponent(int opponentId, Integer season, Range<LocalDate> dateRange, String level, Integer bestOf, String surface, Boolean indoor, Range<Integer> speedRange, String round, Integer tournamentId, String outcome, String score) {
		return new MatchFilter(season, dateRange, level, bestOf, surface, indoor, speedRange, round, null, tournamentId, null, OpponentFilter.forStats(opponentId), OutcomeFilter.forStats(outcome), ScoreFilter.forStats(score), null, false, null);
	}

	public static MatchFilter forSeason(int season) {
		return new MatchFilter(season, null, null, null, null, null, null, null, null, null, null, null, null, null, null, false, null);
	}

	public static MatchFilter forSurface(String surface) {
		return new MatchFilter(null, null, null, null, surface, null, null, null, null, null, null, null, null, null, null, false, null);
	}

	public static MatchFilter forSeasonAndSurface(int season, String surface) {
		return new MatchFilter(season, null, null, null, surface, null, null, null, null, null, null, null, null, null, null, false, null);
	}

	public static MatchFilter forMatches(Range<LocalDate> dateRange, String level, Integer bestOf, String surface, Boolean indoor, Range<Integer> speedRange, Integer tournamentId, String round, String searchPhrase) {
		return forMatches(null, dateRange, level, bestOf, surface, indoor, speedRange, round, null, tournamentId, null, null, null, null, null, false, searchPhrase);
	}

	public static MatchFilter forMatches(Integer season, Range<LocalDate> dateRange, String level, Integer bestOf, String surface, Boolean indoor, Range<Integer> speedRange, String round, String result, Integer tournamentId, Integer tournamentEventId, OpponentFilter opponentFilter, OutcomeFilter outcomeFilter, ScoreFilter scoreFilter, StatsFilter statsFilter, boolean bigWin, String searchPhrase) {
		return new MatchFilter(season, dateRange, level, bestOf, surface, indoor, speedRange, round, result, tournamentId, tournamentEventId, opponentFilter, outcomeFilter, scoreFilter, statsFilter, bigWin, searchPhrase);
	}

	// For Compare Stats
	public static MatchFilter forStats(Integer season, String level, String surface) {
		return forStats(season, null, level, null, surface, null, null, null, null, null, null, null, null, null, null, false, null);
	}

	// For Compare Match Stats
	public static MatchFilter forStats(Integer season, String level, String surface, String round, Integer opponentId) {
		return forStats(season, null, level, null, surface, null, null, round, null, null, null, OpponentFilter.forStats(opponentId), null, null, null, false, null);
	}

	// For H2H Compare Stats
	public static MatchFilter forStats(Integer season, String level, String surface, OpponentFilter opponentFilter) {
		return forStats(season, null, level, null, surface, null, null, null, null, null, null, opponentFilter, null, null, null, false, null);
	}

	// For Tournaments
	public static MatchFilter forStats(Integer season, Range<LocalDate> dateRange, String level, String surface, Boolean indoor, Range<Integer> speedRange, String result, Integer tournamentId, StatsFilter statsFilter, String searchPhrase) {
		return forStats(season, dateRange, level, null, surface, indoor, speedRange, null, result, tournamentId, null, null, null, null, statsFilter, false, searchPhrase);
	}

	// For Performance and Statistics
	public static MatchFilter forStats(Integer season, Range<LocalDate> dateRange, String level, Integer bestOf, String surface, Boolean indoor, Range<Integer> speedRange, String round, String result, Integer tournamentId, OpponentFilter opponentFilter) {
		return forStats(season, dateRange, level, bestOf, surface, indoor, speedRange, round, result, tournamentId, null, opponentFilter, null, null, null, false, null);
	}

	// For Matches
	public static MatchFilter forStats(Integer season, Range<LocalDate> dateRange, String level, Integer bestOf, String surface, Boolean indoor, Range<Integer> speedRange, String round, String result, Integer tournamentId, Integer tournamentEventId, OpponentFilter opponentFilter, OutcomeFilter outcomeFilter, ScoreFilter scoreFilter, StatsFilter statsFilter, boolean bigWin, String searchPhrase) {
		return new MatchFilter(season, dateRange, level, bestOf, surface, indoor, speedRange, round, result, tournamentId, tournamentEventId, opponentFilter, outcomeFilter, scoreFilter, statsFilter, bigWin, searchPhrase) {
			@Override protected String getBigWinCriterion() {
				return STATS_BIG_WIN_CRITERION;
			}
			@Override protected String getSearchCriterion() {
				return STATS_SEARCH_CRITERION;
			}
		};
	}


	// Instance

	private final Integer bestOf;
	private final String round;
	private final OpponentFilter opponentFilter;
	private final OutcomeFilter outcomeFilter;
	private final ScoreFilter scoreFilter;
	private final boolean bigWin;

	private static final String BEST_OF_CRITERION         = " AND m.best_of = :bestOf";
	private static final String ROUND_CRITERION           = " AND m.round %1$s :round::match_round";
	private static final String ENTRY_ROUND_CRITERION     = " AND m.round BETWEEN 'R128' AND 'R16'";
	private static final String MATCHES_SEARCH_CRITERION  = " AND (e.name ILIKE '%' || :searchPhrase || '%' OR pw.name ILIKE '%' || :searchPhrase || '%' OR pl.name ILIKE '%' || :searchPhrase || '%')";
	private static final String STATS_SEARCH_CRITERION    = " AND (e.name ILIKE '%' || :searchPhrase || '%' OR o.name ILIKE '%' || :searchPhrase || '%')";
	private static final String MATCHES_BIG_WIN_CRITERION = " AND m.winner_id = :playerId AND ((m.winner_rank <= 20 OR m.loser_rank <= 20) OR m.loser_elo_rating > 2000)";
	private static final String STATS_BIG_WIN_CRITERION   = " AND m.player_id = :playerId AND ((m.player_rank <= 20 OR m.opponent_rank <= 20) OR m.opponent_elo_rating > 2000) AND m.p_matches = 1";

	private MatchFilter(Integer season, Range<LocalDate> dateRange, String level, Integer bestOf, String surface, Boolean indoor, Range<Integer> speedRange, String round, String result, Integer tournamentId, Integer tournamentEventId, OpponentFilter opponentFilter, OutcomeFilter outcomeFilter, ScoreFilter scoreFilter, StatsFilter statsFilter, boolean bigWin, String searchPhrase) {
		super(season, dateRange, level, surface, indoor, speedRange, result, tournamentId, tournamentEventId, statsFilter, searchPhrase);
		this.bestOf = bestOf;
		this.round = round;
		this.opponentFilter = opponentFilter != null ? opponentFilter : OpponentFilter.ALL;
		this.outcomeFilter = outcomeFilter != null ? outcomeFilter : OutcomeFilter.ALL;
		this.scoreFilter = scoreFilter != null ? scoreFilter : ScoreFilter.ALL;
		this.bigWin = bigWin;
	}

	@Override protected void appendCriteria(StringBuilder criteria) {
		super.appendCriteria(criteria);
		if (bestOf != null)
			criteria.append(BEST_OF_CRITERION);
		if (!isNullOrEmpty(round))
			criteria.append(round.equals(Round.ENTRY.getCode()) ? ENTRY_ROUND_CRITERION : format(ROUND_CRITERION, round.endsWith("+") ? ">=" : "="));
		opponentFilter.appendCriteria(criteria);
		outcomeFilter.appendCriteria(criteria);
		scoreFilter.appendCriteria(criteria);
		if (bigWin)
			criteria.append(getBigWinCriterion());
	}

	@Override public void addParams(MapSqlParameterSource params) {
		super.addParams(params);
		if (bestOf != null)
			params.addValue("bestOf", bestOf);
		if (!isNullOrEmpty(round) && !round.equals(Round.ENTRY.getCode()))
			params.addValue("round", round.endsWith("+") ? round.substring(0, round.length() - 1) : round);
		opponentFilter.addParams(params);
		outcomeFilter.addParams(params);
		scoreFilter.addParams(params);
	}

	@Override protected String getPrefix() {
		return "m.";
	}

	protected String getBigWinCriterion() {
		return MATCHES_BIG_WIN_CRITERION;
	}

	@Override protected String getSearchCriterion() {
		return MATCHES_SEARCH_CRITERION;
	}

	@Override public boolean isEmpty() {
		return super.isEmpty() && bestOf == null && isNullOrEmpty(round) && opponentFilter.isEmpty() && outcomeFilter.isEmpty() && scoreFilter.isEmpty() && !bigWin;
	}

	public boolean isTournamentEventFilterEmpty() {
		return super.isEmpty();
	}

	public OpponentFilter getOpponentFilter() {
		return opponentFilter;
	}

	public boolean isBigWin() {
		return bigWin;
	}

	public boolean isForSeason() {
		Integer season = getSeason();
		return season != null && equals(forSeason(season));
	}

	public boolean isForSurface() {
		String surface = getSurface();
		return !isNullOrEmpty(surface) && surface.length() == 1 && equals(forSurface(surface));
	}

	public boolean isForSeasonAndSurface() {
		Integer season = getSeason();
		String surface = getSurface();
		return season != null && !isNullOrEmpty(surface) && surface.length() == 1 && equals(forSeasonAndSurface(season, surface));
	}


	// Object methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof MatchFilter)) return false;
		if (!super.equals(o)) return false;
		MatchFilter filter = (MatchFilter)o;
		return Objects.equals(bestOf, filter.bestOf) && stringsEqual(round, filter.round) && opponentFilter.equals(filter.opponentFilter) && outcomeFilter.equals(filter.outcomeFilter) && scoreFilter.equals(filter.scoreFilter) && bigWin == filter.bigWin;
	}

	@Override public int hashCode() {
		return Objects.hash(super.hashCode(), bestOf, emptyToNull(round), opponentFilter, outcomeFilter, scoreFilter, bigWin);
	}

	@Override protected ToStringHelper toStringHelper() {
		return super.toStringHelper()
			.add("bestOf", bestOf)
			.add("round", emptyToNull(round))
			.add("opponentFilter", opponentFilter)
			.add("outcomeFilter", outcomeFilter)
			.add("scoreFilter", scoreFilter)
			.add("bigWin", nullIf(bigWin, true));
	}
}
