package org.strangeforest.tcb.stats.model;

import java.util.*;

import org.strangeforest.tcb.stats.util.*;

public enum TournamentLevel implements CodedEnum {

	GRAND_SLAM("G", "Grand Slam"),
	TOUR_FINALS("F", "Tour Finals"),
	MASTERS("M", "Masters"),
	OLYMPICS("O", "Olympics"),
	ATP_500("A", "ATP 500"),
	ATP_250("B", "ATP 250"),
	CHALLENGERS("C", "Challengers"),
	FUTURES("U", "Futures"),
	EXHIBITIONS("E", "Exhibitions"),
	OTHERS("H", "Others"),
	DAVIS_CUP("D", "Davis Cup"),
	OTHERS_TEAM("T", "Others Team");

	private final String code;
	private final String text;

	TournamentLevel(String code, String text) {
		this.code = code;
		this.text = text;
	}

	@Override public String getCode() {
		return code;
	}

	@Override public String getText() {
		return text;
	}

	public static TournamentLevel decode(String code) {
		return CodedEnum.decode(TournamentLevel.class, code);
	}

	public static Map<String, String> asMap() {
		return CodedEnum.asMap(TournamentLevel.class);
	}

	public static EnumSet<TournamentLevel> MAIN_TOURNAMENT_LEVELS = EnumSet.of(GRAND_SLAM, TOUR_FINALS, MASTERS, OLYMPICS, ATP_500, ATP_250);
	public static EnumSet<TournamentLevel> TOURNAMENT_LEVELS = EnumSet.of(GRAND_SLAM, TOUR_FINALS, MASTERS, OLYMPICS, ATP_500, ATP_250, DAVIS_CUP);
	public static EnumSet<TournamentLevel> ALL_TOURNAMENT_LEVELS = EnumSet.of(GRAND_SLAM, TOUR_FINALS, MASTERS, OLYMPICS, ATP_500, ATP_250, OTHERS, DAVIS_CUP, OTHERS_TEAM);

	public static String mapResult(String level, String result) {
		if (Objects.equals(level, "O")) {
			switch (result) {
				case "W": return "G";
				case "F": return "S";
				default: return result;
			}
		}
		else
			return result;
	}
}
