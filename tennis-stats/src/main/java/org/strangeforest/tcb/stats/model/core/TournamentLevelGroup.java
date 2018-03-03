package org.strangeforest.tcb.stats.model.core;

import java.util.*;

import org.strangeforest.tcb.stats.util.*;

import static java.util.stream.Collectors.*;
import static org.strangeforest.tcb.stats.model.core.TournamentLevel.*;

public enum TournamentLevelGroup implements CodedEnum {

	BIG(EnumSet.of(GRAND_SLAM, TOUR_FINALS, ALT_FINALS, MASTERS, OLYMPICS), "Big (GS/TF/AF/M/O)"),
	ALL_FINALS(EnumSet.of(TOUR_FINALS, ALT_FINALS), "All Finals (TF/AF)"),
	MEDIUM(EnumSet.of(MASTERS, OLYMPICS), "Medium (M/O)"),
	SMALL(EnumSet.of(ATP_500, ATP_250), "Small (500/250)"),
	INDIVIDUAL(EnumSet.of(GRAND_SLAM, TOUR_FINALS, ALT_FINALS, MASTERS, OLYMPICS, ATP_500, ATP_250), "Individual"),
	TEAM(EnumSet.of(DAVIS_CUP, OTHERS_TEAM), "Team"),
	BEST_OF_5(EnumSet.of(GRAND_SLAM, ALT_FINALS, DAVIS_CUP), "Best of 5"),
	BEST_OF_3(EnumSet.of(TOUR_FINALS, MASTERS, OLYMPICS, ATP_500, ATP_250, OTHERS_TEAM), "Best of 3"),
	BEST_OF_5_IND(EnumSet.of(GRAND_SLAM, ALT_FINALS), "Best of 5"),
	BEST_OF_3_IND(EnumSet.of(TOUR_FINALS, MASTERS, OLYMPICS, ATP_500, ATP_250), "Best of 3");

	private final EnumSet<TournamentLevel> levels;
	private final String codes;
	private final String text;

	TournamentLevelGroup(EnumSet<TournamentLevel> levels, String text) {
		this.levels = levels;
		codes = levels.stream().map(TournamentLevel::getCode).collect(joining());
		this.text = text;
	}

	public EnumSet<TournamentLevel> getLevels() {
		return levels;
	}

	@Override public String getCode() {
		return codes;
	}

	public String getCodes() {
		return codes;
	}

	@Override public String getText() {
		return text;
	}

	public static TournamentLevelGroup decode(String code) {
		return CodedEnum.decode(TournamentLevelGroup.class, code);
	}

	public static EnumSet<TournamentLevelGroup> INDIVIDUAL_LEVEL_GROUPS = EnumSet.of(BIG, ALL_FINALS, MEDIUM, SMALL, BEST_OF_5_IND, BEST_OF_3_IND);
	public static EnumSet<TournamentLevelGroup> ALL_LEVEL_GROUPS = EnumSet.of(BIG, ALL_FINALS, MEDIUM, SMALL, INDIVIDUAL, TEAM, BEST_OF_5, BEST_OF_3);
}
