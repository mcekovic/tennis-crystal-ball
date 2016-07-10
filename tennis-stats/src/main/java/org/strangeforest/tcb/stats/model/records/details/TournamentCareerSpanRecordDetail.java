package org.strangeforest.tcb.stats.model.records.details;

import java.sql.*;
import java.util.Date;

import com.fasterxml.jackson.annotation.*;

public class TournamentCareerSpanRecordDetail extends CareerSpanRecordDetail {

	private TournamentEventDetail startEvent;
	private TournamentEventDetail endEvent;

	public TournamentCareerSpanRecordDetail(String span, @JsonProperty("start_date") java.util.Date startDate, @JsonProperty("end_date") Date endDate) {
		super(span, startDate, endDate);
	}

	public TournamentEventDetail getStartEvent() {
		return startEvent;
	}

	public TournamentEventDetail getEndEvent() {
		return endEvent;
	}

	public void read(ResultSet rs, boolean activePlayers) throws SQLException {
		startEvent = new TournamentEventDetail(rs, "start_");
		endEvent = new TournamentEventDetail(rs, "end_");
	}
}
