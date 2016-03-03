package org.strangeforest.tcb.stats.model;

import java.util.*;

import static org.strangeforest.tcb.stats.util.PercentageUtil.*;

public class TournamentEvent {

	private final int id;
	private final int tournamentId;
	private final String tournamentExtId;
	private final int season;
	private final Date date;
	private final String name;
	private final String level;
	private final String surface;
	private final boolean indoor;
	private String drawType;
	private Integer drawSize;
	private int playerCount;
	private int participationPoints;
	private int maxParticipationPoints;
	private double participationPct;
	private MatchPlayer winner;
	private MatchPlayer loser;
	private String score;

	public TournamentEvent(int id, int tournamentId, String tournamentExtId, int season, Date date, String name, String level, String surface, boolean indoor) {
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

	public void setDraw(String drawType, Integer drawSize, int playerCount, int participationPoints, int maxParticipationPoints) {
		this.drawType = drawType;
		this.drawSize = drawSize;
		this.playerCount = playerCount;
		this.participationPoints = participationPoints;
		this.maxParticipationPoints = maxParticipationPoints;
		participationPct = pct(participationPoints, maxParticipationPoints);
	}

	public void setFinal(MatchPlayer winner, MatchPlayer loser, String score) {
		this.winner = winner;
		this.loser = loser;
		this.score = score;
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

	public Integer getDrawSize() {
		return drawSize;
	}

	public String getDraw() {
		return drawType + (drawSize != null ? " " + drawSize : "");
	}

	public int getPlayerCount() {
		return playerCount;
	}

	public int getParticipationPoints() {
		return participationPoints;
	}

	public int getMaxParticipationPoints() {
		return maxParticipationPoints;
	}

	public double getParticipationPct() {
		return participationPct;
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
