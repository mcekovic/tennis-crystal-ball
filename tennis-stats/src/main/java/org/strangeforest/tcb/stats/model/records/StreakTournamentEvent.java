package org.strangeforest.tcb.stats.model.records;

import java.sql.*;

public class StreakTournamentEvent {

	private int tournamentEventId;
	private String name;
	private String level;

	public int getTournamentEventId() {
		return tournamentEventId;
	}

	public String getName() {
		return name;
	}

	public String getLevel() {
		return level;
	}

	static StreakTournamentEvent readTournamentEvent(ResultSet rs, String prefix) throws SQLException {
		StreakTournamentEvent event = new StreakTournamentEvent();
		event.tournamentEventId = rs.getInt(prefix + "tournament_event_id");
		event.name = rs.getString(prefix + "tournament");
		event.level = rs.getString(prefix + "level");
		return event;
	}
}
