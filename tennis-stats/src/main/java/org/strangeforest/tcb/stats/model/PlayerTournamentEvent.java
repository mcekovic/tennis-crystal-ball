package org.strangeforest.tcb.stats.model;

import java.util.*;

public class PlayerTournamentEvent {

	private final int tournamentEventId;
	private final int season;
	private final Date date;
	private final String name;
	private final String level;
	private final String surface;
	private final Integer drawSize;
	private final String result;

	public PlayerTournamentEvent(int tournamentEventId, int season, Date date, String name, String level, String surface, Integer drawSize, String result) {
		this.tournamentEventId = tournamentEventId;
		this.season = season;
		this.date = date;
		this.name = name;
		this.level = level;
		this.surface = surface;
		this.drawSize = drawSize;
		this.result = result;
	}

	public int getTournamentEventId() {
		return tournamentEventId;
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

	public Integer getDrawSize() {
		return drawSize;
	}

	public String getResult() {
		return result;
	}
}
