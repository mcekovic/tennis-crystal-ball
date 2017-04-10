package org.strangeforest.tcb.stats.model;

import java.util.*;
import java.util.Map.*;

import static org.strangeforest.tcb.stats.util.PercentageUtil.*;

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

	public String getFirstResult() {
		return results.iterator().next();
	}

	public Collection<PlayerForecast> getPlayerForecasts() {
		return playerForecasts.values();
	}

	public PlayerForecast getOtherPlayer(int index) {
		int otherIndex = index + (index % 2 == 0 ? 1 : -1);
		return otherIndex < playerForecasts.size() ? new ArrayList<>(playerForecasts.values()).get(otherIndex) : null;
	}

	public double getStrength(int fromIndex, int count) {
		return playerForecasts.values().stream().skip(fromIndex).limit(count).mapToDouble(PlayerForecast::getWinProbability).sum();
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

	void removePlayersWORemainingResults() {
		for (Entry<Integer, PlayerForecast> forecastEntry : new HashMap<>(playerForecasts).entrySet()) {
			PlayerForecast playerForecast = forecastEntry.getValue();
			if (!playerForecast.hasAnyResult(results))
				playerForecasts.remove(forecastEntry.getKey());
		}
	}

	void removePastRounds() {
		for (String result : new ArrayList<>(results)) {
			if (!"W".equals(result) && isPastRound(result))
				results.remove(result);
		}
	}

	private boolean isPastRound(String result) {
		for (PlayerForecast playerForecast : getPlayerForecasts()) {
			Double probability = playerForecast.getProbability(result);
			if (probability != null && probability > 0.0 && probability < PCT)
				return false;
		}
		return true;
	}
}
