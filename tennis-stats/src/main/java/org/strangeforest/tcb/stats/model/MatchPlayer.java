package org.strangeforest.tcb.stats.model;

public class MatchPlayer {

	private final int id;
	private final String name;
	private final int seed;
	private final String entry;

	public MatchPlayer(int id, String name, int seed, String entry) {
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

	public int getSeed() {
		return seed;
	}

	public String getEntry() {
		return entry;
	}
}
