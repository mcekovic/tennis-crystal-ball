package org.strangeforest.tcb.stats.model.records.details;

import java.sql.*;

import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.records.*;

public abstract class WonDrawLostRecordDetail implements RecordDetail {

	protected WonDrawLost wonDrawLost;

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

	public void read(ResultSet rs, boolean activePlayers) throws SQLException {
		wonDrawLost = new WonDrawLost(rs.getInt("won"), rs.getInt("draw"), rs.getInt("lost"));
	}
}
