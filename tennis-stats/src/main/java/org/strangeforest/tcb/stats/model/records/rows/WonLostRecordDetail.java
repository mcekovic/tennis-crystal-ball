package org.strangeforest.tcb.stats.model.records.rows;

import java.sql.*;

import org.strangeforest.tcb.stats.model.*;
import org.strangeforest.tcb.stats.model.records.*;

public abstract class WonLostRecordDetail implements RecordDetail {

	protected WonLost wonLost;

	@Override public void read(ResultSet rs, boolean activePlayers) throws SQLException {
		wonLost = new WonLost(rs.getInt("won"), rs.getInt("lost"));
	}
}
