package org.strangeforest.tcb.stats.model.records;

import java.sql.*;

public interface RecordDetail {

	void read(ResultSet rs, boolean activePlayers) throws SQLException;
}
