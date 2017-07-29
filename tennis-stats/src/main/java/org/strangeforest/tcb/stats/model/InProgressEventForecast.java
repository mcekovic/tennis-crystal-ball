package org.strangeforest.tcb.stats.model;

import java.util.*;

public class InProgressEventForecast {

	private final InProgressEvent event;
	private final Map<String, PlayersForecast> playersForecasts;

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

	public PlayersForecast getCurrentForecasts() {
		return getPlayersForecasts(CURRENT);
	}

	public PlayersForecast getPlayersForecasts(String baseResult) {
		return playersForecasts.get(baseResult);
	}

	public void addForecast(List<PlayerForecast> players, int playerId, String baseResult, String result, double probability) {
		baseResult = baseResult.equals("W") ? CURRENT : baseResult;
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
		PlayersForecast currentForecast = getCurrentForecasts();
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