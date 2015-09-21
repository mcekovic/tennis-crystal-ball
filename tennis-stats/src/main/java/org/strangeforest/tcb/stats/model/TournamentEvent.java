package org.strangeforest.tcb.stats.model;

public class TournamentEvent {

	private final int id;
	private final String name;
	private final int season;

	public TournamentEvent(int id, String name, int season) {
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
