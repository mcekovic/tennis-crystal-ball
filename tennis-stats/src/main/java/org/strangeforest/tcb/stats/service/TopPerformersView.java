package org.strangeforest.tcb.stats.service;

import java.util.*;

public class TopPerformersView {

	private static final Map<String, String> LEVEL_CATEGORY_MAP = Map.of(
		"G", "grandSlamMatches",
		"F", "tourFinalsMatches",
		"L", "altFinalsMatches",
		"M", "mastersMatches",
		"O", "olympicsMatches",
		"A", "atp500Matches",
		"B", "atp250Matches",
		"D", "davisCupMatches",
		"T", "teamCupsMatches"
	);
	private static final Map<String, String> TOURNAMENT_LEVEL_CATEGORY_MAP = Map.of(
		"G", "grandSlamMatches",
		"M", "mastersMatches",
		"A", "atp500Matches",
		"B", "atp250Matches"
	);
	private static final Map<Integer, String> BEST_OF_CATEGORY_MAP = Map.of(
		3, "bestOf3Matches",
		5, "bestOf5Matches"
	);
	private static final Map<String, String> SURFACE_CATEGORY_MAP = Map.of(
		"H", "hardMatches",
		"C", "clayMatches",
		"G", "grassMatches",
		"P", "carpetMatches"
	);
	private static final Map<Boolean, String> INDOOR_CATEGORY_MAP = Map.of(
		Boolean.FALSE, "outdoorMatches",
		Boolean.TRUE, "indoorMatches"
	);
	private static final Map<String, String> ROUND_CATEGORY_MAP = Map.of(
		"F", "finals"
	);
	private static final Map<Opponent, String> OPPOSITION_CATEGORY_MAP = Map.of(
		Opponent.NO_1, "vsNo1",
		Opponent.TOP_5, "vsTop5",
		Opponent.TOP_10, "vsTop10"
	);

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
				if (filter.isForBestOf())
					return optimizedAll(BEST_OF_CATEGORY_MAP.get(filter.getBestOf()));
				else if (filter.isForSurface())
					return optimizedAll(SURFACE_CATEGORY_MAP.get(filter.getSurface()));
				else if (filter.isForIndoor())
					return optimizedAll(INDOOR_CATEGORY_MAP.get(filter.getIndoor()));
				else if (filter.isForRound())
					return optimizedAll(ROUND_CATEGORY_MAP.get(filter.getRound()));
				else if (filter.isForOpposition())
					return optimizedAll(OPPOSITION_CATEGORY_MAP.get(filter.getOpponentFilter().getOpponent()));
				else if (filter.isForSeasonAndLevel())
					return optimizedSeason(LEVEL_CATEGORY_MAP.get(filter.getLevel()), filter.getSeason());
				else if (filter.isForSeasonAndBestOf())
					return optimizedSeason(BEST_OF_CATEGORY_MAP.get(filter.getBestOf()), filter.getSeason());
				else if (filter.isForSeasonAndSurface())
					return optimizedSeason(SURFACE_CATEGORY_MAP.get(filter.getSurface()), filter.getSeason());
				else if (filter.isForSeasonAndIndoor())
					return optimizedSeason(INDOOR_CATEGORY_MAP.get(filter.getIndoor()), filter.getSeason());
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
			case "altFinalsMatches": return deoptimizeTournamentLevel("L");
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
