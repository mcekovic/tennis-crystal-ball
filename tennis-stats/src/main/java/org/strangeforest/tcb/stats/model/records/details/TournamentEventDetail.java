package org.strangeforest.tcb.stats.model.records.details;

import java.sql.*;

import com.fasterxml.jackson.annotation.*;

public class TournamentEventDetail {

	private int tournamentEventId;
	private String name;
	private String level;

	public TournamentEventDetail() {}

	public TournamentEventDetail(ResultSet rs, String prefix) throws SQLException {
		tournamentEventId = rs.getInt(prefix + "tournament_event_id");
		name = rs.getString(prefix + "tournament");
		level = rs.getString(prefix + "level");
	}


	@JsonGetter("tournamentEventId")
	public int getTournamentEventId() {
		return tournamentEventId;
	}

	@JsonSetter("tournament_event_id")
	public void setTournamentEventId(int tournamentEventId) {
		this.tournamentEventId = tournamentEventId;
	}

	public String getName() {
		return name;
	}

	public String getLevel() {
		return level;
	}
}
