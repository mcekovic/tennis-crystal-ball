package org.strangeforest.tcb.stats.model;

import java.util.*;

import org.strangeforest.tcb.stats.model.core.*;

import static java.util.stream.Collectors.*;
import static org.strangeforest.tcb.stats.model.core.TournamentLevel.*;

public class PlayerTimeline {

	private final Map<Integer, PlayerTournamentTimeline> tournaments;
	private final SortedSet<Integer> seasons;
	private final Set<TournamentSeason> tournamentSeasons;
	private Map<Integer, Integer> titles;
	private Map<Integer, Integer> entries;
	private Map<Integer, Integer> yearEndRanks;
	private Map<Integer, Integer> bestEloRatings;
	private Map<Integer, Integer> goatPoints;

	public PlayerTimeline(Set<TournamentSeason> tournamentSeasons) {
		tournaments = new HashMap<>();
		seasons = new TreeSet<>();
		this.tournamentSeasons = tournamentSeasons;
	}

	public List<PlayerTournamentTimeline> getBigTournaments() {
		return getTournaments(EnumSet.of(GRAND_SLAM, TOUR_FINALS, ALT_FINALS, MASTERS, OLYMPICS));
	}

	public List<PlayerTournamentTimeline> getOtherTournaments() {
		return getTournaments(EnumSet.of(ATP_500, ATP_250));
	}

	private List<PlayerTournamentTimeline> getTournaments(Set<TournamentLevel> levels) {
		var selectedTournaments = tournaments.values().stream().filter(tournament -> levels.contains(tournament.maxLevel())).collect(toList());
		Collections.sort(selectedTournaments);
		TournamentLevel lastLevel = null;
		for (var tournament : selectedTournaments) {
			var level = tournament.maxLevel();
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
		var tournament = getTournamentTimeline(item);
		tournament.addItem(item);
		seasons.add(item.getSeason());
	}

	private PlayerTournamentTimeline getTournamentTimeline(PlayerTimelineItem item) {
		return tournaments.computeIfAbsent(item.getOriginalTournamentId(), id -> new PlayerTournamentTimeline(this, item.getTournamentId()));
	}

	boolean hasSeason(int tournamentId, int season) {
		return tournamentSeasons.contains(new TournamentSeason(tournamentId, season));
	}

	public Map<Integer, Integer> getTitles() {
		return titles;
	}

	public void setTitles(Map<Integer, Integer> titles) {
		this.titles = titles;
	}

	public Map<Integer, Integer> getEntries() {
		return entries;
	}

	public void setEntries(Map<Integer, Integer> entries) {
		this.entries = entries;
	}

	public Map<Integer, Integer> getYearEndRanks() {
		return yearEndRanks;
	}

	public void setYearEndRanks(Map<Integer, Integer> yearEndRanks) {
		this.yearEndRanks = yearEndRanks;
	}

	public Map<Integer, Integer> getBestEloRatings() {
		return bestEloRatings;
	}

	public void setBestEloRatings(Map<Integer, Integer> bestEloRatings) {
		this.bestEloRatings = bestEloRatings;
	}

	public Map<Integer, Integer> getGoatPoints() {
		return goatPoints;
	}

	public void setGoatPoints(Map<Integer, Integer> goatPoints) {
		this.goatPoints = goatPoints;
	}
}
