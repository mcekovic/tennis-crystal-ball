package org.strangeforest.tcb.stats.model;

import java.util.*;

public class InProgressEventForecast {

	private final InProgressEvent event;
	private final Set<String> baseResults;
	private final Map<String, PlayersForecast> playersForecasts;

	private static final String CURRENT = "Current";

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
		baseResult = baseResult.equals("W") ? CURRENT : baseResult;
		playersForecasts.computeIfAbsent(baseResult, round -> new PlayersForecast()).addForecast(playerNum, playerId, name, seed, entry, countryId, result, probability);
		baseResults.add(baseResult);
	}

	public void process() {
		if (!playersForecasts.isEmpty())
			playersForecasts.values().iterator().next().addByes();
		PlayersForecast currentForecast = getPlayersForecasts(CURRENT);
		if (currentForecast != null) {
			currentForecast.addByes();
			currentForecast.removePastRounds();
		}
	}
}