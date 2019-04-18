package org.strangeforest.tcb.stats.model.forecast;

import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.price.*;

import static org.strangeforest.tcb.stats.model.price.PriceUtil.*;

public class FavoritePlayer extends PlayerRow {

	private final double probability;
	private String price;

	public FavoritePlayer(int favorite, int playerId, String name, String countryId, double probability) {
		super(favorite, playerId, name, countryId, null);
		this.probability = probability;
	}

	public FavoritePlayer(int favorite, int playerId, String name, String countryId, double probability, PriceFormat priceFormat) {
		this(favorite, playerId, name, countryId, probability);
		price = priceFormat != null ? toFormattedPrice(probability, priceFormat) : null;
	}

	public double getProbability() {
		return probability;
	}

	public String getPrice() {
		return price;
	}

	public String getPrice(String format) {
		return toFormattedPrice(probability, format);
	}
}
