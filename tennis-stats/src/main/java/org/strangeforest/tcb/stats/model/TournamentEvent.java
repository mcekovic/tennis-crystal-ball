package org.strangeforest.tcb.stats.model;

import java.util.*;

public class TournamentEvent {

	private final int id;
	private final int season;
	private final Date date;
	private final String name;
	private final String level;
	private final String surface;
	private final boolean indoor;
	private final String drawType;
	private final int drawSize;
	private final MatchPlayer winner;
	private final MatchPlayer loser;
	private final String score;

	public TournamentEvent(int id, int season, Date date, String name, String level, String surface, boolean indoor, String drawType, int drawSize, MatchPlayer winner, MatchPlayer loser, String score) {
		this.id = id;
		this.season = season;
		this.date = date;
		this.name = name;
		this.level = level;
		this.surface = surface;
		this.indoor = indoor;
		this.drawType = drawType;
		this.drawSize = drawSize;
		this.winner = winner;
		this.loser = loser;
		this.score = score;
	}

	public int getId() {
		return id;
	}

	public int getSeason() {
		return season;
	}

	public Date getDate() {
		return date;
	}

	public String getName() {
		return name;
	}

	public String getLevel() {
		return level;
	}

	public String getSurface() {
		return surface;
	}

	public boolean isIndoor() {
		return indoor;
	}

	public String getDrawType() {
		return drawType;
	}

	public int getDrawSize() {
		return drawSize;
	}

	public String getDraw() {
		return drawType + drawSize;
	}

	public MatchPlayer getWinner() {
		return winner;
	}

	public MatchPlayer getLoser() {
		return loser;
	}

	public String getScore() {
		return score;
	}
}
