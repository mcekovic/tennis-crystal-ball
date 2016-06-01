package org.strangeforest.tcb.stats.model.records;

import java.sql.*;

public class TournamentEventIntegerRecordRow extends TournamentIntegerRecordRow {

	private int season;
	private int matches;

	public TournamentEventIntegerRecordRow(int rank, int playerId, String name, String countryId, Boolean active) {
		super(rank, playerId, name, countryId, active);
	}

	public int getSeason() {
		return season;
	}

	public int getMatches() {
		return matches;
	}

	@Override public void readValues(ResultSet rs) throws SQLException {
		super.readValues(rs);
		season = rs.getInt("season");
		matches = rs.getInt("matches");
	}
}
