package org.strangeforest.tcb.stats.model.prediction;

import java.time.*;

import org.strangeforest.tcb.stats.model.core.*;
import org.strangeforest.tcb.stats.model.elo.*;

import com.google.common.base.*;

public final class MatchData {

	private final LocalDate date;
	private final int tournamentId;
	private final int tournamentEventId;
	private final boolean inProgress;
	private final TournamentLevel level;
	private final Surface surface;
	private final Round round;
	private final int opponentId;
	private final Integer opponentRank;
	private final Integer opponentEloRating;
	private final String opponentHand;
	private final String opponentBackhand;
	private final String opponentEntry;
	private final int pMatches;
	private final int oMatches;
	private final int pSets;
	private final int oSets;

	public MatchData(LocalDate date, int tournamentId, int tournamentEventId, boolean inProgress, TournamentLevel level, Surface surface, Round round,
	                 int opponentId, Integer opponentRank, Integer opponentEloRating, String opponentHand, String opponentBackhand, String opponentEntry,
	                 int pMatches, int oMatches, int pSets, int oSets) {
		this.date = date;
		this.tournamentId = tournamentId;
		this.tournamentEventId = tournamentEventId;
		this.inProgress = inProgress;
		this.level = level;
		this.surface = surface;
		this.round = round;
		this.opponentId = opponentId;
		this.opponentRank = opponentRank;
		this.opponentEloRating = opponentEloRating;
		this.opponentHand = opponentHand;
		this.opponentBackhand = opponentBackhand;
		this.opponentEntry = opponentEntry;
		this.pMatches = pMatches;
		this.oMatches = oMatches;
		this.pSets = pSets;
		this.oSets = oSets;
	}

	public LocalDate getDate() {
		return date;
	}

	public int getTournamentId() {
		return tournamentId;
	}

	public int getTournamentEventId() {
		return tournamentEventId;
	}

	public boolean isInProgress() {
		return inProgress;
	}

	public TournamentLevel getLevel() {
		return level;
	}

	public Surface getSurface() {
		return surface;
	}

	public Round getRound() {
		return round;
	}

	public int getOpponentId() {
		return opponentId;
	}

	public Integer getOpponentRank() {
		return opponentRank;
	}

	public Integer getOpponentEloRating() {
		return opponentEloRating;
	}

	public double getOpponentEloScore() {
		if (opponentEloRating != null) {
			double ratingDelta = opponentEloRating - StartEloRatings.START_RATING;
			if (ratingDelta > 0.0) {
				if (oMatches > pMatches)
					ratingDelta = -ratingDelta;
				return ratingDelta;
			}
		}
		return 0.0;
	}

	public String getOpponentHand() {
		return opponentHand;
	}

	public String getOpponentBackhand() {
		return opponentBackhand;
	}

	public String getOpponentEntry() {
		return opponentEntry;
	}

	public int getPMatches() {
		return pMatches;
	}

	public int getOMatches() {
		return oMatches;
	}

	public int getMatches() {
		return pMatches + oMatches;
	}

	public int getPSets() {
		return pSets;
	}

	public int getOSets() {
		return oSets;
	}

	public int getSets() {
		return pSets + oSets;
	}

	@Override public String toString() {
		return MoreObjects.toStringHelper(this).omitNullValues()
			.add("date", date)
			.add("tournamentId", tournamentId)
			.add("tournamentEventId", tournamentEventId)
			.add("inProgress", inProgress)
			.add("level", level)
			.add("surface", surface)
			.add("round", round)
			.add("opponentId", opponentId)
			.add("opponentRank", opponentRank)
			.add("opponentEloRating", opponentEloRating)
			.add("opponentHand", opponentHand)
			.add("opponentBackhand", opponentBackhand)
			.add("opponentEntry", opponentEntry)
			.add("pMatches", pMatches)
			.add("oMatches", oMatches)
			.add("pSets", pSets)
			.add("oSets", oSets)
		.toString();
	}
}
