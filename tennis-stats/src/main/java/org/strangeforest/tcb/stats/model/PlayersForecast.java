package org.strangeforest.tcb.stats.model;

import java.util.*;
import java.util.Map.*;

public class PlayersForecast {

	private final Set<String> results;
	private final Map<Integer, PlayerForecast> playerForecasts;

	PlayersForecast(List<PlayerForecast> players) {
		results = new LinkedHashSet<>();
		playerForecasts = new LinkedHashMap<>();
		for (PlayerForecast player : players)
			playerForecasts.put(player.getId(), new PlayerForecast(player));
	}

	public Set<String> getResults() {
		return results;
	}

	public Collection<PlayerForecast> getPlayerForecasts() {
		return playerForecasts.values();
	}

	void addResult(int playerId, String result, double probability) {
		playerForecasts.get(playerId).addForecast(result, probability);
		results.add(result);
	}

	void removePlayersWOResults() {
		for (Entry<Integer, PlayerForecast> forecastEntry : new HashMap<>(playerForecasts).entrySet()) {
			PlayerForecast playerForecast = forecastEntry.getValue();
			if (playerForecast.isEmpty())
				playerForecasts.remove(forecastEntry.getKey());
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
