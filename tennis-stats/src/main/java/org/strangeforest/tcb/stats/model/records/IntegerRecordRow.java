package org.strangeforest.tcb.stats.model.records;

import java.sql.*;

public class IntegerRecordRow extends RecordRow {

	private int value;

	public IntegerRecordRow(int rank, int playerId, String name, String countryId, boolean active) {
		super(rank, playerId, name, countryId, active);
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	@Override public void readValues(ResultSet rs) throws SQLException {
		value = rs.getInt("value");
	}
}
