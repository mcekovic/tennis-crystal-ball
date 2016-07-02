package org.strangeforest.tcb.stats.model.records.rows;

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

	@Override public void read(ResultSet rs, boolean activePlayers) throws SQLException {
		super.read(rs, activePlayers);
		startSeason = rs.getInt("start_season");
		endSeason = rs.getInt("end_season");
	}
}
