package org.strangeforest.tcb.stats.model;

import java.util.*;

import static org.strangeforest.tcb.stats.util.PercentageUtil.*;

public class PlayerForecast extends MatchPlayerEx {

	static final PlayerForecast BYE = new PlayerForecast(0, null, null, null, null);

	private final Map<String, Double> forecast;

	PlayerForecast(int playerId, String name, Integer seed, String entry, String countryId) {
		super(playerId, name, seed, entry, countryId);
		forecast = new LinkedHashMap<>();
	}

	public boolean isEmpty() {
		return getName() == null;
	}

	public boolean isBye() {
		return isEmpty() && getEntry() == null;
	}

	public boolean isQualifier() {
		return isEmpty() && "Q".equals(getEntry());
	}

	public Double getProbability(String result) {
		Double probability = forecast.get(result);
		return probability != null ? PCT * probability : null;
	}

	void addForecast(String result, double probability) {
		forecast.put(result, probability);
	}

	boolean hasAnyResult(Iterable<String> results) {
		for (String result : results) {
			if (forecast.containsKey(result))
				return true;
		}
		return false;
	}
}
