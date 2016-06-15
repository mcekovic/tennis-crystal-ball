package org.strangeforest.tcb.stats.model.records;

import java.sql.*;
import java.util.Date;

public class CareerSpanRecordRow extends RecordRow {

	private String span;
	private Date startDate;
	private Date endDate;

	public CareerSpanRecordRow(int rank, int playerId, String name, String countryId, Boolean active) {
		super(rank, playerId, name, countryId, active);
	}

	public String getSpan() {
		return span;
	}

	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	@Override public void read(ResultSet rs) throws SQLException {
		span = rs.getString("span");
		startDate = rs.getDate("start_date");
		endDate = rs.getDate("end_date");
	}
}
