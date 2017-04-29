package org.strangeforest.tcb.stats.model;

import java.util.*;

import static java.util.stream.Collectors.*;
import static org.strangeforest.tcb.stats.model.TournamentLevel.*;

public enum TournamentLevelGroup {

	BIG(EnumSet.of(GRAND_SLAM, TOUR_FINALS, MASTERS, OLYMPICS), "Big Tournaments"),
	SMALL(EnumSet.of(ATP_500, ATP_250), "Small Tournaments"),
	NON_TEAM(EnumSet.of(GRAND_SLAM, TOUR_FINALS, MASTERS, OLYMPICS, ATP_500, ATP_250), "Non-Team Tournaments"),
	TEAM(EnumSet.of(DAVIS_CUP, OTHERS_TEAM), "Team Tournaments");

	private final String codes;
	private final String text;

	TournamentLevelGroup(EnumSet<TournamentLevel> levels, String text) {
		codes = levels.stream().map(TournamentLevel::getCode).collect(joining());
		this.text = text;
	}

	public String getCodes() {
		return codes;
	}

	public String getText() {
		return text;
	}

	public static EnumSet<TournamentLevelGroup> NON_TEAM_LEVEL_GROUPS = EnumSet.of(BIG, SMALL);
	public static EnumSet<TournamentLevelGroup> ALL_LEVEL_GROUPS = EnumSet.of(BIG, SMALL, NON_TEAM, TEAM);
}
