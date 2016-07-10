package org.strangeforest.tcb.stats.model.records.details;

import java.sql.*;

import com.fasterxml.jackson.annotation.*;

public class PlayerDetail {

	private int playerId;
	private String name;
	private String countryId;
	private Boolean active;

	public PlayerDetail() {}

	public PlayerDetail(ResultSet rs, boolean activePlayers) throws SQLException {
		playerId = rs.getInt("player_id2");
		name = rs.getString("name2");
		countryId = rs.getString("country_id2");
		active = !activePlayers ? rs.getBoolean("active2") : null;
	}

	@JsonGetter("playerId")
	public int getPlayerId() {
		return playerId;
	}

	@JsonSetter("player_id2")
	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}

	@JsonGetter("name")
	public String getName() {
		return name;
	}

	@JsonSetter("name2")
	public void setName(String name) {
		this.name = name;
	}

	@JsonGetter("countryId")
	public String getCountryId() {
		return countryId;
	}

	@JsonSetter("country_id2")
	public void setCountryId(String countryId) {
		this.countryId = countryId;
	}

	@JsonGetter("active")
	public Boolean getActive() {
		return active;
	}

	@JsonSetter("active2")
	public void setActive(Boolean active) {
		this.active = active;
	}
}
