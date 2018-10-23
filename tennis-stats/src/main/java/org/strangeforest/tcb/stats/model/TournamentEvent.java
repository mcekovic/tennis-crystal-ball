package org.strangeforest.tcb.stats.model;

import java.time.*;

public class TournamentEvent {

	private final int id;
	private final int tournamentId;
	private final String tournamentExtId;
	private final int season;
	private final LocalDate date;
	private final String name;
	private final String level;
	private final String surface;
	private final boolean indoor;
	private String drawType;
	private Integer drawSize;
	private int playerCount;
	private double participation;
	private int strength;
	private int averageEloRating;
	private Integer speed;
	private MatchPlayer winner;
	private MatchPlayer runnerUp;
	private String score;
	private String outcome;
	private Double titleDifficulty;
	private String mapProperties;

	public TournamentEvent(int id, int tournamentId, String tournamentExtId, int season, LocalDate date, String name, String level, String surface, boolean indoor) {
		this.id = id;
		this.tournamentId = tournamentId;
		this.tournamentExtId = tournamentExtId;
		this.season = season;
		this.date = date;
		this.name = name;
		this.level = level;
		this.surface = surface;
		this.indoor = indoor;
	}

	public void setDraw(String drawType, Integer drawSize, int playerCount, double participation, int strength, int averageEloRating) {
		this.drawType = drawType;
		this.drawSize = drawSize;
		this.playerCount = playerCount;
		this.participation = participation;
		this.strength = strength;
		this.averageEloRating = averageEloRating;
	}

	public void setSpeed(Integer speed) {
		this.speed = speed;
	}

	public void setFinal(MatchPlayer winner, MatchPlayer loser, String score, String outcome) {
		this.winner = winner;
		this.runnerUp = loser;
		this.score = score;
		this.outcome = outcome;
	}

	public void clearFinal() {
		winner = null;
		runnerUp = null;
		score = null;
		outcome = null;
	}

	public void setTitleDifficulty(Double titleDifficulty) {
		this.titleDifficulty = titleDifficulty;
	}

	public void setMapProperties(String mapProperties) {
		this.mapProperties = mapProperties;
	}

	public int getId() {
		return id;
	}

	public int getTournamentId() {
		return tournamentId;
	}

	public String getTournamentExtId() {
		return tournamentExtId;
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

	public String getDrawType() {
		return drawType;
	}

	public Integer getDrawSize() {
		return drawSize;
	}

	public String getDraw() {
		return drawType + (drawSize != null ? " " + drawSize : "");
	}

	public int getPlayerCount() {
		return playerCount;
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

	public Integer getSpeed() {
		return speed;
	}

	public MatchPlayer getWinner() {
		return winner;
	}

	public MatchPlayer getRunnerUp() {
		return runnerUp;
	}

	public String getScore() {
		return score;
	}

	public String scoreFormatted() {
		return score.replace("(", "<sup>(").replace(")", ")</sup>");
	}

	public String getOutcome() {
		return outcome;
	}

	public Double getTitleDifficulty() {
		return titleDifficulty;
	}

	public String getMapProperties() {
		return mapProperties;
	}
}
