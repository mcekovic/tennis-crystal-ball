package org.strangeforest.tcb.stats.model.records;

import java.sql.*;

public class SeasonTwoIntegersRecordRow extends IntegerRecordRow {

	private int value2;
	private int season;

	public SeasonTwoIntegersRecordRow(int rank, int playerId, String name, String countryId, Boolean active) {
		super(rank, playerId, name, countryId, active);
	}

	public int getValue2() {
		return value2;
	}

	public int getSeason() {
		return season;
	}

	@Override public void read(ResultSet rs) throws SQLException {
		super.read(rs);
		value2 = rs.getInt("value2");
		season = rs.getInt("season");
	}
}
