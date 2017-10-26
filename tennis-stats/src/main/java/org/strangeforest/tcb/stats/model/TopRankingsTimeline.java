package org.strangeforest.tcb.stats.model;

import java.util.*;

import static java.util.Comparator.*;

public class TopRankingsTimeline {

	private final int topRanks;
	private final Map<Integer, List<TopRankingsPlayer>> seasonsTopPlayers = new TreeMap<>(reverseOrder());
	private final Map<Integer, Integer> playersYENo1 = new HashMap<>();

	public TopRankingsTimeline(int topRanks) {
		this.topRanks = topRanks;
	}

	public int getTopRanks() {
		return topRanks;
	}

	public Set<Integer> getSeasons() {
		return seasonsTopPlayers.keySet();
	}

	public List<TopRankingsPlayer> getTopPlayers(int season) {
		return seasonsTopPlayers.get(season);
	}

	public int getYENo1(int playerId) {
		return playersYENo1.get(playerId);
	}

	public void addSeasonTopPlayer(int season, TopRankingsPlayer player) {
		addSeasonPlayer(season, player);
		updatePlayerYENo1s(player);
	}

	private void addSeasonPlayer(int season, TopRankingsPlayer player) {
		seasonsTopPlayers.computeIfAbsent(season, s -> new ArrayList<>()).add(player);
	}

	private void updatePlayerYENo1s(TopRankingsPlayer player) {
		if (player.getRank() == 1)
			player.setYeNo1(playersYENo1.compute(player.getPlayerId(), (p, ye) -> ye != null ? ye + 1 : 1));
	}
}