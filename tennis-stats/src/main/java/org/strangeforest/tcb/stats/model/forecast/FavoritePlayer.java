package org.strangeforest.tcb.stats.model.forecast;

import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.prediction.*;

import static org.strangeforest.tcb.stats.model.prediction.PriceUtil.*;

public class FavoritePlayer extends PlayerRow {

	private double probability;

	public FavoritePlayer(int favorite, int playerId, String name, String countryId, double probability) {
		super(favorite, playerId, name, countryId, null);
		this.probability = probability;
	}

	public double getProbability() {
		return probability;
	}

	public String getPrice(String format) {
		return PriceFormat.valueOf(format).format(toPrice(probability));
	}
}
