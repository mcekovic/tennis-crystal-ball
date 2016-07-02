package org.strangeforest.tcb.stats.model.records.rows;

import java.sql.*;

public class TournamentEventIntegerRecordRow extends IntegerRecordRow {

	private int season;
	private int tournamentEventId;
	private String tournament;
	private String level;
	private int matches;

	public TournamentEventIntegerRecordRow(int rank, int playerId, String name, String countryId, Boolean active) {
		super(rank, playerId, name, countryId, active);
	}

	public int getSeason() {
		return season;
	}

	public int getTournamentEventId() {
		return tournamentEventId;
	}

	public String getTournament() {
		return tournament;
	}

	public String getLevel() {
		return level;
	}

	public int getMatches() {
		return matches;
	}

	@Override public void read(ResultSet rs, boolean activePlayers) throws SQLException {
		super.read(rs, activePlayers);
		season = rs.getInt("season");
		tournamentEventId = rs.getInt("tournament_event_id");
		tournament = rs.getString("tournament");
		level = rs.getString("level");
		matches = rs.getInt("matches");
	}
}
