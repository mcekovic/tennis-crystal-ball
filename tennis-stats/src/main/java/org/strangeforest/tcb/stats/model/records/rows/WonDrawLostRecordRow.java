package org.strangeforest.tcb.stats.model.records.rows;

import java.sql.*;

import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.records.*;

public abstract class WonDrawLostRecordRow extends RecordRow {

	protected WonDrawLost wonDrawLost;

	public WonDrawLostRecordRow(int rank, int playerId, String name, String countryId, Boolean active) {
		super(rank, playerId, name, countryId, active);
	}

	@Override public void read(ResultSet rs, boolean activePlayers) throws SQLException {
		wonDrawLost = new WonDrawLost(rs.getInt("won"), rs.getInt("draw"), rs.getInt("lost"));
	}

	public int getWon() {
		return wonDrawLost.getWon();
	}

	public int getDraw() {
		return wonDrawLost.getDraw();
	}

	public int getLost() {
		return wonDrawLost.getLost();
	}

	public int getPlayed() {
		return wonDrawLost.getTotal();
	}
}
