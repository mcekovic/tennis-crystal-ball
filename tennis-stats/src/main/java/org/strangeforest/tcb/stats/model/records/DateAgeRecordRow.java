package org.strangeforest.tcb.stats.model.records;

import java.sql.*;
import java.util.Date;

public class DateAgeRecordRow extends RecordRow {

	private String age;
	private Date date;

	public DateAgeRecordRow(int rank, int playerId, String name, String countryId, Boolean active) {
		super(rank, playerId, name, countryId, active);
	}

	public String getAge() {
		return age;
	}

	public Date getDate() {
		return date;
	}

	@Override public void read(ResultSet rs) throws SQLException {
		age = rs.getString("age");
		date = rs.getDate("date");
	}
}
