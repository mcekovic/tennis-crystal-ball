package org.strangeforest.tcb.stats.model;

import java.util.*;

import static java.util.Comparator.*;

public class TournamentLevelTimeline {

	private final String level;
	private final Map<Integer, List<TournamentItem>> seasonsTournaments = new TreeMap<>(reverseOrder());
	private final Map<Integer, List<TournamentLevelTimelineItem>> seasonsEvents = new HashMap<>();
	private final Map<Integer, Integer> playerWins = new HashMap<>();

	public TournamentLevelTimeline(String level) {
		this.level = level;
	}

	public Set<Integer> getSeasons() {
		return seasonsTournaments.keySet();
	}

	public boolean areSameTournaments(int season) {
		Optional<Integer> nextSeason = getNextSeason(season);
		return nextSeason.isPresent() && seasonsTournaments.get(season).equals(seasonsTournaments.get(nextSeason.get()));
	}

	public List<TournamentItem> getTournaments(int season) {
		return seasonsTournaments.get(season);
	}

	public List<TournamentLevelTimelineItem> getSeasonEvents(int season) {
		return seasonsEvents.get(season);
	}

	public int getWins(int playerId) {
		return playerWins.get(playerId);
	}

	public void addItem(TournamentLevelTimelineItem item) {
		addSeasonTournament(item);
		addSeasonEvent(item);
		updatePlayerWins(item);
	}

	private void addSeasonTournament(TournamentLevelTimelineItem item) {
		int season = item.getSeason();
		List<TournamentItem> seasonTournaments = seasonsTournaments.get(season);
		if (seasonTournaments == null) {
			seasonTournaments = new ArrayList<>();
			seasonsTournaments.put(season, seasonTournaments);
		}
		seasonTournaments.add(new TournamentItem(item.getTournamentId(), item.getName(), level));
	}

	private void addSeasonEvent(TournamentLevelTimelineItem item) {
		int season = item.getSeason();
		List<TournamentLevelTimelineItem> seasonEvents = seasonsEvents.get(season);
		if (seasonEvents == null) {
			seasonEvents = new ArrayList<>();
			seasonsEvents.put(season, seasonEvents);
		}
		seasonEvents.add(item);
	}

	private void updatePlayerWins(TournamentLevelTimelineItem item) {
		PlayerRow winner = item.getWinner();
		if (winner == null)
			return;
		int playerId = winner.getPlayerId();
		Integer wins = playerWins.get(playerId);
		wins = wins != null ? wins + 1 : 1;
		item.setWinnerWins(wins);
		playerWins.put(playerId, wins);
	}

	public void addMissingLastSeasonTournaments() {
		int season = getLastSeason();
		int prevSeason = getPrevSeason(season).get();
		List<TournamentItem> seasonTournaments = seasonsTournaments.get(season);
		List<TournamentItem> prevSeasonTournaments = seasonsTournaments.get(prevSeason);
		if (startsWith(prevSeasonTournaments, seasonTournaments)) {
			List<TournamentLevelTimelineItem> prevSeasonEvents = seasonsEvents.get(prevSeason);
			for (int index = seasonTournaments.size(); index < prevSeasonEvents.size(); index++) {
				TournamentLevelTimelineItem item = prevSeasonEvents.get(index);
				addItem(new TournamentLevelTimelineItem(item.getTournamentId(), item.getName(), season, 0, null, null));
			}
		}
	}


	// Util

	private int getLastSeason() {
		return getSeasons().iterator().next();
	}

	private Optional<Integer> getPrevSeason(int season) {
		Set<Integer> seasons = getSeasons();
		if (seasons.stream().sorted(naturalOrder()).findFirst().get() == season)
			return Optional.empty();
		else {
			while (true) {
				int nextSeason = season - 1;
				if (seasons.contains(nextSeason))
					return Optional.of(nextSeason);
			}
		}
	}

	private Optional<Integer> getNextSeason(int season) {
		Set<Integer> seasons = getSeasons();
		if (seasons.stream().findFirst().get() == season)
			return Optional.empty();
		else {
			while (true) {
				int nextSeason = season + 1;
				if (seasons.contains(nextSeason))
					return Optional.of(nextSeason);
			}
		}
	}

	private static boolean startsWith(List<TournamentItem> list, List<TournamentItem> subList) {
		return subList.size() < list.size() && list.subList(0, subList.size()).equals(subList);
	}
}
