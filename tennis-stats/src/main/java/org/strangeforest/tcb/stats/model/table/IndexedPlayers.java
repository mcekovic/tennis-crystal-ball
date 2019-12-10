package org.strangeforest.tcb.stats.model.table;

import java.util.*;

public class IndexedPlayers {

	private final Map<Integer, Integer> playerIndexMap = new LinkedHashMap<>();
	private final List<String> players = new ArrayList<>();

	public void addPlayer(int playerId, String playerName, int index) {
		playerIndexMap.put(playerId, index);
		players.add(playerName);
	}

	public IndexedPlayers union(IndexedPlayers players) {
		IndexedPlayers union = new IndexedPlayers();
		union.merge(this);
		union.merge(players);
		return union;
	}

	private void merge(IndexedPlayers players) {
		for (Integer playerId : players.playerIndexMap.keySet())
			playerIndexMap.put(playerId, playerIndexMap.size());
		this.players.addAll(players.players);
	}

	public Collection<Integer> getPlayerIds() {
		return playerIndexMap.keySet();
	}

	public List<String> getPlayers() {
		return players;
	}

	public boolean isEmpty() {
		return players.isEmpty();
	}

	public int getCount() {
		return players.size();
	}

	int getIndex(int playerId) {
		return playerIndexMap.get(playerId);
	}


	// Object methods

	@Override public String toString() {
		return String.valueOf(players);
	}
}
