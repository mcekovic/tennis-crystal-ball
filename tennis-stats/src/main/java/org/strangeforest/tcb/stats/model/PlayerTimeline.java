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
			String level = tournament.getLevel();
			tournament.setFirstByLevel(!Objects.equals(level, lastLevel));
			lastLevel = level;
		}
		return sortedTournaments;
	}

	public Collection<Integer> getSeasons() {
		return seasons;
	}

	public void addItem(PlayerTimelineItem item) {
		PlayerTournamentTimeline tournament = getTournamentTimeline(item);
		tournament.addItem(item);
		seasons.add(item.getSeason());
	}

	private PlayerTournamentTimeline getTournamentTimeline(PlayerTimelineItem item) {
		int tournamentId = item.getTournamentId();
		PlayerTournamentTimeline tournament = tournaments.get(tournamentId);
		if (tournament == null) {
			tournament = new PlayerTournamentTimeline(this, tournamentId, item.getLevel(), item.getSurface(),  item.getName(), item.getDate());
			tournaments.put(tournamentId, tournament);
		}
		return tournament;
	}
}
