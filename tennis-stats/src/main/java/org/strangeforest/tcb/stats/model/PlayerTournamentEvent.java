package org.strangeforest.tcb.stats.model;

import java.time.*;

import static org.strangeforest.tcb.stats.model.core.TournamentLevel.*;

public class PlayerTournamentEvent {

	private final int tournamentEventId;
	private final int season;
	private final LocalDate date;
	private final String name;
	private final String level;
	private final String surface;
	private final boolean indoor;
	private Integer speed;
	private final String drawType;
	private final Integer drawSize;
	private double participation;
	private int strength;
	private int averageEloRating;
	private final String result;

	public PlayerTournamentEvent(int tournamentEventId, int season, LocalDate date, String name, String level, String surface, boolean indoor, Integer speed, String drawType, Integer drawSize, double participation, int strength, int averageEloRating, String result) {
		this.tournamentEventId = tournamentEventId;
		this.season = season;
		this.date = date;
		this.name = name;
		this.level = level;
		this.surface = surface;
		this.indoor = indoor;
		this.speed = speed;
		this.drawType = drawType;
		this.drawSize = drawSize;
		this.participation = participation;
		this.strength = strength;
		this.averageEloRating = averageEloRating;
		this.result = result;
	}

	public int getTournamentEventId() {
		return tournamentEventId;
	}

	public int getSeason() {
		return season;
	}

	public LocalDate getDate() {
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

	public Integer getSpeed() {
		return speed;
	}

	public String getDrawType() {
		return drawType;
	}

	public Integer getDrawSize() {
		return drawSize;
	}

	public String getDraw() {
		return drawType + (drawSize != null ? " " + drawSize : "");
	}

	public double getParticipation() {
		return participation;
	}

	public int getStrength() {
		return strength;
	}

	public int getAverageEloRating() {
		return averageEloRating;
	}

	public String getResult() {
		return mapResult(level, result);
	}

	public String result() {
		return result;
	}
}
