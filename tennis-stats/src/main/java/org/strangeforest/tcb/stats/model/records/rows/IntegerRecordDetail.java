package org.strangeforest.tcb.stats.model.records.rows;

import java.sql.*;

import org.strangeforest.tcb.stats.model.records.*;

public class IntegerRecordDetail implements RecordDetail {

	private int value;

	public IntegerRecordDetail() {}

	public IntegerRecordDetail(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	@Override public void read(ResultSet rs, boolean activePlayers) throws SQLException {
		value = rs.getInt("value");
	}
}
