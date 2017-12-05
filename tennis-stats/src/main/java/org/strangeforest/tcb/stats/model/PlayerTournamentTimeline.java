package org.strangeforest.tcb.stats.model;

import java.time.*;
import java.util.*;
import java.util.function.*;

import org.strangeforest.tcb.util.*;

import static java.util.Comparator.*;
import static java.util.stream.Collectors.*;
import static org.strangeforest.tcb.stats.model.PlayerTimelineItem.*;

public class PlayerTournamentTimeline implements Comparable<PlayerTournamentTimeline> {

	private final PlayerTimeline timeline;
	private final int tournamentId;
	private String name;
	private final Map<String, String> levels = new LinkedHashMap<>(); // <Level, Name>
	private final Set<TimelineSurface> surfaces = new LinkedHashSet<>();
	private final List<LocalDate> dates = new ArrayList<>();
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
		return maxLevel().getCode();
	}

	TournamentLevel maxLevel() {
		return maxLevel.get();
	}

	public Iterable<TimelineSurface> getSurfaces() {
		return surfaces;
	}

	public int getSurfaceCount() {
		return surfaces.size();
	}

	public List<PlayerTimelineItem> getItems() {
		return timeline.getSeasons().stream().map(season -> {
			PlayerTimelineItem item = items.get(season);
			return item != null ? item : new PlayerTimelineItem(tournamentId, season, timeline.hasSeason(tournamentId, season) ? ABSENT : null);
		}).collect(toList());
	}

	public void addItem(PlayerTimelineItem item) {
		if (name == null)
			name = item.getTournamentName();
		items.put(item.getSeason(), item);
		levels.put(item.getLevel(), item.getName());
		String surface = item.getSurface();
		if (surface != null)
			surfaces.add(new TimelineSurface(surface, item.isIndoor()));
		dates.add(item.getDate());
	}

	public boolean isFirstByLevel() {
		return firstByLevel;
	}

	public void setFirstByLevel(boolean firstByLevel) {
		this.firstByLevel = firstByLevel;
	}

	@Override public int compareTo(PlayerTournamentTimeline tournament) {
		int result = maxLevel().compareTo(tournament.maxLevel());
		return result != 0 ? result : endDay.get().compareTo(tournament.endDay.get());
	}

	private final Supplier<TournamentLevel> maxLevel = Memoizer.of(
		() -> levels.keySet().stream().map(TournamentLevel::decode).min(naturalOrder()).get()
	);

	private final Supplier<MonthDay> endDay = Memoizer.of(
		() -> MonthDay.from(LocalDate.ofYearDay(REFERENCE_YEAR, (int)dates.stream().mapToInt(LocalDate::getDayOfYear).average().getAsDouble()))
	);

	private static final int REFERENCE_YEAR = 2000;


	public static class TimelineSurface {

		private final String surface;
		private final boolean indoor;

		public TimelineSurface(String surface) {
			this(surface, false);
		}

		public TimelineSurface(String surface, boolean indoor) {
			this.surface = surface;
			this.indoor = indoor;
		}

		public String getSurface() {
			return surface;
		}

		public boolean isIndoor() {
			return indoor;
		}

		@Override public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			TimelineSurface that = (TimelineSurface)o;
			return Objects.equals(surface, that.surface) && indoor == that.indoor;
		}

		@Override public int hashCode() {
			return Objects.hash(surface, indoor);
		}
	}
}
