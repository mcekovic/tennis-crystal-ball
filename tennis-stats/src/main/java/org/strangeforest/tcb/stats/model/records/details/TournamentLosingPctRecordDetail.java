package org.strangeforest.tcb.stats.model.records.details;

import com.fasterxml.jackson.annotation.*;

public class TournamentLosingPctRecordDetail extends TournamentWonLostRecordDetail {

	public TournamentLosingPctRecordDetail(
		@JsonProperty("won") int won,
		@JsonProperty("lost") int lost,
		@JsonProperty("tournament_id") int tournamentId,
		@JsonProperty("tournament") String tournament,
		@JsonProperty("level") String level
	) {
		super(won, lost, tournamentId, tournament, level);
	}

	@Override public String getValue() {
		return wonLost.inverted().getWonPctStr(2);
	}
}
