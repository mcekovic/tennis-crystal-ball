package org.strangeforest.tcb.stats.service;

import java.time.*;
import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.core.*;

import com.google.common.collect.*;

import static java.lang.Math.*;
import static java.util.Map.*;
import static org.strangeforest.tcb.util.RangeUtil.*;

@Service
public class MinEntries {

	@Autowired private DataService dataService;
	@Autowired private TournamentService tournamentService;

	public static final Range<LocalDate> EMPTY_DATE_RANGE = Range.closed(LocalDate.of(1900, 1, 1), LocalDate.of(1900, 1, 1));

	private static final int MIN_ENTRIES_SEASON_FACTOR =  10;
	private static final int MIN_ENTRIES_MONTH_FACTOR  =  10;
	private static final int MIN_ENTRIES_EVENT_FACTOR  = 100;
	private static final Map<String, Double> MIN_ENTRIES_LEVEL_WEIGHT_MAP = Map.ofEntries(
		entry("G",      0.25),
		entry("F",      0.05),
		entry("L",      0.05),
		entry("M",      0.25),
		entry("O",      0.025),
		entry("A",      0.25),
		entry("B",      0.25),
		entry("D",      0.1),
		entry("T",      0.025),
		entry("GFLMO",  0.5),
		entry("FL",     0.05),
		entry("MO",     0.25),
		entry("AB",     0.5),
		entry("DT",     0.1),
		entry("GLD",    0.25),
		entry("FMOABT", 0.75)
	);
	private static final Map<Integer, Double> MIN_ENTRIES_BEST_OF_WEIGHT_MAP = Map.of(
		3, 0.75,
		5, 0.25
	);
	private static final Map<String, Double> MIN_ENTRIES_SURFACE_WEIGHT_MAP = Map.of(
		"H", 0.5,
		"C", 0.5,
		"G", 0.25,
		"P", 0.25
	);
	private static final Map<Boolean, Double> MIN_ENTRIES_INDOOR_WEIGHT_MAP = Map.of(
		Boolean.FALSE, 0.75,
		Boolean.TRUE,  0.25
	);
	private static final Map<CourtSpeed, Double> MIN_ENTRIES_SPEED_WEIGHT_MAP = Map.ofEntries(
		entry(CourtSpeed.VERY_FAST, 0.05),
		entry(CourtSpeed.FAST, 0.075),
		entry(CourtSpeed.MEDIUM_FAST, 0.125),
		entry(CourtSpeed.MEDIUM, 0.15),
		entry(CourtSpeed.MEDIUM_SLOW, 0.125),
		entry(CourtSpeed.SLOW, 0.075),
		entry(CourtSpeed.VERY_SLOW, 0.05),
		entry(CourtSpeed.GE_FAST, 0.125),
		entry(CourtSpeed.GE_MEDIUM_FAST, 0.25),
		entry(CourtSpeed.GE_MEDIUM, 0.50),
		entry(CourtSpeed.GE_MEDIUM_SLOW, 0.75),
		entry(CourtSpeed.GE_SLOW, 1.0),
		entry(CourtSpeed.LE_FAST, 1.0),
		entry(CourtSpeed.LE_MEDIUM_FAST, 0.75),
		entry(CourtSpeed.LE_MEDIUM, 0.50),
		entry(CourtSpeed.LE_MEDIUM_SLOW, 0.25),
		entry(CourtSpeed.LE_SLOW, 0.125)
	);
	private static final Map<String, Double> MIN_ENTRIES_ROUND_WEIGHT_MAP = Map.ofEntries(
		entry("F",    0.1),
		entry("BR",   0.005),
		entry("BR+",  0.1),
		entry("SF",   0.1),
		entry("SF+",  0.15),
		entry("QF",   0.15),
		entry("QF+",  0.2),
		entry("R16",  0.25),
		entry("R16+", 0.5),
		entry("R32",  0.5),
		entry("R32+", 0.75),
		entry("R64",  0.25),
		entry("R128", 0.1),
		entry("ENT",  0.75),
		entry("RR",   0.05)
	);
	private static final Map<String, Double> MIN_ENTRIES_RESULT_WEIGHT_MAP = ImmutableMap.<String, Double>builder()
		.put("W",    0.05)
		.putAll(MIN_ENTRIES_ROUND_WEIGHT_MAP)
	.build();
	private static final Map<String, Double> MIN_ENTRIES_OPPONENT_WEIGHT_MAP = Map.ofEntries(
		// Rank
		entry(Opponent.NO_1.name(), 0.05),
		entry(Opponent.TOP_5.name(), 0.1),
		entry(Opponent.TOP_10.name(), 0.1),
		entry(Opponent.TOP_20.name(), 0.25),
		entry(Opponent.TOP_50.name(), 0.5),
		entry(Opponent.TOP_100.name(), 1.0),
		entry(Opponent.OVER_100.name(), 0.1),
		entry(Opponent.HIGHER_RANKED.name(), 0.25),
		entry(Opponent.LOWER_RANKED.name(), 0.25),
		// Elo Rating
		entry(Opponent.ELO_2400.name(), 0.025),
		entry(Opponent.ELO_2200.name(), 0.1),
		entry(Opponent.ELO_2000.name(), 0.5),
		entry(Opponent.ELO_1800.name(), 1.0),
		entry(Opponent.HIGHER_ELO.name(), 0.25),
		entry(Opponent.LOWER_ELO.name(), 0.25),
		// Age
		entry(Opponent.UNDER_18.name(), 0.05),
		entry(Opponent.UNDER_21.name(), 0.2),
		entry(Opponent.UNDER_25.name(), 0.5),
		entry(Opponent.OVER_25.name(), 0.5),
		entry(Opponent.OVER_30.name(), 0.2),
		entry(Opponent.OVER_35.name(), 0.05),
		entry(Opponent.YOUNGER.name(), 0.25),
		entry(Opponent.OLDER.name(), 0.25),
		// Playing style
		entry(Opponent.RIGHT_HANDED.name(), 1.0),
		entry(Opponent.LEFT_HANDED.name(), 0.2),
		entry(Opponent.BACKHAND_2.name(), 0.1),
		entry(Opponent.BACKHAND_1.name(), 0.1),
		// Seeding
		entry(Opponent.SEEDED.name(), 0.5),
		entry(Opponent.UNSEEDED.name(), 0.5),
		entry(Opponent.QUALIFIER.name(), 0.1),
		entry(Opponent.WILD_CARD.name(), 0.05),
		entry(Opponent.LUCKY_LOSER.name(), 0.05),
		entry(Opponent.PROTECTED_RANKING.name(), 0.005),
		entry(Opponent.SPECIAL_EXEMPT.name(), 0.005),
		// Height
		entry(Opponent.UNDER_5_10.name(), 0.25),
		entry(Opponent.UNDER_6_0.name(), 0.5),
		entry(Opponent.OVER_6_2.name(), 0.5),
		entry(Opponent.OVER_6_4.name(), 0.25),
		entry(Opponent.SHORTER.name(), 0.25),
		entry(Opponent.TALLER.name(), 0.25)
	);
	private static final Map<Range<Integer>, Double> MIN_ENTRIES_TOURNAMENT_FACTOR_MAP = Map.of(
		Range.atMost(2), 100.0,
		Range.closed(3, 5), 50.0,
		Range.closed(6, 9), 25.0,
		Range.atLeast(10), 20.0
	);
	private static final int MIN_ENTRIES_COUNTRY_FACTOR = 10;

	public int getFilteredMinEntries(int minEntries, PerfStatsFilter filter) {
		var today = LocalDate.now();
		var dateRange = Range.closed(LocalDate.of(dataService.getFirstSeason(), 1, 1), today);
		if (filter.hasSeason()) {
			var season = filter.getSeason();
			dateRange = intersection(dateRange, Range.closed(LocalDate.of(season, 1, 1), LocalDate.of(season, 12, 31)), EMPTY_DATE_RANGE);
		}
		if (filter.isLast52Weeks())
			dateRange = intersection(dateRange, Range.closed(today.minusYears(1), today), EMPTY_DATE_RANGE);
		if (filter.hasDateRange())
			dateRange = intersection(dateRange, filter.getDateRange(), EMPTY_DATE_RANGE);
		minEntries *= getMinEntriesWeight(Period.between(dateRange.lowerEndpoint(), dateRange.upperEndpoint()));

		if (filter.hasLevel())
			minEntries *= getMinEntriesWeight(filter.getLevel(), MIN_ENTRIES_LEVEL_WEIGHT_MAP);
		else if (filter.hasBestOf())
			minEntries *= getMinEntriesWeight(filter.getBestOf(), MIN_ENTRIES_BEST_OF_WEIGHT_MAP);

		if (filter.hasSurface())
			minEntries *= getMinEntriesSummedWeight(filter.getSurface(), MIN_ENTRIES_SURFACE_WEIGHT_MAP);
		if (filter.hasIndoor())
			minEntries *= getMinEntriesWeight(filter.getIndoor(), MIN_ENTRIES_INDOOR_WEIGHT_MAP);
		if (filter.hasSpeedRange())
			minEntries *= getMinEntriesWeight(CourtSpeed.forSpeedRange(filter.getSpeedRange()), MIN_ENTRIES_SPEED_WEIGHT_MAP);

		if (filter.hasRound())
			minEntries *= getMinEntriesWeight(filter.getRound(), MIN_ENTRIES_ROUND_WEIGHT_MAP);
		else if (filter.hasResult())
			minEntries *= getMinEntriesWeight(filter.getResult(), MIN_ENTRIES_RESULT_WEIGHT_MAP);

		if (filter.hasTournamentEvent())
			minEntries /= MIN_ENTRIES_EVENT_FACTOR;
		else if (filter.hasTournament())
			minEntries /= getMinEntriesTournamentFactor(filter.getTournamentId());
		
		if (filter.hasOpponent()) {
			var opponentFilter = filter.getOpponentFilter();
			if (opponentFilter.hasOpponent())
				minEntries *= getMinEntriesWeight(opponentFilter.getOpponent().name(), MIN_ENTRIES_OPPONENT_WEIGHT_MAP);
			if (opponentFilter.hasCountries())
				minEntries /= MIN_ENTRIES_COUNTRY_FACTOR;
		}
		return max(minEntries, 2);
	}

	private <I> double getMinEntriesWeight(I item, Map<I, Double> weightMap) {
		return weightMap.getOrDefault(item, 1.0);
	}

	private double getMinEntriesSummedWeight(String items, Map<String, Double> weightMap) {
		return min(items.chars().mapToObj(i -> (char)i).mapToDouble(c -> weightMap.getOrDefault(c.toString(), 0.0)).sum(), 1.0);
	}

	private static double getMinEntriesWeight(Period period) {
		var years = period.getYears();
		if (years == 0) {
			var months = period.getMonths();
			if (months == 0)
				months = 1;
			if (months < 10)
				return ((double)months) / (MIN_ENTRIES_SEASON_FACTOR * MIN_ENTRIES_MONTH_FACTOR);
			else
				return 1.0 / MIN_ENTRIES_SEASON_FACTOR;
		}
		else if (years < MIN_ENTRIES_SEASON_FACTOR)
			return Math.round(20.0 * years / MIN_ENTRIES_SEASON_FACTOR) / 20.0;
		else
			return 1.0;
	}

	private double getMinEntriesTournamentFactor(int tournamentId) {
		var eventCount = tournamentService.getTournamentEventCount(tournamentId);
		return MIN_ENTRIES_TOURNAMENT_FACTOR_MAP.entrySet().stream().filter(entry -> entry.getKey().contains(eventCount)).findFirst().orElseThrow().getValue();
	}
}
