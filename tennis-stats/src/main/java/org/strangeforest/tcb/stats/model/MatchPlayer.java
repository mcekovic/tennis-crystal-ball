package org.strangeforest.tcb.stats.model;

import org.strangeforest.tcb.util.*;

import static com.google.common.base.Strings.*;

public class MatchPlayer {

	private final int id;
	private final String name;
	private final Integer seed;
	private final String entry;
	private final Country country;

	public MatchPlayer(int id, String name, Integer seed, String entry, String countryId) {
		this.id = id;
		this.name = name;
		this.seed = seed;
		this.entry = entry;
		country = countryId != null ? new Country(countryId) : Country.UNKNOWN;
	}

	public MatchPlayer(MatchPlayer player) {
		id = player.id;
		name = player.name;
		seed = player.seed;
		entry = player.entry;
		country = player.country;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Integer getSeed() {
		return seed;
	}

	public String getEntry() {
		return entry;
	}

	public String seedAndEntry() {
		return formatSeedAndEntry(seed, entry);
	}

	public String formattedSeedAndEntry() {
		return seed != null || !isNullOrEmpty(entry) ? " (" + formatSeedAndEntry(seed, entry) + ")" : "";
	}

	public Country getCountry() {
		return country;
	}


	// Util

	public static String formatSeedAndEntry(Integer seed, String entry) {
		if (seed != null)
			return !isNullOrEmpty(entry) ? seed + " " + entry : String.valueOf(seed);
		else
			return !isNullOrEmpty(entry) ? entry : "";
	}
}
