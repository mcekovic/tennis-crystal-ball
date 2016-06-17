package org.strangeforest.tcb.stats.model.records;

import java.sql.*;
import java.util.Date;

public class DateRangeIntegerRecordRow extends IntegerRecordRow {

	private Date startDate;
	private Date endDate;

	public DateRangeIntegerRecordRow(int rank, int playerId, String name, String countryId, Boolean active) {
		super(rank, playerId, name, countryId, active);
	}

	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	@Override public void read(ResultSet rs) throws SQLException {
		super.read(rs);
		startDate = rs.getDate("start_date");
		endDate = rs.getDate("end_date");
	}
}
