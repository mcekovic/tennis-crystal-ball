package org.strangeforest.tcb.stats.service;

import java.util.*;

import com.google.common.collect.*;

public class TopPerformersView {

	private static final Map<String, String> LEVEL_CATEGORY_MAP = ImmutableMap.<String, String>builder()
		.put("G", "grandSlamMatches")
		.put("F", "tourFinalsMatches")
		.put("M", "mastersMatches")
		.put("O", "olympicsMatches")
		.put("A", "atp500Matches")
		.put("B", "atp250Matches")
		.put("D", "davisCupMatches")
	.build();
	private static final Map<String, String> SURFACE_CATEGORY_MAP = ImmutableMap.<String, String>builder()
		.put("H", "hardMatches")
		.put("C", "clayMatches")
		.put("G", "grassMatches")
		.put("P", "carpetMatches")
	.build();
	private static final Map<String, String> ROUND_CATEGORY_MAP = ImmutableMap.<String, String>builder()
		.put("F", "finals")
	.build();
	private static final Map<Opponent, String> OPPOSITION_CATEGORY_MAP = ImmutableMap.<Opponent, String>builder()
		.put(Opponent.NO_1, "vsNo1")
		.put(Opponent.TOP_5, "vsTop5")
		.put(Opponent.TOP_10, "vsTop10")
	.build();

	private final String category;
	private final PerfStatsFilter filter;

	public TopPerformersView(String category, PerfStatsFilter filter) {
		this.category = category;
		this.filter = filter;
	}

	public String getCategory() {
		return category;
	}

	public PerfStatsFilter getFilter() {
		return filter;
	}

	public TopPerformersView optimize() {
		if (category.equals("matches")) {
			if (filter.isForLevel())
				return optimizedAll(LEVEL_CATEGORY_MAP.get(filter.getLevel()));
			else if (filter.isForSurface())
				return optimizedAll(SURFACE_CATEGORY_MAP.get(filter.getSurface()));
			else if (filter.isForRound())
				return optimizedAll(ROUND_CATEGORY_MAP.get(filter.getRound()));
			else if (filter.isForOpposition())
				return optimizedAll(OPPOSITION_CATEGORY_MAP.get(filter.getOpponentFilter().getOpponent()));
			if (filter.isForSeasonAndLevel())
				return optimizedSeason(LEVEL_CATEGORY_MAP.get(filter.getLevel()), filter.getSeason());
			else if (filter.isForSeasonAndSurface())
				return optimizedSeason(SURFACE_CATEGORY_MAP.get(filter.getSurface()), filter.getSeason());
			else if (filter.isForSeasonAndRound())
				return optimizedSeason(ROUND_CATEGORY_MAP.get(filter.getRound()), filter.getSeason());
			else if (filter.isForSeasonAndOpposition())
				return optimizedSeason(OPPOSITION_CATEGORY_MAP.get(filter.getOpponentFilter().getOpponent()), filter.getSeason());
		}
		return this;
	}

	private TopPerformersView optimizedAll(String category) {
		return category != null ? new TopPerformersView(category, PerfStatsFilter.ALL) : this;
	}

	private TopPerformersView optimizedSeason(String category, Integer season) {
		return category != null ? new TopPerformersView(category, PerfStatsFilter.forSeason(season)) : this;
	}
}
