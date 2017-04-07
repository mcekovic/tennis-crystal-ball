package org.strangeforest.tcb.stats.model;

import java.util.*;
import java.util.Map.*;

public class PlayersForecast {

	private final Set<String> results;
	private final Map<Integer, PlayerForecast> playerForecasts;

	PlayersForecast() {
		results = new LinkedHashSet<>();
		playerForecasts = new TreeMap<>();
	}

	public Set<String> getResults() {
		return results;
	}

	public Collection<PlayerForecast> getPlayerForecasts() {
		return playerForecasts.values();
	}

	void addForecast(int playerNum, int playerId, String name, Integer seed, String entry, String countryId, String result, double probability) {
		playerForecasts.computeIfAbsent(playerNum, num -> new PlayerForecast(playerId, name, seed, entry, countryId)).addForecast(result, probability);
		results.add(result);
	}

	void addByes() {
		Integer prevNum = null;
		for (int playerNum : new ArrayList<>(playerForecasts.keySet())) {
			if (prevNum != null && prevNum < playerNum - 1) {
				for (int byeNum = prevNum + 1; byeNum < playerNum; byeNum++)
				playerForecasts.put(byeNum, PlayerForecast.BYE);
			}
			prevNum = playerNum;
		}
	}

	void removePastRounds() {
		for (String result : new ArrayList<>(results)) {
			if (!"W".equals(result) && isPastRound(result))
				results.remove(result);
		}
		for (Entry<Integer, PlayerForecast> forecastEntry : new HashMap<>(playerForecasts).entrySet()) {
			PlayerForecast playerForecast = forecastEntry.getValue();
			if (!playerForecast.hasAnyResult(results))
				playerForecasts.remove(forecastEntry.getKey());
		}
	}

	private boolean isPastRound(String result) {
		for (PlayerForecast playerForecast : getPlayerForecasts()) {
			Double probability = playerForecast.getProbability(result);
			if (probability != null && probability > 0.0 && probability < 1.0)
				return false;
		}
		return true;
	}
}
