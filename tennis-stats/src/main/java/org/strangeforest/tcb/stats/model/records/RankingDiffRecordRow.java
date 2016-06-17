package org.strangeforest.tcb.stats.model.records;

import java.sql.*;
import java.util.Date;

import org.strangeforest.tcb.stats.model.*;

public class RankingDiffRecordRow extends IntegerRecordRow {

	private PlayerRow player2;
	private int value1;
	private int value2;
	private Date date;

	public RankingDiffRecordRow(int rank, int playerId, String name, String countryId, Boolean active) {
		super(rank, playerId, name, countryId, active);
	}

	public PlayerRow getPlayer2() {
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

	@Override public void read(ResultSet rs, boolean activePlayers) throws SQLException {
		super.read(rs, activePlayers);
		int playerId = rs.getInt("player_id2");
		String name = rs.getString("name2");
		String countryId = rs.getString("country_id2");
		Boolean active = !activePlayers ? rs.getBoolean("active2") : null;
		player2 = new PlayerRow(2, playerId, name, countryId, active);
		value1 = rs.getInt("value1");
		value2 = rs.getInt("value2");
		date = rs.getDate("date");
	}
}
