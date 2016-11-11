package org.strangeforest.tcb.stats.model;

import org.strangeforest.tcb.util.*;

public class MatchPlayerEx extends MatchPlayer {

	private final Country country;

	public MatchPlayerEx(int id, String name, Integer seed, String entry, String countryId) {
		super(id, name, seed, entry);
		country = new Country(countryId);
	}

	public Country getCountry() {
		return country;
	}
}
