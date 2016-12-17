package org.strangeforest.tcb.stats.model;

import java.time.*;
import java.util.*;

public class DominanceTimeline {

	private final List<PlayerDominanceTimeline> players;
	private final SortedSet<Integer> seasons;
	private final LocalDate now;
	private List<DominanceSeason> dominanceSeasons;
	private List<DominanceEra> dominanceEras;

	public DominanceTimeline() {
		players = new ArrayList<>();
		seasons = new TreeSet<>(Comparator.reverseOrder());
		now = LocalDate.now();
	}

	public List<PlayerDominanceTimeline> getPlayers() {
		return players;
	}

	public Collection<Integer> getSeasons() {
		return seasons;
	}

	public List<DominanceSeason> getDominanceSeasons() {
		return dominanceSeasons;
	}

	public List<DominanceEra> getDominanceEras() {
		return dominanceEras;
	}

	public void addPlayer(PlayerDominanceTimeline player) {
		players.add(player);
		seasons.addAll(player.getSeasons());
		player.setTimeline(this);
	}

	public void calculateDominanceSeasons() {
		dominanceSeasons = new ArrayList<>(seasons.size());
		for (Integer season : seasons) {
			DominanceSeason dominanceSeason = new DominanceSeason(season);
			for (PlayerDominanceTimeline player : players)
				dominanceSeason.processPlayer(player);
			dominanceSeasons.add(dominanceSeason);
		}
	}

	public void calculateDominanceEras() {
		int seasonCount = dominanceSeasons.size();
		for (int i = 0; i < seasonCount; i++) {
			DominanceSeason dominanceSeason = dominanceSeasons.get(i);
			if (isEligibleForEra(dominanceSeason.getSeason())) {
				PlayerDominanceTimeline bestPlayer = dominanceSeason.getBestPlayer();
				PlayerDominanceTimeline prevBestPlayer = i > 0 ? dominanceSeasons.get(i - 1).getBestPlayer() : null;
				PlayerDominanceTimeline nextBestPlayer = i < seasonCount - 1 ? dominanceSeasons.get(i + 1).getBestPlayer() : null;
				dominanceSeason.setEraPlayer(prevBestPlayer == null || prevBestPlayer != nextBestPlayer ? bestPlayer : prevBestPlayer);
			}
		}
		dominanceEras = new ArrayList<>();
		List<DominanceSeason> eraSeasons = null;
		for (DominanceSeason dominanceSeason : dominanceSeasons) {
			if (eraSeasons != null && dominanceSeason.getEraPlayer() != eraSeasons.get(0).getEraPlayer()) {
				dominanceEras.add(new DominanceEra(eraSeasons));
				eraSeasons = null;
			}
			if (eraSeasons == null)
				eraSeasons = new ArrayList<>();
			eraSeasons.add(dominanceSeason);
		}
		dominanceEras.add(new DominanceEra(eraSeasons));
	}

	private boolean isEligibleForEra(int season) {
		int year = now.getYear();
		return season < year || (season == year && now.getMonthValue() >= 11);
	}


	// Util

	static int roundDominanceRatio(double dominanceRatio) {
		return 10 * ((int)dominanceRatio / 10);
	}
}
