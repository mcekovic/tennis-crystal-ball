package org.strangeforest.tcb.stats.model.records.details;

import java.sql.*;

import org.strangeforest.tcb.stats.model.records.*;

import com.fasterxml.jackson.annotation.*;

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

	@JsonGetter("tournamentEventId")
	public int getTournamentEventId() {
		return tournamentEventId;
	}

	@JsonSetter("tournament_event_id")
	public void setTournamentEventId(int tournamentEventId) {
		this.tournamentEventId = tournamentEventId;
	}

	public String getTournament() {
		return tournament;
	}

	public String getLevel() {
		return level;
	}

	public void read(ResultSet rs, boolean activePlayers) throws SQLException {
		age = rs.getString("age");
		season = rs.getInt("season");
		tournamentEventId = rs.getInt("tournament_event_id");
		tournament = rs.getString("tournament");
		level = rs.getString("level");
	}
}
