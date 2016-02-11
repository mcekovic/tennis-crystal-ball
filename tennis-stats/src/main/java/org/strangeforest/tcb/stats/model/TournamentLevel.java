package org.strangeforest.tcb.stats.model;

public enum TournamentLevel {

	GRAND_SLAM("G"),
	TOUR_FINALS("F"),
	MASTERS("M"),
	OLYMPICS("O"),
	ATP("A"),
	DAVIS_CUP("D"),
	WORLD_TEAM_CUP("T"),
	OTHERS("H"),
	CHALLENGERS("C"),
	FUTURES("U");

	private final String code;

	TournamentLevel(String code) {
		this.code = code;
	}

	public String code() {
		return code;
	}

	public static TournamentLevel forCode(String code) {
		for (TournamentLevel level : TournamentLevel.values()) {
			if (level.code.equals(code))
				return level;
		}
		throw new IllegalArgumentException("Cannot find TournamentLevel for code: " + code);
	}
}
