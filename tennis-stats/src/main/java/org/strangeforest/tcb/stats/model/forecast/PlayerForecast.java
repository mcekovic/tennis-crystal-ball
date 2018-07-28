package org.strangeforest.tcb.stats.model.forecast;

import java.util.*;

import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.price.*;

import static org.strangeforest.tcb.stats.model.price.PriceUtil.*;
import static org.strangeforest.tcb.stats.util.PercentageUtil.*;
import static org.strangeforest.tcb.util.EnumUtil.*;

public class PlayerForecast extends MatchPlayerEx {

	private Map<String, Double> forecast;
	private final EloRatingDelta recentEloRatingDelta;
	private final EloRatingDelta surfaceEloRatingDelta;
	private final EloRatingDelta inOutEloRatingDelta;
	private final EloRatingDelta setEloRatingDelta;

	public PlayerForecast(int playerId, String name, Integer seed, String entry, String countryId, Integer rank, Integer eloRating, Integer recentEloRating, Integer surfaceEloRating, Integer inOutEloRating, Integer setEloRating) {
		super(playerId, name, seed, entry, countryId, rank, eloRating, null);
		recentEloRatingDelta = new EloRatingDelta(recentEloRating, null);
		surfaceEloRatingDelta = new EloRatingDelta(surfaceEloRating, null);
		inOutEloRatingDelta = new EloRatingDelta(inOutEloRating, null);
		setEloRatingDelta = new EloRatingDelta(setEloRating, null);
	}

	public PlayerForecast(PlayerForecast player) {
		super(player);
		recentEloRatingDelta = new EloRatingDelta(player.recentEloRatingDelta);
		surfaceEloRatingDelta = new EloRatingDelta(player.surfaceEloRatingDelta);
		inOutEloRatingDelta = new EloRatingDelta(player.inOutEloRatingDelta);
		setEloRatingDelta = new EloRatingDelta(player.setEloRatingDelta);
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


	// Elo rating

	public Integer getEloRating(ForecastEloType eloType) {
		switch (eloType) {
			case OVERALL: return eloRatingDelta.getEloRating();
			case RECENT: return recentEloRatingDelta.getEloRating();
			case SURFACE: return surfaceEloRatingDelta.getEloRating();
			case IN_OUT: return inOutEloRatingDelta.getEloRating();
			case SET: return setEloRatingDelta.getEloRating();
			default: throw unknownEnum(eloType);
		}
	}

	public int getNextEloRating(ForecastEloType eloType) {
		switch (eloType) {
			case OVERALL: return eloRatingDelta.getNextEloRating();
			case RECENT: return recentEloRatingDelta.getNextEloRating();
			case SURFACE: return surfaceEloRatingDelta.getNextEloRating();
			case IN_OUT: return inOutEloRatingDelta.getNextEloRating();
			case SET: return setEloRatingDelta.getNextEloRating();
			default: throw unknownEnum(eloType);
		}
	}

	public Integer getEloRatingDelta(ForecastEloType eloType) {
		switch (eloType) {
			case OVERALL: return eloRatingDelta.getEloRatingDelta();
			case RECENT: return recentEloRatingDelta.getEloRatingDelta();
			case SURFACE: return surfaceEloRatingDelta.getEloRatingDelta();
			case IN_OUT: return inOutEloRatingDelta.getEloRatingDelta();
			case SET: return setEloRatingDelta.getEloRatingDelta();
			default: throw unknownEnum(eloType);
		}
	}

	void setNextEloRatings(Integer nextEloRating, Integer nextRecentEloRating, Integer nextSurfaceEloRating, Integer nextInOutEloRating, Integer nextSetEloRating) {
		eloRatingDelta.setNextEloRating(nextEloRating);
		recentEloRatingDelta.setNextEloRating(nextRecentEloRating);
		surfaceEloRatingDelta.setNextEloRating(nextSurfaceEloRating);
		inOutEloRatingDelta.setNextEloRating(nextInOutEloRating);
		setEloRatingDelta.setNextEloRating(nextSetEloRating);
	}
}
