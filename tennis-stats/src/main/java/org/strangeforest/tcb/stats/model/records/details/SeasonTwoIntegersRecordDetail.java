package org.strangeforest.tcb.stats.model.records.details;

import java.sql.*;

import com.fasterxml.jackson.annotation.*;

public class SeasonTwoIntegersRecordDetail extends IntegerRecordDetail {

	private int value2;
	private int season;

	public SeasonTwoIntegersRecordDetail(@JsonProperty("value") int value) {
		super(value);
	}

	public int getValue2() {
		return value2;
	}

	public int getSeason() {
		return season;
	}

	public void read(ResultSet rs, boolean activePlayers) throws SQLException {
		value2 = rs.getInt("value2");
		season = rs.getInt("season");
	}
}
