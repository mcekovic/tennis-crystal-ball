package org.strangeforest.tcb.stats.model.records.rows;

import java.sql.*;

import static org.strangeforest.tcb.stats.model.records.rows.RecordTournamentEvent.*;

public class TournamentCareerSpanRecordDetail extends CareerSpanRecordDetail {

	private RecordTournamentEvent startEvent;
	private RecordTournamentEvent endEvent;

	public RecordTournamentEvent getStartEvent() {
		return startEvent;
	}

	public RecordTournamentEvent getEndEvent() {
		return endEvent;
	}

	@Override public void read(ResultSet rs, boolean activePlayers) throws SQLException {
		super.read(rs, activePlayers);
		startEvent = readTournamentEvent(rs, "start_");
		endEvent = readTournamentEvent(rs, "end_");
	}
}
