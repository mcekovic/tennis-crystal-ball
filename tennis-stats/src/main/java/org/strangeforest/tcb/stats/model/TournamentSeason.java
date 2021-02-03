package org.strangeforest.tcb.stats.model;

import java.util.*;

public class TournamentSeason {

	private final int tournamentId;
	private final int season;

	public TournamentSeason(int tournamentId, int season) {
		this.tournamentId = tournamentId;
		this.season = season;
	}

	public int getTournamentId() {
		return tournamentId;
	}

	public int getSeason() {
		return season;
	}


	// Object methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof TournamentSeason)) return false;
		var tournament = (TournamentSeason)o;
		return tournamentId == tournament.tournamentId && season == tournament.season;
	}

	@Override public int hashCode() {
		return Objects.hash(tournamentId, season);
	}
}
