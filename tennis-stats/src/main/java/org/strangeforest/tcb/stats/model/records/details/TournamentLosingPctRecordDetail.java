package org.strangeforest.tcb.stats.model.records.details;

import com.fasterxml.jackson.annotation.*;

import static java.lang.String.*;

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

	public int getLost() {
		return wonLost.getLost();
	}

	@Override public String toDetailString() {
		return format("%3$s %1$d/%2$d", wonLost.getLost(), wonLost.getTotal(), getTournament());
	}
}
