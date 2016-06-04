package org.strangeforest.tcb.stats.model.records;

import java.sql.*;

public class TournamentIntegerRecordRow extends IntegerRecordRow {

	private int tournamentId;
	private String tournament;
	private String level;

	public TournamentIntegerRecordRow(int rank, int playerId, String name, String countryId, Boolean active) {
		super(rank, playerId, name, countryId, active);
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

	@Override public void read(ResultSet rs) throws SQLException {
		super.read(rs);
		tournamentId = rs.getInt("tournament_id");
		tournament = rs.getString("tournament");
		level = rs.getString("level");
	}
}
