package org.strangeforest.tcb.stats.model.core;

import java.util.*;

import org.strangeforest.tcb.stats.util.*;

public enum TournamentLevel implements CodedEnum {

	GRAND_SLAM("G", "G", "Grand Slam", 5),
	TOUR_FINALS("F", "FL", "Tour Finals", 3),
	ALT_FINALS("L", "FL", "Alt. Finals", 3),
	MASTERS("M", "MO", "Masters", 3),
	OLYMPICS("O", "MO", "Olympics", 3),
	ATP_500("A", "AB", "ATP 500", 3),
	ATP_250("B", "AB", "ATP 250", 3),
	CHALLENGERS("C", "CUEH", "Challengers", 3),
	FUTURES("U", "CUEH", "Futures", 3),
	EXHIBITIONS("E", "CUEH", "Exhibitions", 3),
	OTHERS("H", "CUEH", "Others", 3),
	DAVIS_CUP("D", "DT", "Davis Cup", 5),
	OTHERS_TEAM("T", "DT", "World Team Cup", 3);

	private final String code;
	private final String predictionCodes;
	private final String text;
	private final short bestOf;

	TournamentLevel(String code, String predictionCodes, String text, int bestOf) {
		this.code = code;
		this.predictionCodes = predictionCodes;
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

	public String getPredictionCodes() {
		return predictionCodes;
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

	private static final String GOLD = "G";
	private static final String SILVER = "S";

	public static String mapResult(String level, String result) {
		if (Objects.equals(level, "O")) {
			switch (result) {
				case "W": return GOLD;
				case "F": return SILVER;
				default: return result;
			}
		}
		else
			return result;
	}
}
