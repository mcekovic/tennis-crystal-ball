package org.strangeforest.tcb.stats.model.records.details;

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
}
