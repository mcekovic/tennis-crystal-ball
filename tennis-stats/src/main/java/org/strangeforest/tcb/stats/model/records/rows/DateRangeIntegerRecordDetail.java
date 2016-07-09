package org.strangeforest.tcb.stats.model.records.rows;

import java.sql.*;
import java.util.Date;

public class DateRangeIntegerRecordDetail extends IntegerRecordDetail {

	private Date startDate;
	private Date endDate;

	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	@Override public void read(ResultSet rs, boolean activePlayers) throws SQLException {
		super.read(rs, activePlayers);
		startDate = rs.getDate("start_date");
		endDate = rs.getDate("end_date");
	}
}
