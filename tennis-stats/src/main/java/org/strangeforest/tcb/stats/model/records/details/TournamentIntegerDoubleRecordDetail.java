package org.strangeforest.tcb.stats.model.records.details;

import com.fasterxml.jackson.annotation.*;

import static java.lang.String.*;

public class TournamentIntegerDoubleRecordDetail extends IntegerDoubleRecordDetail {

	private final int tournamentId;
	private final String tournament;
	private final String level;

	public TournamentIntegerDoubleRecordDetail(
		@JsonProperty("value") double value,
		@JsonProperty("int_value") int intValue,
		@JsonProperty("tournament_id") int tournamentId,
		@JsonProperty("tournament") String tournament,
		@JsonProperty("level") String level
	) {
		super(value, intValue);
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

	@Override public String toDetailString() {
		return format("%1$s - %2$d", tournament, getIntValue());
	}
}
