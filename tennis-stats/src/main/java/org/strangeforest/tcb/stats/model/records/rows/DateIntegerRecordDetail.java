package org.strangeforest.tcb.stats.model.records.rows;

import java.sql.*;
import java.util.Date;

public class DateIntegerRecordDetail extends IntegerRecordDetail {

	private Date date;

	public Date getDate() {
		return date;
	}

	@Override public void read(ResultSet rs, boolean activePlayers) throws SQLException {
		super.read(rs, activePlayers);
		date = rs.getDate("date");
	}
}
