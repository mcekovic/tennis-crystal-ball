package org.strangeforest.tcb.stats.model.records;

import java.sql.*;

public class SeasonRangeIntegerRecordRow extends IntegerRecordRow {

	private int startSeason;
	private int endSeason;

	public SeasonRangeIntegerRecordRow(int rank, int playerId, String name, String countryId, Boolean active) {
		super(rank, playerId, name, countryId, active);
	}

	public int getStartSeason() {
		return startSeason;
	}

	public int getEndSeason() {
		return endSeason;
	}

	@Override public void read(ResultSet rs) throws SQLException {
		super.read(rs);
		startSeason = rs.getInt("start_season");
		endSeason = rs.getInt("end_season");
	}
}
