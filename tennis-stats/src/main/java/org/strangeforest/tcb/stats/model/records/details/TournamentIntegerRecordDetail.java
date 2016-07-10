package org.strangeforest.tcb.stats.model.records.details;

import com.fasterxml.jackson.annotation.*;

public class TournamentIntegerRecordDetail extends IntegerRecordDetail {

	private final int tournamentId;
	private final String tournament;
	private final String level;

	public TournamentIntegerRecordDetail(
		@JsonProperty("value") int value,
		@JsonProperty("tournament_id") int tournamentId,
		@JsonProperty("tournament") String tournament,
		@JsonProperty("level") String level
	) {
		super(value);
		this.tournamentId = tournamentId;
		this.tournament = tournament;
		this.level = level;
	}

	public int getTournamentId() {
		return tournamentId;
	}

	public String getTournament() {
		return tournament;
	}

	public String getLevel() {
		return level;
	}
}
