package org.strangeforest.tcb.stats.model;

public class TournamentLevelTimelinePlayer extends PlayerRow {

	private String fullName;
	private Integer seed;
	private String entry;

	public TournamentLevelTimelinePlayer(int rank, int playerId, String name, String countryId, Boolean active) {
		super(rank, playerId, name, countryId, active);
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public Integer getSeed() {
		return seed;
	}

	public void setSeed(Integer seed) {
		this.seed = seed;
	}

	public String getEntry() {
		return entry;
	}

	public void setEntry(String entry) {
		this.entry = entry;
	}
}
