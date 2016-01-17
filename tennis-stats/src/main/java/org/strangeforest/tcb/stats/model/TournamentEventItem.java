package org.strangeforest.tcb.stats.model;

public class TournamentEventItem {

	private final int id;
	private final String name;
	private final int season;

	public TournamentEventItem(int id, String name, int season) {
		this.id = id;
		this.name = name;
		this.season = season;
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
}
