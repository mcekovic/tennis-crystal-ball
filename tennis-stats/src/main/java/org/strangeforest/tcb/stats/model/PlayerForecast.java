package org.strangeforest.tcb.stats.model;

import java.util.*;

public class PlayerForecast extends PlayerRow {

	private final Map<String, Double> forecast;

	public PlayerForecast(int playerNum, int playerId, String name, String countryId) {
		super(playerNum, playerId, name, countryId, null);
		forecast = new HashMap<>();
	}

	public Map<String, Double> getForecast() {
		return forecast;
	}

	public void addForecast(String result, double probability) {
		forecast.put(result, probability);
	}
}
