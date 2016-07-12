package org.strangeforest.tcb.stats.model.records.details;

import com.fasterxml.jackson.annotation.*;

import static java.lang.String.*;

public class TournamentWinningPctRecordDetail extends TournamentWonLostRecordDetail {

	public TournamentWinningPctRecordDetail(
		@JsonProperty("won") int won,
		@JsonProperty("lost") int lost,
		@JsonProperty("tournament_id") int tournamentId,
		@JsonProperty("tournament") String tournament,
		@JsonProperty("level") String level
	) {
		super(won, lost, tournamentId, tournament, level);
	}

	@Override public String getValue() {
		return wonLost.getWonPctStr(2);
	}

	public int getWon() {
		return wonLost.getWon();
	}

	@Override public String toDetailString() {
		return format("%3$s %1$d/%2$d", wonLost.getWon(), wonLost.getTotal(), getTournament());
	}
}
