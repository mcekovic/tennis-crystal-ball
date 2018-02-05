package org.strangeforest.tcb.stats.model.core;

import java.util.*;

import org.strangeforest.tcb.stats.util.*;

public enum TournamentLevel implements CodedEnum {

	GRAND_SLAM("G", "Grand Slam", 5),
	TOUR_FINALS("F", "Tour Finals", 3),
	ALT_FINALS("L", "Alt. Finals", 3),
	MASTERS("M", "Masters", 3),
	OLYMPICS("O", "Olympics", 3),
	ATP_500("A", "ATP 500", 3),
	ATP_250("B", "ATP 250", 3),
	CHALLENGERS("C", "Challengers", 3),
	FUTURES("U", "Futures", 3),
	EXHIBITIONS("E", "Exhibitions", 3),
	OTHERS("H", "Others", 3),
	DAVIS_CUP("D", "Davis Cup", 5),
	OTHERS_TEAM("T", "World Team Cup", 3);

	private final String code;
	private final String text;
	private final short bestOf;

	TournamentLevel(String code, String text, int bestOf) {
		this.code = code;
		this.text = text;
		this.bestOf = (short)bestOf;
	}

	@Override public String getCode() {
		return code;
	}

	@Override public String getText() {
		return text;
	}

	public short getBestOf() {
		return bestOf;
	}

	public static TournamentLevel decode(String code) {
		return CodedEnum.decode(TournamentLevel.class, code);
	}

	public static TournamentLevel safeDecode(String code) {
		return CodedEnum.safeDecode(TournamentLevel.class, code);
	}

	public static Map<String, String> asMap() {
		return CodedEnum.asMap(TournamentLevel.class);
	}

	public static EnumSet<TournamentLevel> MAIN_TOURNAMENT_LEVELS = EnumSet.of(GRAND_SLAM, TOUR_FINALS, ALT_FINALS, MASTERS, OLYMPICS, ATP_500, ATP_250);
	public static EnumSet<TournamentLevel> TOURNAMENT_LEVELS = EnumSet.of(GRAND_SLAM, TOUR_FINALS, ALT_FINALS, MASTERS, OLYMPICS, ATP_500, ATP_250, DAVIS_CUP);
	public static EnumSet<TournamentLevel> ALL_TOURNAMENT_LEVELS = EnumSet.of(GRAND_SLAM, TOUR_FINALS, ALT_FINALS, MASTERS, OLYMPICS, ATP_500, ATP_250, OTHERS, DAVIS_CUP, OTHERS_TEAM);

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
