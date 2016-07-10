package org.strangeforest.tcb.stats.model.records.details;

import java.sql.*;

import com.fasterxml.jackson.annotation.*;

public abstract class TournamentWonLostRecordDetail extends WonLostRecordDetail {

	private int tournamentId;
	private String tournament;
	private String level;

	@JsonGetter("tournamentId")
	public int getTournamentId() {
		return tournamentId;
	}

	@JsonSetter("tournament_id")
	public void setTournamentId(int tournamentId) {
		this.tournamentId = tournamentId;
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
