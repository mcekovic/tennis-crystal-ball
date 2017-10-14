package org.strangeforest.tcb.stats.model;

import java.util.*;

public class InProgressEventForecast {

	private final InProgressEvent event;
	private final Map<String, PlayersForecast> playersForecasts;
	private String maxResult;

	private static final String CURRENT = "Current";

	public InProgressEventForecast(InProgressEvent event) {
		this.event = event;
		playersForecasts = new LinkedHashMap<>();
	}

	public InProgressEvent getEvent() {
		return event;
	}

	public Set<String> getBaseResults() {
		return playersForecasts.keySet();
	}

	public String getMaxResult() {
		return maxResult;
	}

	public PlayersForecast getCurrentForecast() {
		return getPlayersForecast(CURRENT);
	}

	public PlayersForecast getPlayersForecast(String baseResult) {
		return playersForecasts.get(baseResult);
	}

	public PlayersForecast getPrevPlayersForecast(String baseResult) {
		String prevResult = null;
		for (String result : playersForecasts.keySet()) {
			if (result.equals(baseResult))
				return prevResult != null ? playersForecasts.get(prevResult) : null;
			prevResult = result;
		}
		return null;
	}

	public void addForecast(List<PlayerForecast> players, int playerId, String baseResult, String result, double probability) {
		if (baseResult.equals("W"))
			baseResult = CURRENT;
		else
			maxResult = baseResult;
		playersForecasts.computeIfAbsent(baseResult, round -> new PlayersForecast(players)).addResult(playerId, result, probability);
	}

	public void process() {
		if (playersForecasts.isEmpty())
			return;
		KOResult entryRound = playersForecasts.values().iterator().next().getEntryRound();
		playersForecasts.forEach((baseResult, forecast) -> {
			if (!(baseResult.equals(entryRound.name()) || baseResult.equals(CURRENT)))
				forecast.removePlayersWOResults();
		});
		PlayersForecast currentForecast = getCurrentForecast();
		if (currentForecast != null) {
			currentForecast.removePastRounds();
			if (!currentForecast.getResults().isEmpty()) {
				String firstResult = currentForecast.getFirstResult();
				if (entryRound.hasNext() && !entryRound.next().name().equals(firstResult))
					currentForecast.removePlayersWORemainingResults();
			}
		}
	}
}