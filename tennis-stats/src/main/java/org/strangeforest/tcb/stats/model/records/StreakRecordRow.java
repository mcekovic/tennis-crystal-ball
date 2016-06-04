package org.strangeforest.tcb.stats.model.records;

import java.sql.*;

import static org.strangeforest.tcb.stats.model.records.StreakTournamentEvent.*;

public class StreakRecordRow extends IntegerRecordRow {

	private int startSeason;
	private StreakTournamentEvent startEvent;
	private int endSeason;
	private StreakTournamentEvent endEvent;

	public StreakRecordRow(int rank, int playerId, String name, String countryId, Boolean active) {
		super(rank, playerId, name, countryId, active);
	}

	public int getStartSeason() {
		return startSeason;
	}

	public StreakTournamentEvent getStartEvent() {
		return startEvent;
	}

	public int getEndSeason() {
		return endSeason;
	}

	public StreakTournamentEvent getEndEvent() {
		return endEvent;
	}

	@Override public void read(ResultSet rs) throws SQLException {
		super.read(rs);
		startSeason = rs.getInt("start_season");
		startEvent = readTournamentEvent(rs, "start_");
		endSeason = rs.getInt("end_season");
		endEvent = readTournamentEvent(rs, "end_");
	}
}
