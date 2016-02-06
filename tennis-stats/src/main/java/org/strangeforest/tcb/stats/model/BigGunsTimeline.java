package org.strangeforest.tcb.stats.model;

import java.time.*;
import java.util.*;

public class BigGunsTimeline {

	private final List<BigGunsPlayerTimeline> players;
	private final SortedSet<Integer> seasons;
	private final LocalDate now;
	private List<BigGunsSeason> bigGunsSeasons;
	private List<BigGunsEra> bigGunsEras;

	public BigGunsTimeline() {
		players = new ArrayList<>();
		seasons = new TreeSet<>(Comparator.reverseOrder());
		now = LocalDate.now();
	}

	public List<BigGunsPlayerTimeline> getPlayers() {
		return players;
	}

	public Collection<Integer> getSeasons() {
		return seasons;
	}

	public List<BigGunsSeason> getBigGunsSeasons() {
		return bigGunsSeasons;
	}

	public List<BigGunsEra> getBigGunsEras() {
		return bigGunsEras;
	}

	public void addPlayer(BigGunsPlayerTimeline player) {
		players.add(player);
		seasons.addAll(player.getSeasons());
		player.setTimeline(this);
	}

	public void calculateBigGunsSeasons() {
		bigGunsSeasons = new ArrayList<>(seasons.size());
		for (Integer season : seasons) {
			BigGunsSeason bigGunsSeason = new BigGunsSeason(season);
			for (BigGunsPlayerTimeline player : players)
				bigGunsSeason.processPlayer(player);
			bigGunsSeasons.add(bigGunsSeason);
		}
	}

	public void calculateBigGunsEras() {
		int seasonCount = bigGunsSeasons.size();
		for (int i = 0; i < seasonCount; i++) {
			BigGunsSeason bigGunsSeason = bigGunsSeasons.get(i);
			if (isEligibleForEra(bigGunsSeason.getSeason())) {
				BigGunsPlayerTimeline bestPlayer = bigGunsSeason.getBestPlayer();
				BigGunsPlayerTimeline prevBestPlayer = i > 0 ? bigGunsSeasons.get(i - 1).getBestPlayer() : null;
				BigGunsPlayerTimeline nextBestPlayer = i < seasonCount - 1 ? bigGunsSeasons.get(i + 1).getBestPlayer() : null;
				bigGunsSeason.setEraPlayer(prevBestPlayer == null || prevBestPlayer != nextBestPlayer ? bestPlayer : prevBestPlayer);
			}
		}
		bigGunsEras = new ArrayList<>();
		List<BigGunsSeason> eraSeasons = null;
		for (BigGunsSeason bigGunsSeason : bigGunsSeasons) {
			if (eraSeasons != null && bigGunsSeason.getEraPlayer() != eraSeasons.get(0).getEraPlayer()) {
				bigGunsEras.add(new BigGunsEra(eraSeasons));
				eraSeasons = null;
			}
			if (eraSeasons == null)
				eraSeasons = new ArrayList<>();
			eraSeasons.add(bigGunsSeason);
		}
		bigGunsEras.add(new BigGunsEra(eraSeasons));
	}

	private boolean isEligibleForEra(int season) {
		int year = now.getYear();
		return season < year || (season == year && now.getMonthValue() >= 11);
	}


	// Util

	static int roundDominanceRatio(double dominanceRatio) {
		return 10*Math.floorDiv((int)dominanceRatio, 10);
	}
}
