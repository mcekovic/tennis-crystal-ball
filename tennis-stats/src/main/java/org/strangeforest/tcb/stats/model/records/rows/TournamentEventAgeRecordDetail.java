package org.strangeforest.tcb.stats.model.records.rows;

import java.sql.*;

import org.strangeforest.tcb.stats.model.records.*;

public class TournamentEventAgeRecordDetail implements RecordDetail {

	private String age;
	private int season;
	private int tournamentEventId;
	private String tournament;
	private String level;

	public String getAge() {
		return age;
	}

	public int getSeason() {
		return season;
	}

	public int getTournamentEventId() {
		return tournamentEventId;
	}

	public String getTournament() {
		return tournament;
	}

	public String getLevel() {
		return level;
	}

	@Override public void read(ResultSet rs, boolean activePlayers) throws SQLException {
		age = rs.getString("age");
		season = rs.getInt("season");
		tournamentEventId = rs.getInt("tournament_event_id");
		tournament = rs.getString("tournament");
		level = rs.getString("level");
	}
}
