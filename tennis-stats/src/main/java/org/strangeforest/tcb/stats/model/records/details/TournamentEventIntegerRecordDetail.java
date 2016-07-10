package org.strangeforest.tcb.stats.model.records.details;

import java.sql.*;

import com.fasterxml.jackson.annotation.*;

public class TournamentEventIntegerRecordDetail extends IntegerRecordDetail {

	private int season;
	private int tournamentEventId;
	private String tournament;
	private String level;
	private int matches;

	public TournamentEventIntegerRecordDetail(@JsonProperty("value") int value) {
		super(value);
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

	public int getMatches() {
		return matches;
	}

	public void read(ResultSet rs, boolean activePlayers) throws SQLException {
		season = rs.getInt("season");
		tournamentEventId = rs.getInt("tournament_event_id");
		tournament = rs.getString("tournament");
		level = rs.getString("level");
		matches = rs.getInt("matches");
	}
}
