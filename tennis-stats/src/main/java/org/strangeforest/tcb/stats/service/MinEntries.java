package org.strangeforest.tcb.stats.service;

import java.time.*;
import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import com.google.common.collect.*;

import static java.lang.Math.*;

@Component
public class MinEntries {

	@Autowired
	private TournamentService tournamentService;

	private static final int MIN_ENTRIES_SEASON_FACTOR =  10;
	private static final int MIN_ENTRIES_EVENT_FACTOR  = 100;
	private static final Map<String, Double> MIN_ENTRIES_LEVEL_WEIGHT_MAP = ImmutableMap.<String, Double>builder()
		.put("G", 0.25)
		.put("F", 0.05)
		.put("M", 0.25)
		.put("O", 0.025)
		.put("A", 0.25)
		.put("B", 0.25)
		.put("D", 0.1)
		.put("T", 0.025)
		.put("GFMO", 0.5)
		.put("AB", 0.5)
		.put("DT", 0.1)
	.build();
	private static final Map<String, Double> MIN_ENTRIES_SURFACE_WEIGHT_MAP = ImmutableMap.<String, Double>builder()
		.put("H", 0.5)
		.put("C", 0.5)
		.put("G", 0.25)
		.put("P", 0.25)
	.build();
	private static final Map<String, Double> MIN_ENTRIES_ROUND_WEIGHT_MAP = ImmutableMap.<String, Double>builder()
		.put("F", 0.1)
		.put("BR", 0.005)
		.put("BR+", 0.1)
		.put("SF", 0.1)
		.put("SF+", 0.15)
		.put("QF", 0.15)
		.put("QF+", 0.2)
		.put("R16", 0.25)
		.put("R16+", 0.5)
		.put("R32", 0.5)
		.put("R32+", 0.75)
		.put("R64", 0.25)
		.put("R128", 0.1)
		.put("RR", 0.05)
	.build();
	private static final Map<String, Double> MIN_ENTRIES_OPPONENT_WEIGHT_MAP = ImmutableMap.<String, Double>builder()
		.put("NO_1", 0.05)
		.put("TOP_5", 0.1)
		.put("TOP_10", 0.1)
		.put("TOP_20", 0.25)
		.put("TOP_50", 0.5)
		.put("TOP_100", 1.0)
		.put("UNDER_18", 0.05)
		.put("UNDER_21", 0.2)
		.put("UNDER_25", 0.5)
		.put("OVER_25", 0.5)
		.put("OVER_30", 0.2)
		.put("OVER_35", 0.05)
		.put("RIGHT_HANDED", 1.0)
		.put("LEFT_HANDED", 0.2)
		.put("BACKHAND_2", 0.1)
		.put("BACKHAND_1", 0.1)
		.put("SEEDED", 0.5)
		.put("UNSEEDED", 0.5)
		.put("QUALIFIER", 0.1)
		.put("WILD_CARD", 0.05)
		.put("LUCKY_LOSER", 0.05)
		.put("PROTECTED_RANKING", 0.005)
		.put("SPECIAL_EXEMPT", 0.005)
		.build();
	private static final Map<Range<Integer>, Double> MIN_ENTRIES_TOURNAMENT_FACTOR_MAP = ImmutableMap.<Range<Integer>, Double>builder()
		.put(Range.closed(1, 2), 100.0)
		.put(Range.closed(3, 5), 50.0)
		.put(Range.closed(6, 9), 25.0)
		.put(Range.atLeast(10), 20.0)
	.build();
	private static final int MIN_ENTRIES_COUNTRY_FACTOR = 10;

	public int getFilteredMinEntries(int minEntries, PerfStatsFilter filter) {
		if (filter.hasSeason()) {
			minEntries /= MIN_ENTRIES_SEASON_FACTOR;
			LocalDate today = LocalDate.now();
			if (filter.getSeason() == today.getYear() && today.getMonth().compareTo(Month.SEPTEMBER) <= 0)
				minEntries /= 12.0 / today.getMonth().getValue();
		}
		if (filter.hasLevel())
			minEntries *= getMinEntriesWeight(filter.getLevel(), MIN_ENTRIES_LEVEL_WEIGHT_MAP);
		if (filter.hasSurface())
			minEntries *= getMinEntriesSummedWeight(filter.getSurface(), MIN_ENTRIES_SURFACE_WEIGHT_MAP);
		if (filter.hasRound())
			minEntries *= getMinEntriesWeight(filter.getRound(), MIN_ENTRIES_ROUND_WEIGHT_MAP);
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

	private double getMinEntriesWeight(String item, Map<String, Double> weightMap) {
		return weightMap.getOrDefault(item, 1.0);
	}

	private double getMinEntriesSummedWeight(String items, Map<String, Double> weightMap) {
		return min(items.chars().mapToObj(i -> (char)i).mapToDouble(c -> weightMap.getOrDefault(c.toString(), 0.0)).sum(), 1.0);
	}

	private double getMinEntriesTournamentFactor(int tournamentId) {
		int eventCount = tournamentService.getTournamentEventCount(tournamentId);
		return MIN_ENTRIES_TOURNAMENT_FACTOR_MAP.entrySet().stream().filter(entry -> entry.getKey().contains(eventCount)).findFirst().get().getValue();
	}
}
