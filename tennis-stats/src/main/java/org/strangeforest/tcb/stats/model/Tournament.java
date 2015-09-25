package org.strangeforest.tcb.stats.model;

public class Tournament {

	private final int id;
	private final String name;
	private final String level;

	public Tournament(int id, String name, String level) {
		this.id = id;
		this.name = name;
		this.level = level;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getLevel() {
		return level;
	}
}
