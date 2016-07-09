package org.strangeforest.tcb.stats.model.records.rows;

import java.sql.*;

public class SeasonIntegerRecordDetail extends IntegerRecordDetail {

	private int season;

	public int getSeason() {
		return season;
	}

	@Override public void read(ResultSet rs, boolean activePlayers) throws SQLException {
		super.read(rs, activePlayers);
		season = rs.getInt("season");
	}
}
