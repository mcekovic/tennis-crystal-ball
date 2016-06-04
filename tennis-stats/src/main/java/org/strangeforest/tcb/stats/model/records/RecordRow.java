package org.strangeforest.tcb.stats.model.records;

import java.sql.*;

import org.strangeforest.tcb.stats.model.*;

public abstract class RecordRow extends PlayerRow {

	protected RecordRow(int rank, int playerId, String name, String countryId, Boolean active) {
		super(rank, playerId, name, countryId, active);
	}

	public abstract void read(ResultSet rs) throws SQLException;
}
