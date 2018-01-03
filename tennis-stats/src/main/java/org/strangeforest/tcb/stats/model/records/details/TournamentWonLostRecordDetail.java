package org.strangeforest.tcb.stats.model.records.details;

import static java.lang.String.*;

public abstract class TournamentWonLostRecordDetail extends WonLostRecordDetail {

	private final int tournamentId;
	private final String tournament;
	private final String level;

	protected TournamentWonLostRecordDetail(int won, int lost, int tournamentId, String tournament, String level) {
		super(won, lost);
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
		return format("%4$s %1$d-%2$d/%3$d", wonLost.getWon(), wonLost.getLost(), wonLost.getTotal(), getTournament());
	}
}
