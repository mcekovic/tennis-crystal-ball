package org.strangeforest.tcb.stats.model;

import java.util.*;

import org.strangeforest.tcb.stats.model.core.*;

import static java.util.Collections.*;
import static java.util.Comparator.reverseOrder;

public class TournamentLevelTimeline {

	private final SortedMap<Integer, List<TournamentItem>> seasonsTournaments = new TreeMap<>(reverseOrder());
	private final Map<Integer, List<TournamentLevelTimelineItem>> seasonsEvents = new HashMap<>();
	private final Map<String, Map<Integer, Integer>> playerWins = new HashMap<>();

	public Set<Integer> getSeasons() {
		return seasonsTournaments.keySet();
	}

	public boolean areSameTournaments(int season) {
		var nextSeason = getNextSeason(season);
		return nextSeason.isPresent() && startsWith(seasonsTournaments.get(nextSeason.get()), seasonsTournaments.get(season));
	}

	public List<TournamentItem> getTournaments(int season) {
		return seasonsTournaments.get(season);
	}

	public List<TournamentLevelTimelineItem> getSeasonEvents(int season) {
		return seasonsEvents.get(season);
	}

	public int getWins(String level, int playerId) {
		return playerWins.getOrDefault(level, emptyMap()).get(playerId);
	}

	public void addItem(TournamentLevelTimelineItem item) {
		addSeasonTournament(item);
		addSeasonEvent(item);
		updatePlayerWins(item);
	}

	private void addSeasonTournament(TournamentLevelTimelineItem item) {
		var tournamentItem = new TournamentItem(item.getTournamentId(), item.getName(), item.getLevel());
		seasonsTournaments.computeIfAbsent(item.getSeason(), s -> new ArrayList<>()).add(tournamentItem);
	}

	private void addSeasonEvent(TournamentLevelTimelineItem item) {
		seasonsEvents.computeIfAbsent(item.getSeason(), s -> new ArrayList<>()).add(item);
	}

	private void updatePlayerWins(TournamentLevelTimelineItem item) {
		PlayerRow winner = item.getWinner();
		if (winner == null || "ABD".equals(item.getOutcome()))
			return;
		item.setPlayerWins(playerWins.computeIfAbsent(item.getLevel(), l -> new HashMap<>()).compute(winner.getPlayerId(), (p, w) -> w != null ? w + 1 : 1));
	}

	public void addMissingSeasonLastTournaments() {
		for (int season : getSeasons()) {
			var optionalPrevSeason = getPrevSeason(season);
			if (optionalPrevSeason.isEmpty())
				continue;
			int prevSeason = optionalPrevSeason.get();
			var seasonTournaments = seasonsTournaments.get(season);
			var prevSeasonTournaments = seasonsTournaments.get(prevSeason);
			if (startsWith(prevSeasonTournaments, seasonTournaments)) {
				var prevSeasonEvents = seasonsEvents.get(prevSeason);
				for (var index = seasonTournaments.size(); index < prevSeasonEvents.size(); index++) {
					var item = prevSeasonEvents.get(index);
					addItem(new TournamentLevelTimelineItem(item.getTournamentId(), item.getName(), season, 0, null, item.getLevel(), null));
				}
			}
		}
	}


	// Util

	private Optional<Integer> getPrevSeason(int season) {
		if (season == getFirstSeason())
			return Optional.empty();
		else {
			for (var prevSeason = season - 1; true; prevSeason--) {
				if (hasSeason(prevSeason))
					return Optional.of(prevSeason);
			}
		}
	}

	private Optional<Integer> getNextSeason(int season) {
		if (season == getLastSeason())
			return Optional.empty();
		else {
			for (var nextSeason = season + 1; true; nextSeason++) {
				if (hasSeason(nextSeason))
					return Optional.of(nextSeason);
			}
		}
	}

	private Integer getFirstSeason() {
		return seasonsTournaments.lastKey();
	}

	private Integer getLastSeason() {
		return seasonsTournaments.firstKey();
	}

	private boolean hasSeason(int season) {
		return seasonsTournaments.containsKey(season);
	}

	private static <T> boolean startsWith(List<T> list, List<T> subList) {
		return subList.size() <= list.size() && list.subList(0, subList.size()).equals(subList);
	}
}
