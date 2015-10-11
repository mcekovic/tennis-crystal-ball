package org.strangeforest.tcb.stats.model;

import java.util.*;

public class PlayerTimeline {

	private final Map<Integer, PlayerTournamentTimeline> tournaments;
	private final SortedSet<Integer> seasons;

	public PlayerTimeline() {
		tournaments = new HashMap<>();
		seasons = new TreeSet<>();
	}

	public List<PlayerTournamentTimeline> getTournaments() {
		List<PlayerTournamentTimeline> sortedTournaments = new ArrayList<>(tournaments.values());
		Collections.sort(sortedTournaments);
		String lastLevel = null;
		for (PlayerTournamentTimeline tournament : sortedTournaments) {
			String level = tournament.getMaxLevel();
			tournament.setFirstByLevel(!Objects.equals(level, lastLevel));
			lastLevel = level;
		}
		return sortedTournaments;
	}

	public Collection<Integer> getSeasons() {
		return seasons;
	}

	public boolean isEmpty() {
		return tournaments.isEmpty();
	}

	public void addItem(PlayerTimelineItem item) {
		PlayerTournamentTimeline tournament = getTournamentTimeline(item.getTournamentId());
		tournament.addItem(item);
		seasons.add(item.getSeason());
	}

	private PlayerTournamentTimeline getTournamentTimeline(int tournamentId) {
		PlayerTournamentTimeline tournament = tournaments.get(tournamentId);
		if (tournament == null) {
			tournament = new PlayerTournamentTimeline(this, tournamentId);
			tournaments.put(tournamentId, tournament);
		}
		return tournament;
	}
}
