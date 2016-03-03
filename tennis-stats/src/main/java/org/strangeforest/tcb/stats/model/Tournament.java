package org.strangeforest.tcb.stats.model;

import java.util.*;

public class Tournament {

	private final int id;
	private final String name;
	private final String level;
	private final String surface;
	private final boolean indoor;
	private final List<Integer> seasons;

	public Tournament(int id, String name, String level, String surface, boolean indoor, List<Integer> seasons) {
		this.id = id;
		this.name = name;
		this.level = level;
		this.surface = surface;
		this.indoor = indoor;
		this.seasons = seasons;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getLevel() {
		return level;
	}

	public String getSurface() {
		return surface;
	}

	public boolean isIndoor() {
		return indoor;
	}

	public List<Integer> getSeasons() {
		return seasons;
	}

	public String getFormatedSeasons() {
		if (seasons.isEmpty())
			return "";
		StringBuilder sb = new StringBuilder();
		Integer seasonRangeStart = seasons.get(0);
		int lastSeason = seasons.get(seasons.size() - 1);
		for (int season = seasonRangeStart; season <= lastSeason; season++) {
			if (!seasons.contains(season)) {
				if (seasonRangeStart != null) {
					appendSeasonRange(sb, seasonRangeStart, season - 1);
					seasonRangeStart = null;
				}
			}
			else if (seasonRangeStart == null)
				seasonRangeStart = season;
		}
		if (seasonRangeStart != null)
			appendSeasonRange(sb, seasonRangeStart, lastSeason);
		return sb.toString();
	}

	private static void appendSeasonRange(StringBuilder sb, int seasonStart, int seasonEnd) {
		if (sb.length() > 0)
			sb.append(", ");
		if (seasonStart == seasonEnd)
			sb.append(seasonStart);
		else {
			sb.append(seasonStart);
			sb.append("-");
			sb.append(seasonEnd);
		}
	}
}
