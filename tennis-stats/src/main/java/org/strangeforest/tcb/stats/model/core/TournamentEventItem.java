package org.strangeforest.tcb.stats.model.core;

public class TournamentEventItem {

	private final int id;
	private final String name;
	private final int season;
	private final String level;

	public TournamentEventItem(int id, String name, int season, String level) {
		this.id = id;
		this.name = name;
		this.season = season;
		this.level = level;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getSeason() {
		return season;
	}

	public String getLevel() {
		return level;
	}
}
