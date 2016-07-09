package org.strangeforest.tcb.stats.model.records.rows;

import java.sql.*;
import java.util.Date;

import org.strangeforest.tcb.stats.model.records.*;

public class CareerSpanRecordDetail implements RecordDetail {

	private String span;
	private Date startDate;
	private Date endDate;

	public String getSpan() {
		return span;
	}

	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	@Override public void read(ResultSet rs, boolean activePlayers) throws SQLException {
		span = rs.getString("span");
		startDate = rs.getDate("start_date");
		endDate = rs.getDate("end_date");
	}
}
