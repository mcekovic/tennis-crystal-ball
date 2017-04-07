package org.strangeforest.tcb.stats.model;

import java.util.*;

public class InProgressEventForecast {

	private final InProgressEvent event;
	private final Set<String> baseResults;
	private final Map<String, PlayersForecast> playersForecasts;

	public InProgressEventForecast(InProgressEvent event) {
		this.event = event;
		baseResults = new LinkedHashSet<>();
		playersForecasts = new LinkedHashMap<>();
	}

	public InProgressEvent getEvent() {
		return event;
	}

	public Set<String> getBaseResults() {
		return baseResults;
	}

	public PlayersForecast getPlayersForecasts(String baseResult) {
		return playersForecasts.get(baseResult);
	}

	public void addForecast(String baseResult, int playerNum, int playerId, String name, Integer seed, String entry, String countryId, String result, double probability) {
		playersForecasts.computeIfAbsent(baseResult, round -> new PlayersForecast()).addForecast(playerNum, playerId, name, seed, entry, countryId, result, probability);
		baseResults.add(baseResult);
	}

	public void addByes() {
		if (!playersForecasts.isEmpty())
			playersForecasts.values().iterator().next().addByes();
		PlayersForecast currentForecast = playersForecasts.get("W");
		if (currentForecast != null)
			currentForecast.addByes();
	}
}