package org.strangeforest.tcb.stats.model.records.details;

import java.sql.*;
import java.util.Date;

import com.fasterxml.jackson.annotation.*;

public class RankingDiffRecordDetail extends IntegerRecordDetail {

	private PlayerDetail player2;
	private int value1;
	private int value2;
	private Date date;

	public RankingDiffRecordDetail(@JsonProperty("value") int value) {
		super(value);
	}

	public PlayerDetail getPlayer2() {
		return player2;
	}

	public int getValue1() {
		return value1;
	}

	public int getValue2() {
		return value2;
	}

	public Date getDate() {
		return date;
	}

	public void read(ResultSet rs, boolean activePlayers) throws SQLException {
		player2 = new PlayerDetail(rs, activePlayers);
		value1 = rs.getInt("value1");
		value2 = rs.getInt("value2");
		date = rs.getDate("date");
	}
}
