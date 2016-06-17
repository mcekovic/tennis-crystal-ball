package org.strangeforest.tcb.stats.model.records;

import java.sql.*;

import static org.strangeforest.tcb.stats.model.records.RecordTournamentEvent.*;

public class StreakRecordRow extends IntegerRecordRow {

	private int startSeason;
	private RecordTournamentEvent startEvent;
	private int endSeason;
	private RecordTournamentEvent endEvent;

	public StreakRecordRow(int rank, int playerId, String name, String countryId, Boolean active) {
		super(rank, playerId, name, countryId, active);
	}

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
