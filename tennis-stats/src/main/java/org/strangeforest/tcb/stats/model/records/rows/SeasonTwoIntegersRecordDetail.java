package org.strangeforest.tcb.stats.model.records.rows;

import java.sql.*;

public class SeasonTwoIntegersRecordDetail extends IntegerRecordDetail {

	private int value2;
	private int season;

	public int getValue2() {
		return value2;
	}

	public int getSeason() {
		return season;
	}

	@Override public void read(ResultSet rs, boolean activePlayers) throws SQLException {
		super.read(rs, activePlayers);
		value2 = rs.getInt("value2");
		season = rs.getInt("season");
	}
}
