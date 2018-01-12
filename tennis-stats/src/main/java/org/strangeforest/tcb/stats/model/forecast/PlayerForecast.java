package org.strangeforest.tcb.stats.model.forecast;

import java.util.*;

import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.core.*;
import org.strangeforest.tcb.stats.model.prediction.*;

import static org.strangeforest.tcb.stats.model.prediction.PriceUtil.*;
import static org.strangeforest.tcb.stats.util.PercentageUtil.*;

public class PlayerForecast extends MatchPlayerEx {

	private Map<String, Double> forecast;

	public PlayerForecast(int playerId, String name, Integer seed, String entry, String countryId, Integer rank, Integer eloRating) {
		super(playerId, name, seed, entry, countryId, rank, eloRating, null);
	}

	public PlayerForecast(PlayerForecast player) {
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

	
	// Elo ratings

	public int getNextEloRating() {
		return nextEloRating != null ? nextEloRating : Player.START_ELO_RATING;
	}

	void setNextEloRating(Integer nextEloRating) {
		this.nextEloRating = nextEloRating;
	}


	// Forecast

	public Double getProbability(String result) {
		Double probability = rawProbability(result);
		return probability != null ? PCT * probability : null;
	}

	public double getRawProbability(String result) {
		Double probability = rawProbability(result);
		return probability != null ? probability : 0.0;
	}

	public double getWinProbability() {
		Double probability = rawProbability("W");
		return probability != null ? PCT * probability : 0.0;
	}

	public String getPrice(String result, String format) {
		Double probability = rawProbability(result);
		return probability != null ? PriceFormat.valueOf(format).format(toPrice(probability)) : null;
	}

	private Double rawProbability(String result) {
		return isEmpty() ? null : forecast.get(result);
	}

	public void addForecast(String result, double probability) {
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
