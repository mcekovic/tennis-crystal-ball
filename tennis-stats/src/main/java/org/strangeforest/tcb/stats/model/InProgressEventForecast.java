package org.strangeforest.tcb.stats.model;

import java.util.*;

public class InProgressEventForecast {

	private final InProgressEvent event;
	private final Map<String, List<PlayerForecast>> playerForecasts;

	public InProgressEventForecast(InProgressEvent event) {
		this.event = event;
		playerForecasts = new TreeMap<>();
	}

	public InProgressEvent getEvent() {
		return event;
	}

	public Map<String, List<PlayerForecast>> getPlayerForecasts() {
		return playerForecasts;
	}

	public void addPlayerForecasts(String round, List<PlayerForecast> playerForecasts) {
		this.playerForecasts.put(round, playerForecasts);
	}
}
