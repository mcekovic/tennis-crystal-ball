package org.strangeforest.tcb.stats.model;

import java.util.*;

public class PlayerEventResult {

	private final int season;
	private final Date date;
	private final String level;
	private final String name;
	private final String result;

	public PlayerEventResult(int season, Date date, String level, String name, String result) {
		this.season = season;
		this.date = date;
		this.level = level;
		this.name = name;
		this.result = result;
	}

	public int getSeason() {
		return season;
	}

	public Date getDate() {
		return date;
	}

	public String getLevel() {
		return level;
	}

	public String getLevelName() {
		switch (level) {
			case "G": return "Grand Slam";
			case "F": return "Tour Finals";
			case "M": return "Masters";
			case "A": return "ATP";
			case "O": return "Olympics";
			case "D": return "Davis Cup";
			default: return null;
		}
	}

	public String getName() {
		return name;
	}

	public String getResult() {
		return result;
	}
}
