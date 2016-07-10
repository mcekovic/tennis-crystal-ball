package org.strangeforest.tcb.stats.model.records.details;

import java.sql.*;
import java.util.Date;

import org.strangeforest.tcb.stats.model.records.*;

public class DateAgeRecordDetail implements RecordDetail {

	private String age;
	private Date date;

	public String getAge() {
		return age;
	}

	public Date getDate() {
		return date;
	}

	public void read(ResultSet rs, boolean activePlayers) throws SQLException {
		age = rs.getString("age");
		date = rs.getDate("date");
	}
}
