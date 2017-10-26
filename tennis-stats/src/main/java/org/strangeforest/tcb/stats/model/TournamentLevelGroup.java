package org.strangeforest.tcb.stats.model;

import java.util.*;

import static java.util.stream.Collectors.*;
import static org.strangeforest.tcb.stats.model.TournamentLevel.*;

public enum TournamentLevelGroup {

	BIG(EnumSet.of(GRAND_SLAM, TOUR_FINALS, ALT_FINALS, MASTERS, OLYMPICS), "Big (GS/TF/AF/M/O)"),
	SMALL(EnumSet.of(ATP_500, ATP_250), "Small (500/250)"),
	ALL_FINALS(EnumSet.of(TOUR_FINALS, ALT_FINALS), "All Finals (TF/AF)"),
	INDIVIDUAL(EnumSet.of(GRAND_SLAM, TOUR_FINALS, ALT_FINALS, MASTERS, OLYMPICS, ATP_500, ATP_250), "Individual"),
	TEAM(EnumSet.of(DAVIS_CUP, OTHERS_TEAM), "Team"),
	BEST_OF_5(EnumSet.of(GRAND_SLAM, ALT_FINALS, DAVIS_CUP), "Best of 5"),
	BEST_OF_3(EnumSet.of(TOUR_FINALS, MASTERS, OLYMPICS, ATP_500, ATP_250, OTHERS_TEAM), "Best of 3"),
	BEST_OF_5_IND(EnumSet.of(GRAND_SLAM, ALT_FINALS), "Best of 5"),
	BEST_OF_3_IND(EnumSet.of(TOUR_FINALS, MASTERS, OLYMPICS, ATP_500, ATP_250), "Best of 3");

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

	public static EnumSet<TournamentLevelGroup> INDIVIDUAL_LEVEL_GROUPS = EnumSet.of(BIG, SMALL, ALL_FINALS, BEST_OF_5_IND, BEST_OF_3_IND);
	public static EnumSet<TournamentLevelGroup> ALL_LEVEL_GROUPS = EnumSet.of(BIG, SMALL, ALL_FINALS, INDIVIDUAL, TEAM, BEST_OF_5, BEST_OF_3);
}
