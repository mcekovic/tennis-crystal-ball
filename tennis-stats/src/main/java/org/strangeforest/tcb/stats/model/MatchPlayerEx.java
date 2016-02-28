package org.strangeforest.tcb.stats.model;

import org.strangeforest.tcb.util.*;

public class MatchPlayerEx extends MatchPlayer {

	private final String countryId;

	public MatchPlayerEx(int id, String name, Integer seed, String entry, String countryId) {
		super(id, name, seed, entry);
		this.countryId = countryId;
	}

	public String getCountryId() {
		return countryId;
	}

	public String getCountryCode() {
		return CountryUtil.getISOAlpha2Code(countryId);
	}
}
