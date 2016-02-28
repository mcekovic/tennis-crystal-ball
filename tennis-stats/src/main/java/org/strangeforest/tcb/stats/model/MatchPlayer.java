package org.strangeforest.tcb.stats.model;

import static com.google.common.base.Strings.*;

public class MatchPlayer {

	private final int id;
	private final String name;
	private final Integer seed;
	private final String entry;

	public MatchPlayer(int id, String name, Integer seed, String entry) {
		this.id = id;
		this.name = name;
		this.seed = seed;
		this.entry = entry;
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

	public String getSeedAndEntry() {
		if (seed != null)
			return !isNullOrEmpty(entry) ? seed + " " + entry : String.valueOf(seed);
		else
			return !isNullOrEmpty(entry) ? entry : "";
	}
}
