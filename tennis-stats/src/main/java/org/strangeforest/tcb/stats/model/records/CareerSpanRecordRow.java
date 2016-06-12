package org.strangeforest.tcb.stats.model.records;

import java.sql.*;
import java.util.Date;

import static org.strangeforest.tcb.stats.model.records.RecordTournamentEvent.*;

public class CareerSpanRecordRow extends RecordRow {

	private String span;
	private Date startDate;
	private RecordTournamentEvent startEvent;
	private Date endDate;
	private RecordTournamentEvent endEvent;

	public CareerSpanRecordRow(int rank, int playerId, String name, String countryId, Boolean active) {
		super(rank, playerId, name, countryId, active);
	}

	public String getSpan() {
		return span;
	}

	public Date getStartDate() {
		return startDate;
	}

	public RecordTournamentEvent getStartEvent() {
		return startEvent;
	}

	public Date getEndDate() {
		return endDate;
	}

	public RecordTournamentEvent getEndEvent() {
		return endEvent;
	}

	@Override public void read(ResultSet rs) throws SQLException {
		span = rs.getString("span");
		startDate = rs.getDate("start_date");
		startEvent = readTournamentEvent(rs, "start_");
		endDate = rs.getDate("end_date");
		endEvent = readTournamentEvent(rs, "end_");
	}
}
