package org.strangeforest.tcb.stats.model.records;

import java.sql.*;
import java.util.Date;

public class DateIntegerRecordRow extends IntegerRecordRow {

	private Date date;

	public DateIntegerRecordRow(int rank, int playerId, String name, String countryId, Boolean active) {
		super(rank, playerId, name, countryId, active);
	}

	public Date getDate() {
		return date;
	}

	@Override public void read(ResultSet rs, boolean activePlayers) throws SQLException {
		super.read(rs, activePlayers);
		date = rs.getDate("date");
	}
}
