package org.strangeforest.tcb.stats.model.records;

public class TournamentWinningPctRecordRow extends TournamentWonLostRecordRow {

	public TournamentWinningPctRecordRow(int rank, int playerId, String name, String countryId, Boolean active) {
		super(rank, playerId, name, countryId, active);
	}

	public String getWonLostPct() {
		return wonLost.getWonPctStr(2);
	}

	public int getWon() {
		return wonLost.getWon();
	}

	public int getPlayed() {
		return wonLost.getTotal();
	}
}
