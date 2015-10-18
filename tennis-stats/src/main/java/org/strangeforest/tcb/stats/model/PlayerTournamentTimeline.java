package org.strangeforest.tcb.stats.model;

import java.time.*;
import java.util.*;
import java.util.function.*;

import org.strangeforest.tcb.stats.util.*;

import static java.util.Comparator.*;
import static org.strangeforest.tcb.stats.util.DateUtil.*;

public class PlayerTournamentTimeline implements Comparable<PlayerTournamentTimeline> {

	private final PlayerTimeline timeline;
	private final int tournamentId;
	private String name;
	private final Map<String, String> levels = new LinkedHashMap<>(); // <Level, Name>
	private final Set<String> surfaces = new LinkedHashSet<>();
	private final List<Date> dates = new ArrayList<>();
	private final Map<Integer, PlayerTimelineItem> items = new HashMap<>();
	private boolean firstByLevel;

	public PlayerTournamentTimeline(PlayerTimeline timeline, int tournamentId) {
		this.timeline = timeline;
		this.tournamentId = tournamentId;
	}

	public int getTournamentId() {
		return tournamentId;
	}

	public String getName() {
		return name;
	}

	public Map<String, String> getLevelsAndNames() {
		return levels;
	}

	public int getLevelCount() {
		return levels.size();
	}

	public String getMaxLevel() {
		return maxLevel().code();
	}

	TournamentLevel maxLevel() {
		return maxLevel.get();
	}

	public Iterable<String> getSurfaces() {
		return surfaces;
	}

	public int getSurfaceCount() {
		return surfaces.size();
	}

	public List<PlayerTimelineItem> getItems() {
		List<PlayerTimelineItem> timelineItems = new ArrayList<>();
		for (int season : timeline.getSeasons()) {
			PlayerTimelineItem item = items.get(season);
			if (item == null)
				item = new PlayerTimelineItem(tournamentId, null, season, 0, null, null, null, null, null);
			timelineItems.add(item);
		}
		return timelineItems;
	}

	public void addItem(PlayerTimelineItem item) {
		if (name == null)
			name = item.getTournamentName();
		items.put(item.getSeason(), item);
		levels.put(item.getLevel(), item.getName());
		String surface = item.getSurface();
		if (surface != null)
			surfaces.add(surface);
		dates.add(item.getDate());
	}

	public boolean isFirstByLevel() {
		return firstByLevel;
	}

	public void setFirstByLevel(boolean firstByLevel) {
		this.firstByLevel = firstByLevel;
	}

	private int getDuration() {
		return maxLevel() == TournamentLevel.GRAND_SLAM ? 14 : 7;
	}

	@Override public int compareTo(PlayerTournamentTimeline tournament) {
		int result = maxLevel().compareTo(tournament.maxLevel());
		return result != 0 ? result : endDay.get().compareTo(tournament.endDay.get());
	}

	private final Supplier<TournamentLevel> maxLevel = Memoizer.of(
		() -> levels.keySet().stream().map(TournamentLevel::forCode).min(naturalOrder()).get()
	);

	private final Supplier<MonthDay> endDay = Memoizer.of(
		() -> MonthDay.from(LocalDate.ofYearDay(REFERENCE_YEAR, (int)dates.stream().mapToInt(date -> toLocalDate(date).plusDays(getDuration()).getDayOfYear()).average().getAsDouble()))
	);

	private static final int REFERENCE_YEAR = 2001;
}
