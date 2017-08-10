package org.strangeforest.tcb.stats.service;

import java.time.*;
import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import com.google.common.collect.*;

@Component
public class MinEntries {

	@Autowired
	private TournamentService tournamentService;

	private static final int MIN_ENTRIES_SEASON_FACTOR  =    10;
	private static final int MIN_ENTRIES_EVENT_FACTOR   =   100;
	private static final Map<String, Double> MIN_ENTRIES_LEVEL_WEIGHT_MAP = ImmutableMap.<String, Double>builder()
		.put("G", 0.15)
		.put("F", 0.01)
		.put("M", 0.15)
		.put("O", 0.01)
		.put("A", 0.15)
		.put("B", 0.5)
		.put("D", 0.1)
		.put("T", 0.01)
	.build();
	private static final Map<String, Double> MIN_ENTRIES_SURFACE_WEIGHT_MAP = ImmutableMap.<String, Double>builder()
		.put("H", 0.4)
		.put("C", 0.35)
		.put("G", 0.1)
		.put("P", 0.15)
	.build();
	private static final Map<Range<Integer>, Double> MIN_ENTRIES_TOURNAMENT_FACTOR_MAP = ImmutableMap.<Range<Integer>, Double>builder()
		.put(Range.closed(1, 2), 100.0)
		.put(Range.closed(3, 5), 50.0)
		.put(Range.closed(6, 9), 25.0)
		.put(Range.atLeast(10), 20.0)
	.build();


	public int getFilteredMinEntries(int minEntries, StatsPerfFilter filter) {
		if (filter.hasSeason()) {
			minEntries /= MIN_ENTRIES_SEASON_FACTOR;
			LocalDate today = LocalDate.now();
			if (filter.getSeason() == today.getYear() && today.getMonth().compareTo(Month.SEPTEMBER) <= 0)
				minEntries /= 12.0 / today.getMonth().getValue();
		}
		if (filter.hasLevel())
			minEntries *= getMinEntriesWeight(filter.getLevel(), MIN_ENTRIES_LEVEL_WEIGHT_MAP);
		if (filter.hasSurface())
			minEntries *= getMinEntriesWeight(filter.getSurface(), MIN_ENTRIES_SURFACE_WEIGHT_MAP);
		if (filter.hasTournamentEvent())
			minEntries /= MIN_ENTRIES_EVENT_FACTOR;
		else if (filter.hasTournament())
			minEntries /= getMinEntriesTournamentFactor(filter.getTournamentId());
		return Math.max(minEntries, 2);
	}

	private double getMinEntriesWeight(String items, Map<String, Double> weightMap) {
		return items.chars().mapToObj(i -> (char)i).mapToDouble(c -> weightMap.getOrDefault(c.toString(), 0.0)).sum();
	}

	private double getMinEntriesTournamentFactor(int tournamentId) {
		int eventCount = tournamentService.getTournamentEventCount(tournamentId);
		return MIN_ENTRIES_TOURNAMENT_FACTOR_MAP.entrySet().stream().filter(entry -> entry.getKey().contains(eventCount)).findFirst().get().getValue();
	}
}
