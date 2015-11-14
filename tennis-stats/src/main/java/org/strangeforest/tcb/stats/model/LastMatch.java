package org.strangeforest.tcb.stats.model;

public class LastMatch {

	private final long id;
	private final int season;
	private final String level;
	private final String surface;
	private final String tournament;
	private final String round;
	private final int winnerId;
	private final int loserId;
	private final String score;

	public LastMatch(long id, int season, String level, String surface, String tournament, String round, int winnerId, int loserId, String score) {
		this.id = id;
		this.season = season;
		this.level = level;
		this.surface = surface;
		this.tournament = tournament;
		this.round = round;
		this.winnerId = winnerId;
		this.loserId = loserId;
		this.score = score;
	}

	public long getId() {
		return id;
	}

	public int getSeason() {
		return season;
	}

	public String getLevel() {
		return level;
	}

	public String getSurface() {
		return surface;
	}

	public String getTournament() {
		return tournament;
	}

	public String getRound() {
		return round;
	}

	public int getWinnerId() {
		return winnerId;
	}

	public int getLoserId() {
		return loserId;
	}

	public String getScore() {
		return score;
	}
}
