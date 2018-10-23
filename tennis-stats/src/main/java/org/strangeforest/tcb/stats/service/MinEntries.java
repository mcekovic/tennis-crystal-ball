package org.strangeforest.tcb.stats.service;

import java.time.*;
import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.core.*;

import com.google.common.collect.*;

import static java.lang.Math.*;
import static org.strangeforest.tcb.util.RangeUtil.*;

@Service
public class MinEntries {

	@Autowired private DataService dataService;
	@Autowired private TournamentService tournamentService;

	public static final Range<LocalDate> EMPTY_DATE_RANGE = Range.closed(LocalDate.of(1900, 1, 1), LocalDate.of(1900, 1, 1));

	private static final int MIN_ENTRIES_SEASON_FACTOR =  10;
	private static final int MIN_ENTRIES_MONTH_FACTOR  =  10;
	private static final int MIN_ENTRIES_EVENT_FACTOR  = 100;
	private static final Map<String, Double> MIN_ENTRIES_LEVEL_WEIGHT_MAP = ImmutableMap.<String, Double>builder()
		.put("G",      0.25)
		.put("F",      0.05)
		.put("L",      0.05)
		.put("M",      0.25)
		.put("O",      0.025)
		.put("A",      0.25)
		.put("B",      0.25)
		.put("D",      0.1)
		.put("T",      0.025)
		.put("GFLMO",  0.5)
		.put("FL",     0.05)
		.put("MO",     0.25)
		.put("AB",     0.5)
		.put("DT",     0.1)
		.put("GLD",    0.25)
		.put("FMOABT", 0.75)
	.build();
	private static final Map<Integer, Double> MIN_ENTRIES_BEST_OF_WEIGHT_MAP = ImmutableMap.<Integer, Double>builder()
		.put(3, 0.75)
		.put(5, 0.25)
	.build();
	private static final Map<String, Double> MIN_ENTRIES_SURFACE_WEIGHT_MAP = ImmutableMap.<String, Double>builder()
		.put("H", 0.5)
		.put("C", 0.5)
		.put("G", 0.25)
		.put("P", 0.25)
	.build();
	private static final Map<Boolean, Double> MIN_ENTRIES_INDOOR_WEIGHT_MAP = ImmutableMap.<Boolean, Double>builder()
		.put(Boolean.FALSE, 0.75)
		.put(Boolean.TRUE,  0.25)
	.build();
	private static final Map<CourtSpeed, Double> MIN_ENTRIES_SPEED_WEIGHT_MAP = ImmutableMap.<CourtSpeed, Double>builder()
		.put(CourtSpeed.VERY_FAST, 0.015)
		.put(CourtSpeed.FAST, 0.075)
		.put(CourtSpeed.MEDIUM_FAST, 0.15)
		.put(CourtSpeed.MEDIUM, 0.25)
		.put(CourtSpeed.MEDIUM_SLOW, 0.15)
		.put(CourtSpeed.SLOW, 0.075)
		.put(CourtSpeed.VERY_SLOW, 0.015)
	.build();
	private static final Map<String, Double> MIN_ENTRIES_ROUND_WEIGHT_MAP = ImmutableMap.<String, Double>builder()
		.put("F",    0.1)
		.put("BR",   0.005)
		.put("BR+",  0.1)
		.put("SF",   0.1)
		.put("SF+",  0.15)
		.put("QF",   0.15)
		.put("QF+",  0.2)
		.put("R16",  0.25)
		.put("R16+", 0.5)
		.put("R32",  0.5)
		.put("R32+", 0.75)
		.put("R64",  0.25)
		.put("R128", 0.1)
		.put("ENT",  0.75)
		.put("RR",   0.05)
	.build();
	private static final Map<String, Double> MIN_ENTRIES_RESULT_WEIGHT_MAP = ImmutableMap.<String, Double>builder()
		.put("W",    0.05)
		.putAll(MIN_ENTRIES_ROUND_WEIGHT_MAP)
	.build();
	private static final Map<String, Double> MIN_ENTRIES_OPPONENT_WEIGHT_MAP = ImmutableMap.<String, Double>builder()
		// Rank
		.put(Opponent.NO_1.name(), 0.05)
		.put(Opponent.TOP_5.name(), 0.1)
		.put(Opponent.TOP_10.name(), 0.1)
		.put(Opponent.TOP_20.name(), 0.25)
		.put(Opponent.TOP_50.name(), 0.5)
		.put(Opponent.TOP_100.name(), 1.0)
		.put(Opponent.OVER_100.name(), 0.1)
		.put(Opponent.HIGHER_RANKED.name(), 0.25)
		.put(Opponent.LOWER_RANKED.name(), 0.25)
		// Elo Rating
		.put(Opponent.ELO_2400.name(), 0.025)
		.put(Opponent.ELO_2200.name(), 0.1)
		.put(Opponent.ELO_2000.name(), 0.5)
		.put(Opponent.ELO_1800.name(), 1.0)
		.put(Opponent.HIGHER_ELO.name(), 0.25)
		.put(Opponent.LOWER_ELO.name(), 0.25)
		// Age
		.put(Opponent.UNDER_18.name(), 0.05)
		.put(Opponent.UNDER_21.name(), 0.2)
		.put(Opponent.UNDER_25.name(), 0.5)
		.put(Opponent.OVER_25.name(), 0.5)
		.put(Opponent.OVER_30.name(), 0.2)
		.put(Opponent.OVER_35.name(), 0.05)
		.put(Opponent.YOUNGER.name(), 0.25)
		.put(Opponent.OLDER.name(), 0.25)
		// Playing style
		.put(Opponent.RIGHT_HANDED.name(), 1.0)
		.put(Opponent.LEFT_HANDED.name(), 0.2)
		.put(Opponent.BACKHAND_2.name(), 0.1)
		.put(Opponent.BACKHAND_1.name(), 0.1)
		// Seeding
		.put(Opponent.SEEDED.name(), 0.5)
		.put(Opponent.UNSEEDED.name(), 0.5)
		.put(Opponent.QUALIFIER.name(), 0.1)
		.put(Opponent.WILD_CARD.name(), 0.05)
		.put(Opponent.LUCKY_LOSER.name(), 0.05)
		.put(Opponent.PROTECTED_RANKING.name(), 0.005)
		.put(Opponent.SPECIAL_EXEMPT.name(), 0.005)
		// Height
		.put(Opponent.UNDER_5_10.name(), 0.25)
		.put(Opponent.UNDER_6_0.name(), 0.5)
		.put(Opponent.OVER_6_2.name(), 0.5)
		.put(Opponent.OVER_6_4.name(), 0.25)
		.put(Opponent.SHORTER.name(), 0.25)
		.put(Opponent.TALLER.name(), 0.25)
	.build();
	private static final Map<Range<Integer>, Double> MIN_ENTRIES_TOURNAMENT_FACTOR_MAP = ImmutableMap.<Range<Integer>, Double>builder()
		.put(Range.atMost(2), 100.0)
		.put(Range.closed(3, 5), 50.0)
		.put(Range.closed(6, 9), 25.0)
		.put(Range.atLeast(10), 20.0)
	.build();
	private static final int MIN_ENTRIES_COUNTRY_FACTOR = 10;

	public int getFilteredMinEntries(int minEntries, PerfStatsFilter filter) {
		LocalDate today = LocalDate.now();
		Range<LocalDate> dateRange = Range.closed(LocalDate.of(dataService.getFirstSeason(), 1, 1), today);
		if (filter.hasSeason()) {
			Integer season = filter.getSeason();
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
			OpponentFilter opponentFilter = filter.getOpponentFilter();
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
		int years = period.getYears();
		if (years == 0) {
			int months = period.getMonths();
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
		int eventCount = tournamentService.getTournamentEventCount(tournamentId);
		return MIN_ENTRIES_TOURNAMENT_FACTOR_MAP.entrySet().stream().filter(entry -> entry.getKey().contains(eventCount)).findFirst().get().getValue();
	}
}
