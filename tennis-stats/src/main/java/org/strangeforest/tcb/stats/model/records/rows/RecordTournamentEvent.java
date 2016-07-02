package org.strangeforest.tcb.stats.model.records.rows;

import java.sql.*;

public class RecordTournamentEvent {

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

	static RecordTournamentEvent readTournamentEvent(ResultSet rs, String prefix) throws SQLException {
		RecordTournamentEvent event = new RecordTournamentEvent();
		event.tournamentEventId = rs.getInt(prefix + "tournament_event_id");
		event.name = rs.getString(prefix + "tournament");
		event.level = rs.getString(prefix + "level");
		return event;
	}
}
