package org.strangeforest.tcb.stats.model;

import java.sql.*;

public class PlayerRankingsRow extends PlayerRow {

	private final int points;
	private final int bestRank;
	private final Date bestRankDate;
	private Date pointsDate;

	public PlayerRankingsRow(int rank, int playerId, String name, String countryId, boolean active, int points, int bestRank, Date bestRankDate) {
		super(rank, playerId, name, countryId, active);
		this.points = points;
		this.bestRank = bestRank;
		this.bestRankDate = bestRankDate;
	}

	public int getPoints() {
		return points;
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
}
