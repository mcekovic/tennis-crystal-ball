package org.strangeforest.tcb.stats.model.records.rows;

import java.sql.*;

public class TournamentIntegerRecordDetail extends IntegerRecordDetail {

	private int tournamentId;
	private String tournament;
	private String level;

	public int getTournamentId() {
		return tournamentId;
	}

	public String getTournament() {
		return tournament;
	}

	public String getLevel() {
		return level;
	}

	@Override public void read(ResultSet rs, boolean activePlayers) throws SQLException {
		super.read(rs, activePlayers);
		tournamentId = rs.getInt("tournament_id");
		tournament = rs.getString("tournament");
		level = rs.getString("level");
	}
}
