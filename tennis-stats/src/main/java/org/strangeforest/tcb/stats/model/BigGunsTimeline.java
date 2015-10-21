package org.strangeforest.tcb.stats.model;

import java.util.*;

public class BigGunsTimeline {

	private final List<BigGunsPlayerTimeline> players;
	private final SortedSet<Integer> seasons;
	private List<BigGunsSeason> bigGunsSeasons;

	public BigGunsTimeline() {
		players = new ArrayList<>();
		seasons = new TreeSet<>(Comparator.reverseOrder());
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
}
