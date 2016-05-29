package org.strangeforest.tcb.stats.model.records;

import java.sql.*;

public class IntegerRecordRow extends RecordRow {

	private int value;

	public IntegerRecordRow(int rank, int playerId, String name, String countryId, Boolean active) {
		super(rank, playerId, name, countryId, active);
	}

	public IntegerRecordRow(int rank, int playerId, String name, String countryId, Boolean active, int value) {
		super(rank, playerId, name, countryId, active);
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	@Override public void readValues(ResultSet rs) throws SQLException {
		value = rs.getInt("value");
	}
}
