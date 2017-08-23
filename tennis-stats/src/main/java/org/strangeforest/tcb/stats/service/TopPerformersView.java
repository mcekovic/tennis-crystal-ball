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
	private static final Map<String, String> TOURNAMENT_LEVEL_CATEGORY_MAP = ImmutableMap.<String, String>builder()
		.put("G", "grandSlamMatches")
		.put("M", "mastersMatches")
		.put("A", "atp500Matches")
		.put("B", "atp250Matches")
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
		switch (category) {
			case "matches": {
				if (filter.isForLevel())
					return optimizedAll(LEVEL_CATEGORY_MAP.get(filter.getLevel()));
				else if (filter.isForSurface())
					return optimizedAll(SURFACE_CATEGORY_MAP.get(filter.getSurface()));
				else if (filter.isForRound())
					return optimizedAll(ROUND_CATEGORY_MAP.get(filter.getRound()));
				else if (filter.isForOpposition())
					return optimizedAll(OPPOSITION_CATEGORY_MAP.get(filter.getOpponentFilter().getOpponent()));
				else if (filter.isForSeasonAndLevel())
					return optimizedSeason(LEVEL_CATEGORY_MAP.get(filter.getLevel()), filter.getSeason());
				else if (filter.isForSeasonAndSurface())
					return optimizedSeason(SURFACE_CATEGORY_MAP.get(filter.getSurface()), filter.getSeason());
				else if (filter.isForSeasonAndRound())
					return optimizedSeason(ROUND_CATEGORY_MAP.get(filter.getRound()), filter.getSeason());
				else if (filter.isForSeasonAndOpposition())
					return optimizedSeason(OPPOSITION_CATEGORY_MAP.get(filter.getOpponentFilter().getOpponent()), filter.getSeason());
				else if (filter.isForLevelAndTournament())
					return optimizedTournament(TOURNAMENT_LEVEL_CATEGORY_MAP.get(filter.getLevel()), filter.getTournamentId());
				else if (filter.isForRoundAndTournament())
					return optimizedTournament(ROUND_CATEGORY_MAP.get(filter.getRound()), filter.getTournamentId());
				else if (filter.isForOppositionAndTournament())
					return optimizedTournament(OPPOSITION_CATEGORY_MAP.get(filter.getOpponentFilter().getOpponent()), filter.getTournamentId());
				break;
			}
			case "tourFinalsMatches": return deoptimizeTournamentLevel("F");
			case "olympicsMatches": return deoptimizeTournamentLevel("O");
			case "davisCupMatches": return deoptimizeTournamentLevel("D");
			case "hardMatches": return deoptimizeTournamentSurface("H");
			case "clayMatches": return deoptimizeTournamentSurface("C");
			case "grassMatches": return deoptimizeTournamentSurface("G");
			case "carpetMatches": return deoptimizeTournamentSurface("P");
		}
		return this;
	}

	private TopPerformersView optimizedAll(String category) {
		return category != null ? new TopPerformersView(category, PerfStatsFilter.ALL) : this;
	}

	private TopPerformersView optimizedSeason(String category, Integer season) {
		return category != null ? new TopPerformersView(category, PerfStatsFilter.forSeason(season)) : this;
	}

	private TopPerformersView optimizedTournament(String category, Integer tournamentId) {
		return category != null ? new TopPerformersView(category, PerfStatsFilter.forTournament(tournamentId)) : this;
	}

	private TopPerformersView deoptimizeTournamentLevel(String level) {
		return filter.isForTournament() ? new TopPerformersView("matches", PerfStatsFilter.forLevelAndTournament(level, filter.getTournamentId())) : this;
	}

	private TopPerformersView deoptimizeTournamentSurface(String surface) {
		return filter.isForTournament() ? new TopPerformersView("matches", PerfStatsFilter.forSurfaceAndTournament(surface, filter.getTournamentId())) : this;
	}
}
