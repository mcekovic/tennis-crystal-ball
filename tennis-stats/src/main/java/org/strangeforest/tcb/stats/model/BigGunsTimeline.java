package org.strangeforest.tcb.stats.model;

import java.util.*;

public class BigGunsTimeline {

	private final List<BigGunsPlayerTimeline> players;
	private final SortedSet<Integer> seasons;

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

	public void addPlayer(BigGunsPlayerTimeline player) {
		players.add(player);
		seasons.addAll(player.getSeasons());
		player.setTimeline(this);
	}
}
