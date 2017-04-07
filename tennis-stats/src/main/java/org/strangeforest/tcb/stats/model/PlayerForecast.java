package org.strangeforest.tcb.stats.model;

import java.util.*;

import static org.strangeforest.tcb.stats.util.PercentageUtil.*;

public class PlayerForecast extends MatchPlayerEx {

	private final Map<String, Double> forecast;

	public PlayerForecast(int playerId, String name, Integer seed, String entry, String countryId) {
		super(playerId, name, seed, entry, countryId);
		forecast = new LinkedHashMap<>();
	}

	public boolean isBye() {
		return getName() == null;
	}

	public Double getProbability(String result) {
		Double probability = forecast.get(result);
		return probability != null ? PCT * probability : null;
	}

	public void addForecast(String result, double probability) {
		forecast.put(result, probability);
	}
}
