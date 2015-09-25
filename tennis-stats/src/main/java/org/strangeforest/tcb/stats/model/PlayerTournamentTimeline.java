package org.strangeforest.tcb.stats.model;

import java.time.*;
import java.util.*;

import static org.strangeforest.tcb.stats.util.DateUtil.*;

public class PlayerTournamentTimeline implements Comparable<PlayerTournamentTimeline> {

	private final PlayerTimeline timeline;
	private final int tournamentId;
	private final String level;
	private final String surface;
	private final String name;
	private final Date date;
	private final Map<Integer, PlayerTimelineItem> items;
	private boolean firstByLevel;

	public PlayerTournamentTimeline(PlayerTimeline timeline, int tournamentId, String level, String surface, String name, Date date) {
		this.timeline = timeline;
		this.tournamentId = tournamentId;
		this.level = level;
		this.surface = surface;
		this.name = name;
		this.date = date;
		items = new HashMap<>();
	}

	public int getTournamentId() {
		return tournamentId;
	}

	public String getLevel() {
		return level;
	}

	public String getSurface() {
		return surface;
	}

	public String getName() {
		return name;
	}

	public List<PlayerTimelineItem> getItems() {
		List<PlayerTimelineItem> timelineItems = new ArrayList<>();
		for (int season : timeline.getSeasons()) {
			PlayerTimelineItem item = items.get(season);
			if (item == null)
				item = new PlayerTimelineItem(tournamentId, season, 0, date, level, surface, name, null);
			timelineItems.add(item);
		}
		return timelineItems;
	}

	public void addItem(PlayerTimelineItem item) {
		items.put(item.getSeason(), item);
	}

	public boolean isFirstByLevel() {
		return firstByLevel;
	}

	public void setFirstByLevel(boolean firstByLevel) {
		this.firstByLevel = firstByLevel;
	}

	@Override public int compareTo(PlayerTournamentTimeline tournament) {
		int result = TournamentLevel.forCode(level).compareTo(TournamentLevel.forCode(tournament.level));
		return result != 0 ? result : MonthDay.from(toLocalDate(date)).compareTo(MonthDay.from(toLocalDate(tournament.date)));
	}
}
