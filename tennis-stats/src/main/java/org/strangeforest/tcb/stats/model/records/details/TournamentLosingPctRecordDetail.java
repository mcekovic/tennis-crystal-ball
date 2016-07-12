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

	public String getLostWonPct() {
		return wonLost.inverted().getWonPctStr(2);
	}

	public int getLost() {
		return wonLost.getLost();
	}

	public int getPlayed() {
		return wonLost.getTotal();
	}

	@Override public String toString() {
		return format("%1$s (%2$d/%3$d in %4$s)", getLostWonPct(), wonLost.getLost(), wonLost.getTotal(), getTournament());
	}
}
