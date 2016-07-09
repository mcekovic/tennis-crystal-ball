package org.strangeforest.tcb.stats.model.records.rows;

import java.sql.*;

import static org.strangeforest.tcb.stats.model.records.rows.RecordTournamentEvent.*;

public class StreakRecordDetail extends IntegerRecordDetail {

	private int startSeason;
	private RecordTournamentEvent startEvent;
	private int endSeason;
	private RecordTournamentEvent endEvent;

	public int getStartSeason() {
		return startSeason;
	}

	public RecordTournamentEvent getStartEvent() {
		return startEvent;
	}

	public int getEndSeason() {
		return endSeason;
	}

	public RecordTournamentEvent getEndEvent() {
		return endEvent;
	}

	@Override public void read(ResultSet rs, boolean activePlayers) throws SQLException {
		super.read(rs, activePlayers);
		startSeason = rs.getInt("start_season");
		startEvent = readTournamentEvent(rs, "start_");
		endSeason = rs.getInt("end_season");
		endEvent = readTournamentEvent(rs, "end_");
	}
}
