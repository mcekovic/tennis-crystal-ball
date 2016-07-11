package org.strangeforest.tcb.stats.model.records.details;

import java.util.*;

import org.strangeforest.tcb.stats.model.*;

import com.fasterxml.jackson.annotation.*;

public class RankingDiffRecordDetail extends IntegerRecordDetail {

	private final PlayerRow player2;
	private final int value1;
	private final int value2;
	private final Date date;

	public RankingDiffRecordDetail(
		@JsonProperty("value") int value,
		@JsonProperty("player_id2") int playerId2,
		@JsonProperty("name2") String name2,
		@JsonProperty("country_id2") String countryId2,
		@JsonProperty("active2") Boolean active2,
		@JsonProperty("value1") int value1,
		@JsonProperty("value2") int value2,
		@JsonProperty("date") Date date
	) {
		super(value);
		this.value1 = value1;
		this.value2 = value2;
		this.date = date;
		player2 = new PlayerRow(2, playerId2, name2, countryId2, active2);
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
}
