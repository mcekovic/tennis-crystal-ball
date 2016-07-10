package org.strangeforest.tcb.stats.model.records.details;

import java.sql.*;

import com.fasterxml.jackson.annotation.*;

public class StreakRecordDetail extends IntegerRecordDetail {

	private int startSeason;
	private TournamentEventDetail startEvent;
	private int endSeason;
	private TournamentEventDetail endEvent;

	public StreakRecordDetail(@JsonProperty("value") int value) {
		super(value);
	}

	@JsonGetter("startSeason")
	public int getStartSeason() {
		return startSeason;
	}

	@JsonSetter("start_season")
	public void setStartSeason(int startSeason) {
		this.startSeason = startSeason;
	}

	public TournamentEventDetail getStartEvent() {
		return startEvent;
	}


	@JsonGetter("endSeason")
	public int getEndSeason() {
		return endSeason;
	}

	@JsonGetter("end_season")
	public void setEndSeason(int endSeason) {
		this.endSeason = endSeason;
	}

	public TournamentEventDetail getEndEvent() {
		return endEvent;
	}

	public void read(ResultSet rs, boolean activePlayers) throws SQLException {
		startSeason = rs.getInt("start_season");
		startEvent = new TournamentEventDetail(rs, "start_");
		endSeason = rs.getInt("end_season");
		endEvent = new TournamentEventDetail(rs, "end_");
	}
}
