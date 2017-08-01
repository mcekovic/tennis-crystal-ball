package org.strangeforest.tcb.stats.model;

import java.sql.*;

public class PlayerRankingsRow extends PlayerRow {

	private final int points;
	private final int bestRank;
	private final Date bestRankDate;
	private Integer rankDiff;
	private Integer pointsDiff;
	private Date pointsDate;
	private TournamentEventItem tournamentEvent;

	public PlayerRankingsRow(int rank, int playerId, String name, String countryId, Boolean active, int points, int bestRank, Date bestRankDate) {
		super(rank, playerId, name, countryId, active);
		this.points = points;
		this.bestRank = bestRank;
		this.bestRankDate = bestRankDate;
	}

	public int getPoints() {
		return points;
	}

	public Integer getRankDiff() {
		return rankDiff;
	}

	public void setRankDiff(Integer rankDiff) {
		this.rankDiff = rankDiff;
	}

	public Integer getPointsDiff() {
		return pointsDiff;
	}

	public void setPointsDiff(Integer pointsDiff) {
		this.pointsDiff = pointsDiff;
	}

	public int getBestRank() {
		return bestRank;
	}

	public Date getBestRankDate() {
		return bestRankDate;
	}

	public Date getPointsDate() {
		return pointsDate;
	}

	public void setPointsDate(Date pointsDate) {
		this.pointsDate = pointsDate;
	}

	public TournamentEventItem getTournamentEvent() {
		return tournamentEvent;
	}

	public void setTournamentEvent(TournamentEventItem tournamentEvent) {
		this.tournamentEvent = tournamentEvent;
	}
}
