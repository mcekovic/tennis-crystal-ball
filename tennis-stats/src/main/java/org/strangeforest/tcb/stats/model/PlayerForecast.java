package org.strangeforest.tcb.stats.model;

import java.util.*;

import org.strangeforest.tcb.stats.model.prediction.*;

import static org.strangeforest.tcb.stats.model.prediction.PriceUtil.*;
import static org.strangeforest.tcb.stats.util.PercentageUtil.*;

public class PlayerForecast extends MatchPlayerEx {

	private Map<String, Double> forecast;

	public PlayerForecast(int playerId, String name, Integer seed, String entry, String countryId) {
		super(playerId, name, seed, entry, countryId);
	}

	PlayerForecast(PlayerForecast player) {
		super(player);
	}

	public boolean isKnown() {
		return getName() != null;
	}

	public boolean isBye() {
		return getName() == null && getEntry() == null;
	}

	public boolean isQualifier() {
		return getName() == null && "Q".equals(getEntry());
	}

	public Double getProbability(String result) {
		Double probability = getBareProbability(result);
		return probability != null ? PCT * probability : null;
	}

	public String getPrice(String result, String format) {
		Double probability = getBareProbability(result);
		return probability != null ? PriceFormat.valueOf(format).format(toPrice(probability)) : null;
	}

	private Double getBareProbability(String result) {
		return isEmpty() ? null : forecast.get(result);
	}

	public double probability(String result) {
		Double probability = getProbability(result);
		return probability != null ? probability : 0.0;
	}

	public double winProbability() {
		return probability("W");
	}

	void addForecast(String result, double probability) {
		if (forecast == null)
			forecast = new HashMap<>();
		forecast.put(result, probability);
	}

	boolean isEmpty() {
		return forecast == null;
	}
	
	boolean hasAnyResult(Iterable<String> results) {
		if (isEmpty())
			return false;
		for (String result : results) {
			if (forecast.containsKey(result))
				return true;
		}
		return false;
	}
}
