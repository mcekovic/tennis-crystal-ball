package org.strangeforest.tcb.stats.model.records;

import java.sql.*;

public class SeasonIntegerRecordRow extends IntegerRecordRow {

	private int season;

	public SeasonIntegerRecordRow(int rank, int playerId, String name, String countryId, Boolean active) {
		super(rank, playerId, name, countryId, active);
	}

	public int getSeason() {
		return season;
	}

	@Override public void read(ResultSet rs, boolean activePlayers) throws SQLException {
		super.read(rs, activePlayers);
		season = rs.getInt("season");
	}
}
