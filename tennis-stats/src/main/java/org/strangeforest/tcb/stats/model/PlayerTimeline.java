package org.strangeforest.tcb.stats.model;

import java.util.*;

import static java.util.stream.Collectors.*;
import static org.strangeforest.tcb.stats.model.TournamentLevel.*;

public class PlayerTimeline {

	private final Map<Integer, PlayerTournamentTimeline> tournaments;
	private final SortedSet<Integer> seasons;
	private final Set<TournamentSeason> tournamentSeasons;

	public PlayerTimeline(Set<TournamentSeason> tournamentSeasons) {
		tournaments = new HashMap<>();
		seasons = new TreeSet<>();
		this.tournamentSeasons = tournamentSeasons;
	}

	public List<PlayerTournamentTimeline> getBigTournaments() {
		return getTournaments(EnumSet.of(GRAND_SLAM, TOUR_FINALS, MASTERS, OLYMPICS));
	}

	public List<PlayerTournamentTimeline> getOtherTournaments() {
		return getTournaments(EnumSet.of(ATP_500, ATP_250));
	}

	private List<PlayerTournamentTimeline> getTournaments(EnumSet<TournamentLevel> levels) {
		List<PlayerTournamentTimeline> selectedTournaments = tournaments.values().stream().filter(tournament -> levels.contains(tournament.maxLevel())).collect(toList());
		Collections.sort(selectedTournaments);
		TournamentLevel lastLevel = null;
		for (PlayerTournamentTimeline tournament : selectedTournaments) {
			TournamentLevel level = tournament.maxLevel();
			tournament.setFirstByLevel(!Objects.equals(level, lastLevel));
			lastLevel = level;
		}
		return selectedTournaments;
	}

	public Collection<Integer> getSeasons() {
		return seasons;
	}

	public int getSeasonCount() {
		return seasons.size();
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
		return tournaments.computeIfAbsent(tournamentId, id -> new PlayerTournamentTimeline(this, id));
	}

	boolean hasSeason(int tournamentId, int season) {
		return tournamentSeasons.contains(new TournamentSeason(tournamentId, season));
	}
}
