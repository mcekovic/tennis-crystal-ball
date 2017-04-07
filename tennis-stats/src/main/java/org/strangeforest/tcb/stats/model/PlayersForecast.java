package org.strangeforest.tcb.stats.model;

import java.util.*;

public class PlayersForecast {

	private final Set<String> results;
	private final Map<Integer, PlayerForecast> playerForecasts;

	public PlayersForecast() {
		results = new LinkedHashSet<>();
		playerForecasts = new TreeMap<>();
	}

	public Set<String> getResults() {
		return results;
	}

	public Collection<PlayerForecast> getPlayerForecasts() {
		return playerForecasts.values();
	}

	public void addForecast(int playerNum, int playerId, String name, Integer seed, String entry, String countryId, String result, double probability) {
		playerForecasts.computeIfAbsent(playerNum, num -> new PlayerForecast(playerId, name, seed, entry, countryId)).addForecast(result, probability);
		results.add(result);
	}

	public void addByes() {
		Integer prevNum = null;
		for (int playerNum : new ArrayList<>(playerForecasts.keySet())) {
			if (prevNum != null && prevNum < playerNum - 1) {
				for (int byeNum = prevNum + 1; byeNum < playerNum; byeNum++)
				playerForecasts.put(byeNum, new PlayerForecast(0, null, null, null, null));
			}
			prevNum = playerNum;
		}
	}
}
