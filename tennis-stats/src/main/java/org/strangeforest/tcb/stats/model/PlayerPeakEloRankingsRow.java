package org.strangeforest.tcb.stats.model;

import java.util.*;

public class PlayerPeakEloRankingsRow extends PlayerRankingsRow {

	private final Date pointsDate;
	private final TournamentEventItem tournamentEvent;

	public PlayerPeakEloRankingsRow(int rank, int playerId, String name, String countryId, Boolean active, int points, Date pointsDate, int bestRank, Date bestRankDate, TournamentEventItem tournamentEvent) {
		super(rank, playerId, name, countryId, active, points, bestRank, bestRankDate);
		this.pointsDate = pointsDate;
		this.tournamentEvent = tournamentEvent;
	}

	public Date getPointsDate() {
		return pointsDate;
	}

	public TournamentEventItem getTournamentEvent() {
		return tournamentEvent;
	}
}
