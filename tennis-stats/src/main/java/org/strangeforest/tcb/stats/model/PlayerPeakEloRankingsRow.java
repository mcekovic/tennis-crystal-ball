package org.strangeforest.tcb.stats.model;

import java.time.*;

import org.strangeforest.tcb.stats.model.core.*;

public class PlayerPeakEloRankingsRow extends PlayerRankingsRow {

	private final LocalDate bestRatingDate;
	private final TournamentEventItem tournamentEvent;
	private final Integer bestRatingAge;

	public PlayerPeakEloRankingsRow(int rank, int playerId, String name, String countryId, Boolean active, int points, LocalDate bestRatingDate, int bestRank, LocalDate bestRankDate, TournamentEventItem tournamentEvent, Integer bestRatingAge) {
		super(rank, playerId, name, countryId, active, points, bestRank, bestRankDate);
		this.bestRatingDate = bestRatingDate;
		this.tournamentEvent = tournamentEvent;
		this.bestRatingAge = bestRatingAge;
	}

	public LocalDate getBestRatingDate() {
		return bestRatingDate;
	}

	public TournamentEventItem getTournamentEvent() {
		return tournamentEvent;
	}

	public Integer getBestRatingAge() {
		return bestRatingAge;
	}
}
